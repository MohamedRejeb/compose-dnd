# Axis Lock

By default, items can be dragged freely in any direction. **Axis lock** constrains the drag movement to a single axis -- either horizontal or vertical. This is useful for lists, sliders, or any UI where dragging should only occur along one dimension.

<video src="../../videos/axis-locked.mp4" autoplay loop muted playsinline width="720"></video>

!!! warning "Experimental API"
    The axis lock API is annotated with `@ExperimentalDndApi`. You must opt in with `@OptIn(ExperimentalDndApi::class)` to use it.

## DragAxis

The `DragAxis` enum defines the available axis constraints:

| Value        | Description                                             |
|--------------|---------------------------------------------------------|
| `Free`       | Item can be dragged in any direction (default).         |
| `Horizontal` | Item can only be dragged along the horizontal axis.     |
| `Vertical`   | Item can only be dragged along the vertical axis.       |

## Usage with draggableItem

```kotlin
@OptIn(ExperimentalDndApi::class)
val isDragging = dragAndDropState.isDragging("item-1")

Text(
    text = "I can only move up and down",
    modifier = Modifier
        .graphicsLayer {
            alpha = if (isDragging) 0f else 1f
        }
        .draggableItem(
            key = "item-1",
            data = "Hello",
            state = dragAndDropState,
            dragAxis = DragAxis.Vertical,
            draggableContent = {
                Text(text = "I can only move up and down")
            },
        ),
)
```

## Usage with reorderableItem

Axis lock works naturally with reorderable lists. For a vertical list, lock to the vertical axis so items cannot be dragged sideways:

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun VerticalLockedReorderExample() {
    val dndState = rememberDragAndDropState<String>()
    var items by remember {
        mutableStateOf(listOf("Item 1", "Item 2", "Item 3", "Item 4"))
    }

    DragAndDropContainer(
        state = dndState,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            items(items, key = { it }) { item ->
                val isDragging = dndState.isDragging(item)

                ItemCard(
                    text = item,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = if (isDragging) 0f else 1f
                        }
                        .reorderableItem(
                            key = item,
                            data = item,
                            state = dndState,
                            dragAxis = DragAxis.Vertical,
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
                                ItemCard(text = item)
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(56.dp),
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

## Horizontal Axis Lock

For a horizontal list or row:

```kotlin
@OptIn(ExperimentalDndApi::class)
ItemCard(
    text = item,
    modifier = Modifier.reorderableItem(
        key = item,
        data = item,
        state = dndState,
        dragAxis = DragAxis.Horizontal,
        onDragEnter = { /* reorder logic */ },
        draggableContent = {
            ItemCard(text = item)
        },
    ),
)
```

## How It Works

When axis lock is active:

- **Vertical** -- The drag shadow moves only along the Y axis. Any horizontal movement from the pointer is ignored, keeping the item aligned in its column.
- **Horizontal** -- The drag shadow moves only along the X axis. Any vertical movement is ignored.
- **Free** -- Both axes are tracked, allowing unconstrained dragging.

!!! tip
    Axis lock is particularly useful combined with auto scroll. For vertical lists, locking to `DragAxis.Vertical` ensures the drag shadow stays aligned with the list while auto scroll handles navigation.
