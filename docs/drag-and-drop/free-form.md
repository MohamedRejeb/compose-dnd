# Free-Form Placement (Sticky Notes)

Reordering and Kanban both move items between fixed slots. Some boards have no slots at all -- a note can land anywhere. This guide walks through the pattern used by the Sticky Notes Board sample: a free-form canvas where notes carry an absolute `Offset` position instead of a place in a list.

<video class="demo-phone" src="../../videos/free-form.mp4" autoplay loop muted playsinline></video>

The building blocks:

- Notes are plain `draggableItem`s at an absolute position (`Modifier.offset { ... }`), not `reorderableItem`s -- there is no list order to maintain.
- The board itself is **one big `dropTarget`**, with `dropAlignment = Alignment.TopStart` and a `dropOffset` recomputed every frame from the dragged note's `dragAmount`, so the drop animation lands the ghost exactly on the snapped grid position.
- A trash target overlaps the board with a higher `zIndex`, so it wins the hover check whenever the pointer is over both.

## Notes as Absolute-Position draggableItems

Each note stores its own top-left `Offset` and is positioned with `Modifier.offset`, then made draggable:

```kotlin
notes.forEach { note ->
    StickyNote(
        note = note,
        modifier = Modifier
            .offset { IntOffset(note.position.x.roundToInt(), note.position.y.roundToInt()) }
            .graphicsLayer { alpha = if (state.isDragging(note.id)) 0f else 1f }
            .draggableItem(
                key = note.id,
                data = note.id,
                state = state,
                draggableContent = {
                    StickyNote(note = note, isLifted = true)
                },
            ),
    )
}
```

Nothing here is specific to free-form placement yet -- it is the same hide-the-original-while-dragging pattern used everywhere else. The free-form part is entirely in how the board, as a drop target, computes where the note should land.

## The Board as One Drop Target, with a Live dropOffset

The board is a single `dropTarget` the size of the whole canvas. Its `dropOffset` is not a constant: it is recalculated on every recomposition from the position of the note currently being dragged, snapped to the grid:

```kotlin
fun landingPosition(note: Note, dragAmount: Offset): Offset {
    val moved = note.position + dragAmount
    val snapped = if (snapToGrid) {
        Offset(
            x = (moved.x / gridStepPx).roundToInt() * gridStepPx,
            y = (moved.y / gridStepPx).roundToInt() * gridStepPx,
        )
    } else {
        moved
    }
    return Offset(
        x = snapped.x.coerceIn(0f, (boardSize.width - noteSizePx).coerceAtLeast(0f)),
        y = snapped.y.coerceIn(0f, (boardSize.height - noteSizePx).coerceAtLeast(0f)),
    )
}

val draggedNote = state.draggedItem?.let { dragged -> notes.find { it.id == dragged.data } }
val landing = draggedNote?.let { note ->
    landingPosition(note, state.draggedItem?.dragAmount ?: Offset.Zero)
}

Box(
    modifier = Modifier
        .dropTarget(
            key = BOARD_KEY,
            state = state,
            dropAlignment = Alignment.TopStart,
            // Land the drop animation exactly on the snapped position
            dropOffset = landing ?: Offset.Zero,
            onDrop = { dragged ->
                onNotesChange(
                    notes.map { note ->
                        if (note.id == dragged.data) {
                            note.copy(position = landingPosition(note, dragged.dragAmount))
                        } else {
                            note
                        }
                    },
                )
            },
        ),
) {
    // grid dots, notes, trash target...
}
```

Why this works: `dropAlignment = Alignment.TopStart` makes the target's alignment contribute no offset of its own, so the final drop position is simply the target's own top-left plus `dropOffset`. Since the board *is* the target, that reduces to `dropOffset` alone, in the board's local coordinate space -- the same space `note.position` lives in. Setting `dropOffset` to the live, snapped `landing` position on every frame means that whenever the drag ends, the drop animation already knows the exact snapped square to animate the ghost into, instead of animating to the raw pointer position and jumping to the grid afterwards.

`onDrop` then repeats the same `landingPosition` calculation to commit the final position into state -- using the same function for the live preview and the committed value keeps them in sync by construction.

!!! tip
    Showing a live preview of the landing square is a couple of lines once `landing` is computed: draw a dashed box at `landing` while `state.isDropTargetHovered(BOARD_KEY)` is `true`.

## A Trash Target That Wins on Overlap

The trash icon sits in a corner, visually overlapping the board. Both are drop targets that could receive the drop, so the trash target sets a higher `zIndex` to take priority whenever the two overlap:

```kotlin
TrashTarget(
    isVisible = state.draggedItem != null,
    isHovered = state.isDropTargetHovered(TRASH_TARGET_KEY),
    modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp)
        .dropTarget(
            key = TRASH_TARGET_KEY,
            state = state,
            // Wins over the board it overlaps
            zIndex = 1f,
            onDrop = { dragged ->
                onNotesChange(notes.filterNot { it.id == dragged.data })
            },
        ),
)
```

The board itself is left at the default `zIndex = 0f`. See [Drop Strategies: Z-Index](drop-strategies.md#z-index) for how overlapping targets are resolved in general.

## Toggling Snap-to-Grid

`snapToGrid` is a plain boolean read inside `landingPosition`, so toggling it with a `FilterChip` changes both the live preview and the committed position on the very next frame -- no extra plumbing needed, since both paths already go through the same function.

The complete implementation is in the sample app: [StickyNotesBoardScreen.kt](https://github.com/MohamedRejeb/compose-dnd/blob/main/sample/common/src/commonMain/kotlin/ui/StickyNotesBoardScreen.kt).
