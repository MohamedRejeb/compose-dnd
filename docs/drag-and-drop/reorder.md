# Reorder List

Compose DND provides a higher-level API for reordering items in a list. The reorder API builds on top of the core drag and drop primitives, combining both draggable and drop target behavior into a single `ReorderableItem` composable.

## Creating ReorderState

Use `rememberReorderState` to create and remember a `ReorderState` instance:

```kotlin
val reorderState = rememberReorderState<String>()
```

### Parameters

| Parameter            | Type      | Default | Description                                                                     |
|----------------------|-----------|---------|---------------------------------------------------------------------------------|
| `dragAfterLongPress` | `Boolean` | `false` | If `true`, drag starts after a long press. Applied to all items unless overridden. |

## ReorderContainer

All reorderable items must be placed inside a `ReorderContainer`:

```kotlin
ReorderContainer(
    state = reorderState,
    modifier = Modifier.fillMaxSize(),
) {
    // Reorderable items go here
}
```

### Parameters

| Parameter  | Type                    | Default    | Description                        |
|------------|-------------------------|------------|------------------------------------|
| `state`    | `ReorderState<T>`       | Required   | The reorder state.                 |
| `modifier` | `Modifier`              | `Modifier` | Modifier for the container.        |
| `enabled`  | `Boolean`               | `true`     | Whether reordering is enabled.     |
| `content`  | `@Composable () -> Unit`| Required   | The content of the container.      |

## ReorderableItem

`ReorderableItem` is both a draggable item and a drop target. Use it for each item that can be reordered:

```kotlin
ReorderableItem(
    state = reorderState,
    key = item.id,
    data = item,
    onDragEnter = { state ->
        // Reorder the list when a dragged item enters this item's area
    },
) {
    Text(
        text = item.name,
        modifier = Modifier
            .graphicsLayer {
                alpha = if (isDragging) 0f else 1f
            }
    )
}
```

### Parameters

| Parameter                    | Type                                      | Default                          | Description |
|------------------------------|-------------------------------------------|----------------------------------|-------------|
| `state`                      | `ReorderState<T>`                         | Required                         | The reorder state. |
| `key`                        | `Any`                                     | Required                         | Unique key identifying this item. |
| `data`                       | `T`                                       | Required                         | Data associated with this item. |
| `modifier`                   | `Modifier`                                | `Modifier`                       | Modifier for the item. |
| `zIndex`                     | `Float`                                   | `0f`                             | Z-index for overlapping items. |
| `enabled`                    | `Boolean`                                 | `true`                           | Whether this item is reorderable. |
| `dragAfterLongPress`         | `Boolean`                                 | Inherits from state              | Override long-press drag behavior. |
| `requireFirstDownUnconsumed` | `Boolean`                                 | Inherits from state              | Override unconsumed pointer requirement. |
| `dropTargets`                | `List<Any>`                               | `emptyList()`                    | Restrict which targets this item can be dropped on. |
| `dropStrategy`               | `DropStrategy`                            | `DropStrategy.SurfacePercentage` | Strategy for choosing the hovered target. |
| `onDrop`                     | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when an item is dropped on this target. |
| `onDragEnter`                | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when a dragged item enters this target. |
| `onDragExit`                 | `(DraggedItemState<T>) -> Unit`           | `{}`                             | Called when a dragged item exits this target. |
| `dropAnimationSpec`          | `AnimationSpec<Offset>`                   | `SpringSpec()`                   | Animation for position when dropping. |
| `sizeDropAnimationSpec`      | `AnimationSpec<Size>`                     | `SpringSpec()`                   | Animation for size when dropping. |
| `draggableContent`           | `(@Composable () -> Unit)?`               | `null`                           | Custom drag shadow content. |
| `content`                    | `@Composable ReorderableItemScope.() -> Unit` | Required                     | The item content. |

### ReorderableItemScope

`ReorderableItemScope` extends `DraggableItemScope` and provides:

- `key: Any` -- The key of this item.
- `isDragging: Boolean` -- Whether this item is currently being dragged.

## Reorder Logic with onDragEnter

The reorder logic is implemented in the `onDragEnter` callback. When a dragged item enters another item's area, you update the list order:

```kotlin
onDragEnter = { state ->
    items = items.toMutableList().apply {
        val targetIndex = indexOf(item)
        if (targetIndex == -1) return@ReorderableItem
        remove(state.data)
        add(targetIndex, state.data)
    }
}
```

!!! note
    The `onDragEnter` callback receives the `DraggedItemState` of the item being dragged. Use `state.data` to identify the dragged item and reposition it in your list.

## Observing Reorder State

You can observe the current drag state through `ReorderState`:

```kotlin
// Currently dragged item
val draggedItem = reorderState.draggedItem

// Key of the drop target currently being hovered
val hoveredKey = reorderState.hoveredDropTargetKey
```

## Full Working Example with LazyColumn

```kotlin
@Composable
fun ReorderListExample() {
    val reorderState = rememberReorderState<String>()
    var items by remember {
        mutableStateOf(
            listOf("Item 1", "Item 2", "Item 3", "Item 4", "Item 5")
        )
    }
    val lazyListState = rememberLazyListState()

    ReorderContainer(
        state = reorderState,
        modifier = Modifier.fillMaxSize().padding(20.dp),
    ) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
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
                    draggableContent = {
                        ItemCard(
                            text = item,
                            isDragShadow = true,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ItemCard(
                        text = item,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                            .fillMaxWidth(),
                    )
                }
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
    Use `graphicsLayer { alpha = if (isDragging) 0f else 1f }` to hide the original item while it is being dragged, so only the drag shadow is visible.
