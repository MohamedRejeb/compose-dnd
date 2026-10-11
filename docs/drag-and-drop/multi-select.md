# Drag to Select and Multi-Select

Some interactions need one draggable item to drive two different gestures depending on how the drag starts. This guide walks through the pattern used by the Photo Gallery sample, a grid where the same photo tile supports:

<video class="demo-phone" src="../../videos/multi-select.mp4" autoplay loop muted playsinline></video>

- **Drag to select** -- long-press an unselected photo and drag across the grid to select a range, like Google Photos.
- **Group drag** -- long-press an already-selected photo to drag the whole selection as a stack onto an album.

Both gestures reuse the same `draggableItem`; the mode is picked in `onDragStart`, based on whether the pressed photo was already selected.

The building blocks:

- One `DragAndDropState<Int>` where the dragged data is a single photo id -- the anchor of the gesture, not necessarily everything that moves.
- Every photo tile is **both** a `draggableItem` and a `dropTarget`, so a dragged photo entering another tile can extend the selection.
- `onDragStart` decides the mode; `draggableContent` renders different content (or nothing) depending on it.
- Selection state (`Set<Int>`) lives outside the drag payload, so a group drag can move more items than the single id carried by the drag.

## Picking the Mode in onDragStart

```kotlin
private enum class GalleryDragMode { None, Select, Move }

fun onDragStart(id: Int) {
    anchorId = id
    if (id in selection) {
        dragMode = GalleryDragMode.Move
    } else {
        dragMode = GalleryDragMode.Select
        baseSelection = selection
        selection = selection + id
    }
}
```

`onDragStart` fires once, before the pointer has moved, so it is the right place to branch: dragging a photo that is *not* selected starts a fresh range selection (and selects it immediately); dragging one that *is* selected picks up the whole selection as a group.

## Drag to Select

Each photo is a `draggableItem` with **no drag shadow** in select mode -- the pointer just extends the selection as it passes over tiles -- and a `dropTarget` that only accepts drags while selecting:

```kotlin
PhotoTile(
    photo = photo,
    isSelected = isSelected,
    modifier = Modifier
        .draggableItem(
            key = photo.id,
            data = photo.id,
            state = state,
            dropStrategy = DropStrategy.CenterDistance,
            onDragStart = { gallery.onDragStart(it.data) },
            onDragEnd = { gallery.onDragEnd() },
            draggableContent = {
                // Drag-select has no shadow, only a group drag shows one
                if (gallery.dragMode == GalleryDragMode.Move) {
                    SelectionStack(photos = gallery.photos.filter { it.id in gallery.selection })
                }
            },
        ).dropTarget(
            key = photo.id,
            state = state,
            canDrop = gallery.dragMode == GalleryDragMode.Select,
            dropAnimationEnabled = false,
            onDragEnter = { gallery.extendSelectionTo(photo.id) },
        ),
)
```

- `canDrop = gallery.dragMode == GalleryDragMode.Select` keeps the tile out of hover detection during a group drag, so photos don't re-select themselves while being carried to an album. See [Conditional Drop](conditional-drop.md).
- `dropAnimationEnabled = false` matters here because this target is only ever used for the live range extension in `onDragEnter` -- there is no meaningful "drop" on a photo tile.
- `dropStrategy = DropStrategy.CenterDistance` picks the tile whose center is closest to the dragged photo, which behaves well for equally sized grid cells. See [Drop Strategies](drop-strategies.md).
- `onDragEnter` calls `extendSelectionTo`, which selects every photo between the anchor and the entered tile:

```kotlin
fun extendSelectionTo(id: Int) {
    if (dragMode != GalleryDragMode.Select) return
    val anchorIndex = photos.indexOfFirst { it.id == anchorId }
    val index = photos.indexOfFirst { it.id == id }
    if (anchorIndex == -1 || index == -1) return

    val range = photos.subList(minOf(anchorIndex, index), maxOf(anchorIndex, index) + 1)
    selection = baseSelection + range.map { it.id }
}
```

Because `baseSelection` snapshots the selection *before* the drag started, dragging back and forth shrinks and grows the range instead of accumulating leftover selections.

## Group Drag

When the pressed photo is already selected, `dragMode` becomes `Move` and `draggableContent` renders a fanned stack of the selected photos with a count badge, instead of nothing:

```kotlin
@Composable
private fun SelectionStack(photos: List<Photo>) {
    Box(modifier = Modifier.size(96.dp)) {
        photos.takeLast(3).forEachIndexed { index, photo ->
            PhotoArt(
                photo = photo,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .graphicsLayer { rotationZ = (index - 1) * 8f }
                    .shadow(8.dp, RoundedCornerShape(8.dp)),
            )
        }
        // count badge in the corner...
    }
}
```

The album row is a plain `dropTarget` per album. Note that `onDrop` moves `gallery.selection`, not `state.data` -- the drag only ever carries the one id it started on, but the whole selection is what the group drag represents:

```kotlin
AlbumChip(
    album = album,
    isHovered = state.isDropTargetHovered("album-${album.key}"),
    modifier = Modifier
        .weight(1f)
        .dropTarget(
            key = "album-${album.key}",
            state = state,
            canDrop = isMoving,
            onDrop = { onDropOnAlbum(album.key) },
        ),
)

fun moveSelectionTo(albumKey: String) {
    val moved = photos.filter { it.id in selection }
    photos = photos.filterNot { it.id in selection }
    albums = albums.map { album ->
        if (album.key == albumKey) album.copy(photos = album.photos + moved) else album
    }
    selection = emptySet()
}
```

This is the key idea for group drags: the library only ever drags one payload, but nothing stops your `onDrop`/`onDragEnter` handlers from acting on a larger piece of app state (the selection) that the dragged id merely represents.

## Suppressing Auto Scroll During a Group Drag

The gallery grid uses [auto scroll](auto-scroll.md) while selecting, but disables it while carrying a group to an album, since the pointer is headed for the album row above the grid, not further into it:

```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 96.dp),
    state = gridState,
    modifier = Modifier
        .weight(1f)
        .then(if (isMoving) Modifier else Modifier.dragAutoScroll(state, gridState)),
) {
    // photo tiles...
}
```

The complete implementation is in the sample app: [PhotoGalleryScreen.kt](https://github.com/MohamedRejeb/compose-dnd/blob/main/sample/common/src/commonMain/kotlin/ui/PhotoGalleryScreen.kt).
