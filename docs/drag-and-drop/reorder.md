# Reorder List

Reordering builds on the core drag and drop primitives: the `reorderableItem` modifier makes an item both draggable and a drop target at the same time, which is all a sortable list needs.

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
| `dropStrategy`               | `DropStrategy`                            | `DropStrategy.SurfacePercentage` | Strategy for choosing the hovered target. |
| `dragAxis`                   | `DragAxis`                                | `DragAxis.Free`                  | Constrain drag movement to one axis. See [Axis Lock](axis-lock.md). |
| `hasDragHandle`              | `Boolean`                                 | `false`                          | If `true`, drag is initiated from a `dragHandle` modifier. See [Drag Handle](drag-handle.md). |
| `zIndex`                     | `Float`                                   | `0f`                             | Z-index for overlapping items. |
| `dropAnimationSpec`          | `AnimationSpec<Offset>`                   | `SpringSpec()`                   | Animation for position when dropping. |
| `sizeDropAnimationSpec`      | `AnimationSpec<Size>`                     | `SpringSpec()`                   | Animation for size when dropping. |
| `dropAlignment`              | `Alignment`                               | `Alignment.Center`               | Alignment of the dropped item within the target. |
| `dropOffset`                 | `Offset`                                  | `Offset.Zero`                    | Additional offset for the drop animation position. |
| `dropAnimationEnabled`       | `Boolean`                                 | `true`                           | Whether to animate the drop. |
| `onDrop`                     | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when an item is dropped on this target. |
| `onDragEnter`                | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when a dragged item enters this target. |
| `onDragExit`                 | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when a dragged item exits this target. |
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

## Reorder Hysteresis

When two items of different sizes swap, the swap itself can move the target back under the cursor and immediately trigger the reverse swap, causing the two items to oscillate during a slow drag. Compose DND prevents this with a built-in hysteresis: after a swap, the cursor has to travel a minimum distance in the opposite direction before the just-swapped target can be re-entered.

The default distance is `8.dp` and works out of the box. You can tune it on either state:

```kotlin
val dndState = rememberDragAndDropState<String>(
    reorderHysteresisDistance = 16.dp, // 0.dp disables hysteresis
)

// or with the wrapper API state
val reorderState = rememberReorderState<String>(
    reorderHysteresisDistance = 16.dp,
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
                        .animateItem()
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

## Alternative: Wrapper Composables

If you prefer wrapping composables over modifier chains, the reorder API is also available as `ReorderContainer` + `ReorderableItem` with a dedicated `ReorderState`:

```kotlin
val reorderState = rememberReorderState<String>()

ReorderContainer(
    state = reorderState,
) {
    LazyColumn {
        items(items, key = { it }) { item ->
            ReorderableItem(
                state = reorderState,
                key = item,
                data = item,
                onDrop = {},
                onDragEnter = { state ->
                    items = items.toMutableList().apply {
                        val index = indexOf(item)
                        if (index == -1) return@ReorderableItem
                        remove(state.data)
                        add(index, state.data)
                    }
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

Differences from the modifier API:

- `rememberReorderState` creates a `ReorderState`, a thin wrapper that exposes the underlying `DragAndDropState` as `reorderState.dndState` (needed for modifiers like `dragAutoScroll`). It accepts the same `dragAfterLongPress`, `requireFirstDownUnconsumed`, and `reorderHysteresisDistance` parameters.
- `ReorderableItem` accepts the same parameters as the `reorderableItem` modifier, but `draggableContent` is optional -- when `null`, the item content is used as the drag shadow.
- The `content` lambda runs in a `ReorderableItemScope` providing `key`, `isDragging`, and a scope-based `Modifier.dragHandle()` that doesn't need `key`/`state` arguments.
