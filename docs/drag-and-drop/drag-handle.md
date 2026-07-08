# Drag Handle

By default, dragging can be initiated from anywhere within a draggable item. A **drag handle** restricts the drag gesture to a specific area within the item, such as a grip icon. This is useful when your item contains interactive elements like buttons or text fields that should not trigger a drag.

<video src="../../videos/drag-handle.mp4" autoplay loop muted playsinline width="720"></video>

## Overview

Two pieces work together:

1. Pass `hasDragHandle = true` to the `draggableItem` or `reorderableItem` modifier - this stops the item body from initiating drags.
2. Apply `Modifier.dragHandle(key, state)` to the composable that should act as the handle, using the same `key` as the item.

```kotlin
Row(
    modifier = Modifier
        .reorderableItem(
            key = item,
            data = item,
            state = dndState,
            hasDragHandle = true,
            onDragEnter = { /* reorder */ },
            draggableContent = { ItemRow(item) },
        ),
) {
    Icon(
        imageVector = Icons.Rounded.DragHandle,
        contentDescription = "Drag handle",
        modifier = Modifier.dragHandle(
            key = item,
            state = dndState,
        ), // only this area initiates drag
    )
    Text(item) // the rest of the item stays interactive
}
```

### dragHandle Parameters

| Parameter                    | Type                  | Default  | Description                                                        |
|------------------------------|-----------------------|----------|--------------------------------------------------------------------|
| `key`                        | `Any`                 | Required | The key of the draggable item this handle controls.               |
| `state`                      | `DragAndDropState<T>` | Required | The drag and drop state.                                           |
| `enabled`                    | `Boolean`             | `true`   | Whether the drag handle is active.                                 |
| `dragAfterLongPress`         | `Boolean`             | `false`  | If `true`, drag starts after a long press on the handle.          |
| `requireFirstDownUnconsumed` | `Boolean`             | `false`  | If `true`, the first down event on the handle must be unconsumed. |

## Full Example: Reorderable List with Handles

```kotlin
@Composable
fun DragHandleExample() {
    val dndState = rememberDragAndDropState<String>()
    var items by remember {
        mutableStateOf(listOf("Item 1", "Item 2", "Item 3"))
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = if (isDragging) 0f else 1f
                        }
                        .reorderableItem(
                            key = item,
                            data = item,
                            state = dndState,
                            hasDragHandle = true,
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
                                HandleRow(text = item)
                            },
                        )
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                ) {
                    // Only this area initiates the drag
                    Icon(
                        imageVector = Icons.Rounded.DragHandle,
                        contentDescription = "Drag handle",
                        modifier = Modifier.dragHandle(
                            key = item,
                            state = dndState,
                        ),
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(text = item)
                }
            }
        }
    }
}
```

## With the Wrapper Composables

Inside `DraggableItem` or `ReorderableItem` content, the scope provides a `Modifier.dragHandle()` overload that needs no `key` or `state` - and no `hasDragHandle` flag, since calling it marks the item automatically:

```kotlin
ReorderableItem(state = reorderState, key = item, data = item) {
    Row {
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = "Drag handle",
            modifier = Modifier.dragHandle(), // scope version
        )
        Text(item)
    }
}
```

The scope overload accepts the same `enabled`, `dragAfterLongPress`, and `requireFirstDownUnconsumed` parameters.

## How It Works

When an item has a drag handle:

- Touch events on the item body are **not** intercepted for drag gestures.
- Only touch events starting inside the handle area will initiate a drag.
- Other interactive elements (buttons, text fields, checkboxes) inside the item remain fully functional.

!!! tip
    Drag handles are especially useful in reorderable lists where each item contains clickable elements. Without a drag handle, tapping a button might accidentally start a drag gesture.
