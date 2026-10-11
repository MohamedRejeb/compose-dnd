# Reorder List

Reordering builds on the core drag and drop primitives: the `reorderableItem` modifier makes an item both draggable and a drop target at the same time, which is all a sortable list needs.

<video class="demo-phone" src="../../videos/reorder.mp4" autoplay loop muted playsinline></video>

## Setting Up

Reordering uses the standard `rememberDragAndDropState` and `DragAndDropContainer`:

```kotlin
val dndState = rememberDragAndDropState<String>()

DragAndDropContainer(
    state = dndState,
) {
    // Reorderable items go here
}
```

See [Drag and Drop Overview](overview.md) for the state and container parameters.

## reorderableItem Modifier

Apply `Modifier.reorderableItem` to each item that can be reordered:

```kotlin
items(items, key = { it }) { item ->
    val isDragging = dndState.isDragging(item)

    ItemCard(
        text = item,
        modifier = Modifier
            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
            .reorderableItem(
                key = item,
                data = item,
                state = dndState,
                onDragEnter = { state ->
                    items = items.toMutableList().apply {
                        val index = indexOf(item)
                        if (index != -1) {
                            remove(state.data)
                            add(index, state.data)
                        }
                    }
                },
                draggableContent = {
                    ItemCard(text = item, isDragShadow = true)
                },
            )
            .fillMaxWidth(),
    )
}
```

### Parameters

| Parameter                    | Type                                      | Default                          | Description |
|------------------------------|-------------------------------------------|----------------------------------|-------------|
| `key`                        | `Any`                                     | Required                         | Unique key identifying this item. |
| `data`                       | `T`                                       | Required                         | Data associated with this item. |
| `state`                      | `DragAndDropState<T>`                     | Required                         | The drag and drop state. |
| `enabled`                    | `Boolean`                                 | `true`                           | Whether this item is reorderable. |
| `dragAfterLongPress`         | `Boolean`                                 | Inherits from state              | Override long-press drag behavior. |
| `requireFirstDownUnconsumed` | `Boolean`                                 | Inherits from state              | Override unconsumed pointer requirement. |
| `dropTargets`                | `List<Any>`                               | `emptyList()`                    | Restrict which targets this item can be dropped on. |
| `dropStrategy`               | `DropStrategy`                            | `DropStrategy.Surface`           | Strategy for choosing the hovered target. |
| `dragAxis`                   | `DragAxis`                                | `DragAxis.Free`                  | Constrain drag movement to one axis. See [Axis Lock](axis-lock.md). |
| `hasDragHandle`              | `Boolean`                                 | `false`                          | If `true`, drag is initiated from a `dragHandle` modifier. See [Drag Handle](drag-handle.md). |
| `zIndex`                     | `Float`                                   | `0f`                             | Z-index for overlapping items. |
| `dropAnimationSpec`          | `AnimationSpec<Offset>`                   | `SpringSpec()`                   | Animation for position when dropping. |
| `dropAlignment`              | `Alignment`                               | `Alignment.Center`               | Alignment of the dropped item within the target. |
| `dropOffset`                 | `Offset`                                  | `Offset.Zero`                    | Additional offset for the drop animation position. |
| `dropAnimationEnabled`       | `Boolean`                                 | `true`                           | Whether to animate the drop. |
| `canDrop`                    | `Boolean`                                 | `true`                           | Whether this item currently accepts other items as a drop target. See [Pinned or Locked Items](#pinned-or-locked-items-candrop). |
| `onDrop`                     | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when an item is dropped on this target. |
| `onDragEnter`                | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when a dragged item enters this target. |
| `onDragExit`                 | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when a dragged item exits this target. |
| `onDragStart`                | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when a drag of this item starts. |
| `onDragEnd`                  | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called once the drag of this item is over. See [Persisting the New Order](#persisting-the-new-order). |
| `draggableContent`           | `@Composable () -> Unit`                  | Required                         | Content rendered as the drag shadow. |

## Reorder Logic with onDragEnter

The reorder logic is implemented in the `onDragEnter` callback. When a dragged item enters another item's area, you update the list order:

```kotlin
onDragEnter = { state ->
    items = items.toMutableList().apply {
        val targetIndex = indexOf(item)
        if (targetIndex != -1) {
            remove(state.data)
            add(targetIndex, state.data)
        }
    }
}
```

!!! note
    The `onDragEnter` callback receives the `DraggedItemState` of the item being dragged. Use `state.data` to identify the dragged item and reposition it in your list.

## Persisting the New Order

`onDragEnter` fires on every swap, which makes it the wrong place to write to a database or a `ViewModel` -- a drag across ten items fires it ten times. Use `onDragEnd` instead: it fires exactly once per drag, after the drop or return animation finishes, regardless of how many swaps happened along the way.

```kotlin
.reorderableItem(
    key = item,
    data = item,
    state = dndState,
    onDragEnter = { state ->
        // Update the in-memory list order live, for visual feedback
        items = items.toMutableList().apply {
            val index = indexOf(item)
            if (index != -1) {
                remove(state.data)
                add(index, state.data)
            }
        }
    },
    onDragEnd = {
        // The drag is over -- persist the final order once
        saveOrderToDatabase(items)
    },
    draggableContent = {
        ItemCard(text = item, isDragShadow = true)
    },
)
```

This also runs when the drag is cancelled or released outside any target, so it is a safe single place to sync state, log analytics, or re-enable UI that was disabled during the drag.

## Pinned or Locked Items (canDrop)

<video class="demo-phone" src="../../videos/pinned-chats.mp4" autoplay loop muted playsinline></video>

Pass `canDrop = false` to keep an item's slot out of reordering: dragged items pass over it without hovering it, so it is never swapped. Combine it with `enabled = false` to also stop the item itself from being dragged -- the usual combination for a pinned or locked row:

```kotlin
ItemCard(
    text = item.text,
    modifier = Modifier
        .reorderableItem(
            key = item.id,
            data = item,
            state = dndState,
            enabled = !item.isLocked,  // locked items can't be dragged
            canDrop = !item.isLocked,  // locked items can't be swapped into either
            onDragEnter = { state -> /* reorder logic */ },
            draggableContent = { ItemCard(text = item.text, isDragShadow = true) },
        ),
)
```

See [Conditional Drop](conditional-drop.md) for the equivalent `canDrop` on a plain `dropTarget`.

## How a Swap Is Decided

A dragged item takes the slot of another item when its **leading edge passes the middle of that item**: dragging down, the bottom edge of the dragged item has to get past the middle of the item below; dragging up, its top edge past the middle of the item above. Between rows, columns or lists, the edge is measured along the line between the two slots.

This holds whatever the sizes of the two items. Right after the swap the other item lies on the other side, and its middle is out of reach again, so holding still at the swap point never flips the two back and forth -- also when a short item passes a tall one.

Two more things keep the order under the pointer predictable:

- Items animated with `animateItem()` take a few frames to reach their new slot. Slots are compared at the place where the layout settles them, not where the items are passing through, so the animation does not change what the dragged item hovers.
- The `dropStrategy` of the item only picks among the slots it has reached, which matters when it reaches several at once (a fast drag, or a card entering another list).

A small distance is added around the swap point so that a shaky hold right on it does not swap either: after a swap, the drag has to come back by that distance, plus the spacing between the items, to undo it. The default is `8.dp`. You can tune it when creating the state:

```kotlin
val dndState = rememberDragAndDropState<String>(
    reorderHysteresisDistance = 16.dp, // 0.dp leaves only the spacing between items
)
```

## Observing Reorder State

You can observe the current drag state through `DragAndDropState`:

```kotlin
// Currently dragged item
val draggedItem = dndState.draggedItem

// Key of the drop target currently being hovered
val hoveredKey = dndState.hoveredDropTargetKey

// Whether a specific item is being dragged
val isDragging = dndState.isDragging(item)
```

## Full Working Example with LazyColumn

```kotlin
@Composable
fun ReorderListExample() {
    val dndState = rememberDragAndDropState<String>()
    var items by remember {
        mutableStateOf(
            listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
        )
    }

    DragAndDropContainer(
        state = dndState,
        modifier = Modifier.fillMaxSize().padding(20.dp),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(items, key = { it }) { item ->
                val isDragging = dndState.isDragging(item)

                ItemCard(
                    text = item,
                    modifier = Modifier
                        .animateItem()
                        .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                        .reorderableItem(
                            key = item,
                            data = item,
                            state = dndState,
                            onDragEnter = { state ->
                                items = items.toMutableList().apply {
                                    val index = indexOf(item)
                                    if (index != -1) {
                                        remove(state.data)
                                        add(index, state.data)
                                    }
                                }
                            },
                            draggableContent = {
                                ItemCard(
                                    text = item,
                                    isDragShadow = true,
                                )
                            },
                        )
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ItemCard(
    text: String,
    isDragShadow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(60.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragShadow) 8.dp else 2.dp,
        ),
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(text = text)
        }
    }
}
```

!!! tip
    Use `graphicsLayer { alpha = if (isDragging) 0f else 1f }` to hide the original item while it is being dragged, so only the drag shadow is visible. Adding `Modifier.animateItem()` inside a `LazyColumn` animates the displacement of the other items.

    Place `animateItem()` **before** `reorderableItem` in the modifier chain. This lets the library track the item's animated position, so dropping while items are still moving lands the drag shadow exactly where the item settles.

!!! tip
    If you draw a placeholder in the slot of the dragged item instead of hiding it, add `Modifier.zIndex(if (isDragging) -1f else 0f)` to the item. The items that slide past during a reorder are then drawn over the placeholder instead of behind it.

## Alternative: ReorderableItem Wrapper Composable

If you prefer wrapping composables over modifier chains, `ReorderableItem` offers the same features as the `reorderableItem` modifier, built on the same `DragAndDropState` and `DragAndDropContainer` as everything else:

```kotlin
val dndState = rememberDragAndDropState<String>()

DragAndDropContainer(
    state = dndState,
) {
    LazyColumn {
        items(items, key = { it }) { item ->
            ReorderableItem(
                state = dndState,
                key = item,
                data = item,
                onDragEnter = { state ->
                    items = items.toMutableList().apply {
                        val index = indexOf(item)
                        if (index == -1) return@ReorderableItem
                        remove(state.data)
                        add(index, state.data)
                    }
                },
                onDragEnd = {
                    saveOrderToDatabase(items)
                },
            ) {
                // isDragging is available in this scope
                Text(
                    text = item,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = if (isDragging) 0f else 1f
                        }
                )
            }
        }
    }
}
```

Differences from the `reorderableItem` modifier:

- `ReorderableItem` accepts the same parameters, including `canDrop`, `onDragStart`, and `onDragEnd`, but `draggableContent` is optional -- when `null`, the item content is used as the drag shadow.
- The `content` lambda runs in a `ReorderableItemScope` providing `key`, `isDragging`, and a scope-based `Modifier.dragHandle()` that doesn't need `key`/`state` arguments.

!!! warning "ReorderState is deprecated"
    `ReorderState`, `rememberReorderState`, `ReorderContainer`, and the `ReorderableItem(state: ReorderState, ...)` overload shown in older versions of this guide are deprecated. Use `rememberDragAndDropState`, `DragAndDropContainer`, and `ReorderableItem(state: DragAndDropState, ...)` as above -- one state type for both plain drag and drop and reordering. See the [migration guide](../migration-1.0.md#reorderstate-to-draganddropstate) for a before/after comparison.
