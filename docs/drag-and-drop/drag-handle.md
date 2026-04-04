# Drag Handle

By default, dragging can be initiated from anywhere within a `DraggableItem` or `ReorderableItem`. A **drag handle** restricts the drag gesture to a specific area within the item, such as a grip icon. This is useful when your item contains interactive elements like buttons or text fields that should not trigger a drag.

## Overview

To use a drag handle:

1. Set `dragAfterLongPress = false` on your item (this is the default).
2. Enable the drag handle on the item by setting `hasDragHandle = true`.
3. Place a `DragHandle` composable inside the item's content to define the draggable area.

!!! warning "Experimental API"
    The drag handle API is annotated with `@ExperimentalDndApi`. You must opt in with `@OptIn(ExperimentalDndApi::class)` to use it.

## Using Drag Handle with ReorderableItem

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun DragHandleExample() {
    val reorderState = rememberReorderState<String>()
    var items by remember {
        mutableStateOf(listOf("Item 1", "Item 2", "Item 3"))
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
                    hasDragHandle = true,
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 16.dp),
                    ) {
                        // Only this area initiates the drag
                        DragHandle {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Drag handle",
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(text = item)
                    }
                }
            }
        }
    }
}
```

## Using Drag Handle with DraggableItem

The drag handle also works with `DraggableItem`:

```kotlin
@OptIn(ExperimentalDndApi::class)
DraggableItem(
    state = dragAndDropState,
    key = "item-1",
    data = "Hello",
    hasDragHandle = true,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DragHandle {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag handle",
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text("Drag me by the handle")
    }
}
```

## How It Works

When `hasDragHandle = true`:

- Touch events on the item body are **not** intercepted for drag gestures.
- Only touch events starting inside the `DragHandle` composable will initiate a drag.
- Other interactive elements (buttons, text fields, checkboxes) inside the item remain fully functional.

!!! tip
    Drag handles are especially useful in reorderable lists where each item contains clickable elements. Without a drag handle, tapping a button might accidentally start a drag gesture.
