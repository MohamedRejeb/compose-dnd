# Drag Handle

By default, dragging can be initiated from anywhere within a `DraggableItem` or `ReorderableItem`. A **drag handle** restricts the drag gesture to a specific area within the item, such as a grip icon. This is useful when your item contains interactive elements like buttons or text fields that should not trigger a drag.

## Overview

The content lambda of `DraggableItem` and `ReorderableItem` provides a scope (`DraggableItemScope` / `ReorderableItemScope`) that exposes a `Modifier.dragHandle()` function. Apply it to the composable that should act as the handle:

```kotlin
ReorderableItem(state = reorderState, key = item, data = item) {
    Row {
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = "Drag handle",
            modifier = Modifier.dragHandle(), // only this area initiates drag
        )
        Text(item) // the rest of the item stays interactive
    }
}
```

Calling `dragHandle()` automatically switches the item to handle-driven dragging — no extra flag is needed on the item itself.

With the `draggableItem` / `reorderableItem` modifiers there is no content scope, so a standalone variant takes the item's `key` and `state` instead — see [Using Drag Handle with the Modifier API](#using-drag-handle-with-the-modifier-api).

### dragHandle Parameters

| Parameter                    | Type      | Default | Description                                                        |
|------------------------------|-----------|---------|--------------------------------------------------------------------|
| `enabled`                    | `Boolean` | `true`  | Whether the drag handle is active.                                 |
| `dragAfterLongPress`         | `Boolean` | `false` | If `true`, drag starts after a long press on the handle.          |
| `requireFirstDownUnconsumed` | `Boolean` | `false` | If `true`, the first down event on the handle must be unconsumed. |

## Using Drag Handle with ReorderableItem

```kotlin
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
                        Icon(
                            imageVector = Icons.Rounded.DragHandle,
                            contentDescription = "Drag handle",
                            modifier = Modifier.dragHandle(),
                        )

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
DraggableItem(
    state = dragAndDropState,
    key = "item-1",
    data = "Hello",
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = "Drag handle",
            modifier = Modifier.dragHandle(),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text("Drag me by the handle")
    }
}
```

## Using Drag Handle with the Modifier API

When using the `draggableItem` or `reorderableItem` modifiers, pass `hasDragHandle = true` to the item and use the standalone `Modifier.dragHandle(key, state)` overload on the handle composable:

```kotlin
Row(
    modifier = Modifier
        .draggableItem(
            key = "item-1",
            data = "Hello",
            state = dragAndDropState,
            hasDragHandle = true,
            draggableContent = { ItemCard("Hello") },
        ),
) {
    Icon(
        imageVector = Icons.Rounded.DragHandle,
        contentDescription = "Drag handle",
        modifier = Modifier.dragHandle(
            key = "item-1",
            state = dragAndDropState,
        ),
    )

    Text("Drag me by the handle")
}
```

The standalone overload accepts the same `enabled`, `dragAfterLongPress`, and `requireFirstDownUnconsumed` parameters as the scope version.

## How It Works

When `Modifier.dragHandle()` is applied inside an item's content:

- The item stops intercepting touch events on its body for drag gestures.
- Only touch events starting inside the handle area will initiate a drag.
- Other interactive elements (buttons, text fields, checkboxes) inside the item remain fully functional.

!!! tip
    Drag handles are especially useful in reorderable lists where each item contains clickable elements. Without a drag handle, tapping a button might accidentally start a drag gesture.
