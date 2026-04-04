# Axis Lock

By default, items can be dragged freely in any direction. **Axis lock** constrains the drag movement to a single axis -- either horizontal or vertical. This is useful for lists, sliders, or any UI where dragging should only occur along one dimension.

!!! warning "Experimental API"
    The axis lock API is annotated with `@ExperimentalDndApi`. You must opt in with `@OptIn(ExperimentalDndApi::class)` to use it.

## DragAxis

The `DragAxis` enum defines the available axis constraints:

| Value        | Description                                             |
|--------------|---------------------------------------------------------|
| `Free`       | Item can be dragged in any direction (default).         |
| `Horizontal` | Item can only be dragged along the horizontal axis.     |
| `Vertical`   | Item can only be dragged along the vertical axis.       |

## Usage with DraggableItem

```kotlin
@OptIn(ExperimentalDndApi::class)
DraggableItem(
    state = dragAndDropState,
    key = "item-1",
    data = "Hello",
    dragAxis = DragAxis.Vertical,
) {
    Text(
        text = "I can only move up and down",
        modifier = Modifier.graphicsLayer {
            alpha = if (isDragging) 0f else 1f
        },
    )
}
```

## Usage with ReorderableItem

Axis lock works naturally with reorderable lists. For a vertical list, lock to the vertical axis so items cannot be dragged sideways:

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun VerticalLockedReorderExample() {
    val reorderState = rememberReorderState<String>()
    var items by remember {
        mutableStateOf(listOf("Item 1", "Item 2", "Item 3", "Item 4"))
    }

    ReorderContainer(
        state = reorderState,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            items(items, key = { it }) { item ->
                ReorderableItem(
                    state = reorderState,
                    key = item,
                    data = item,
                    dragAxis = DragAxis.Vertical,
                    onDrop = {},
                    onDragEnter = { state ->
                        items = items.toMutableList().apply {
                            val index = indexOf(item)
                            if (index == -1) return@ReorderableItem
                            remove(state.data)
                            add(index, state.data)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Card(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                        ) {
                            Text(text = item)
                        }
                    }
                }
            }
        }
    }
}
```

## Horizontal Axis Lock

For a horizontal list or row:

```kotlin
@OptIn(ExperimentalDndApi::class)
ReorderableItem(
    state = reorderState,
    key = item,
    data = item,
    dragAxis = DragAxis.Horizontal,
    onDragEnter = { /* reorder logic */ },
) {
    // ...
}
```

## How It Works

When axis lock is active:

- **Vertical** -- The drag shadow moves only along the Y axis. Any horizontal movement from the pointer is ignored, keeping the item aligned in its column.
- **Horizontal** -- The drag shadow moves only along the X axis. Any vertical movement is ignored.
- **Free** -- Both axes are tracked, allowing unconstrained dragging.

!!! tip
    Axis lock is particularly useful combined with auto scroll. For vertical lists, locking to `DragAxis.Vertical` ensures the drag shadow stays aligned with the list while auto scroll handles navigation.
