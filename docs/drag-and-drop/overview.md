# Drag and Drop Overview

Compose DND provides a declarative API for adding drag and drop functionality to your Compose UI. The core building blocks are:

- `DragAndDropState` -- Holds the state for all drag and drop operations.
- `DragAndDropContainer` -- A container that wraps all draggable items and drop targets.
- `draggableItem` modifier -- Makes a composable draggable.
- `dropTarget` modifier -- Marks a composable as a drop target.
- `DraggableItem` composable -- A wrapper alternative to the `draggableItem` modifier (see [the alternative section](#alternative-draggableitem-wrapper-composable)).

## Creating DragAndDropState

Use `rememberDragAndDropState` to create and remember a `DragAndDropState` instance. The type parameter `T` defines the type of data carried by dragged items.

```kotlin
val dragAndDropState = rememberDragAndDropState<String>()
```

### Parameters

| Parameter                    | Type      | Default | Description                                                                 |
|------------------------------|-----------|---------|-----------------------------------------------------------------------------|
| `dragAfterLongPress`         | `Boolean` | `false` | If `true`, drag starts after a long press. If `false`, drag starts on touch move. Applied to all items unless overridden per item. |
| `requireFirstDownUnconsumed` | `Boolean` | `false` | If `true`, the first down pointer event must be unconsumed to initiate drag. |
| `reorderHysteresisDistance`  | `Dp`      | `DefaultReorderHysteresisDistance` (`8.dp`) | How far the cursor must move back, opposite the swap direction, before a just-swapped reorder target can be re-entered. Prevents swap oscillation during slow drags. `0.dp` disables it. |

## DragAndDropContainer

All draggable items and drop targets must be placed inside a `DragAndDropContainer`. This composable handles pointer input tracking and renders the drag shadow.

```kotlin
DragAndDropContainer(
    state = dragAndDropState,
    modifier = Modifier.fillMaxSize(),
    enabled = true,
) {
    // Draggable items and drop targets go here
}
```

### Parameters

| Parameter  | Type                    | Default          | Description                              |
|------------|-------------------------|------------------|------------------------------------------|
| `state`    | `DragAndDropState<T>`   | Required         | The drag and drop state.                 |
| `modifier` | `Modifier`              | `Modifier`       | Modifier for the container.              |
| `enabled`  | `Boolean`               | `true`           | Whether drag and drop is enabled.        |
| `content`  | `@Composable () -> Unit`| Required         | The content of the container.            |

## Making Items Draggable

Apply the `Modifier.draggableItem` modifier to any composable. Each item needs a unique `key`, the `data` to pass to the drop target when dropped, and a `draggableContent` that is rendered as the drag shadow:

```kotlin
val isDragging = dragAndDropState.isDragging("item-1")

Text(
    text = "Drag me",
    modifier = Modifier
        .graphicsLayer { alpha = if (isDragging) 0f else 1f }
        .draggableItem(
            key = "item-1",
            data = "Hello World",
            state = dragAndDropState,
            draggableContent = {
                Text("Drag me") // Content shown as the drag shadow
            },
        ),
)
```

Use `DragAndDropState.isDragging(key)` to check if a specific item is being dragged - hiding the original with `graphicsLayer { alpha = ... }` while the shadow follows the pointer is the usual pattern.

### Parameters

| Parameter                    | Type                           | Default                        | Description |
|------------------------------|--------------------------------|--------------------------------|-------------|
| `key`                        | `Any`                          | Required                       | Unique key identifying this item. |
| `data`                       | `T`                            | Required                       | Data passed to the drop target on drop. |
| `state`                      | `DragAndDropState<T>`          | Required                       | The drag and drop state. |
| `enabled`                    | `Boolean`                      | `true`                         | Whether this specific item is draggable. |
| `dragAfterLongPress`         | `Boolean`                      | Inherits from state            | Override the long-press drag behavior for this item. |
| `requireFirstDownUnconsumed` | `Boolean`                      | Inherits from state            | Override the unconsumed pointer requirement for this item. |
| `dropTargets`                | `List<Any>`                    | `emptyList()`                  | Restrict which drop targets this item can be dropped on. Empty means any target. |
| `dropStrategy`               | `DropStrategy`                 | `DropStrategy.SurfacePercentage` | Strategy for choosing the hovered drop target. |
| `dragAxis`                   | `DragAxis`                     | `DragAxis.Free`                | Constrain drag movement to one axis. See [Axis Lock](axis-lock.md). |
| `hasDragHandle`              | `Boolean`                      | `false`                        | If `true`, drag is initiated from a `dragHandle` modifier instead of the whole item. See [Drag Handle](drag-handle.md). |
| `dropAnimationSpec`          | `AnimationSpec<Offset>`        | `SpringSpec()`                 | Animation for position when dropping. |
| `sizeDropAnimationSpec`      | `AnimationSpec<Size>`          | `SpringSpec()`                 | Animation for size when dropping. |
| `draggableContent`           | `@Composable () -> Unit`       | Required                       | Content rendered as the drag shadow while dragging. |

## Drop Target

Use the `Modifier.dropTarget` extension to mark any composable as a drop target.

```kotlin
Box(
    modifier = Modifier
        .size(200.dp)
        .dropTarget(
            key = "target-1",
            state = dragAndDropState,
            onDrop = { state ->
                println("Dropped item: ${state.data}")
            },
            onDragEnter = { state ->
                println("Item entered: ${state.data}")
            },
            onDragExit = { state ->
                println("Item exited: ${state.data}")
            },
        )
) {
    Text("Drop here")
}
```

### Parameters

| Parameter             | Type                                   | Default            | Description |
|-----------------------|----------------------------------------|--------------------|-------------|
| `key`                 | `Any`                                  | Required           | Unique key identifying this drop target. |
| `state`               | `DragAndDropState<T>`                  | Required           | The drag and drop state. |
| `zIndex`              | `Float`                                | `0f`               | Z-index for overlapping targets. Higher values take priority. |
| `dropAlignment`       | `Alignment`                            | `Alignment.Center` | Alignment of the dropped item within the target for the drop animation. |
| `dropOffset`          | `Offset`                               | `Offset.Zero`      | Additional offset for the drop animation position. |
| `dropAnimationEnabled`| `Boolean`                              | `true`             | Whether to animate the drop. If `false`, the drop callback fires immediately. |
| `canDrop`             | `Boolean`                              | `true`             | Whether this target accepts drops. When `false`, dragged items cannot hover over or be dropped on this target. Can read `state.draggedItem?.data` to validate dynamically. |
| `onDrop`              | `(DraggedItemState<T>) -> Unit`        | `{}`               | Called when an item is dropped on this target. |
| `onDragEnter`         | `(DraggedItemState<T>) -> Unit`        | `{}`               | Called when a dragged item enters this target. |
| `onDragExit`          | `(DraggedItemState<T>) -> Unit`        | `{}`               | Called when a dragged item exits this target. |

### DraggedItemState

The callbacks receive a `DraggedItemState<T>` with the following properties:

- `key: Any` -- The key of the dragged item.
- `data: T` -- The data associated with the dragged item.
- `dragAmount: Offset` -- The total drag offset from the original position.

## Enable/Disable Drag and Drop

### Container Level

Disable drag and drop for all items within a container:

```kotlin
DragAndDropContainer(
    state = dragAndDropState,
    enabled = false, // Disables all drag and drop
) {
    // ...
}
```

### Item Level

Disable drag and drop for a specific item by passing `enabled = false` to the `draggableItem` modifier:

```kotlin
Text(
    text = "Cannot drag me",
    modifier = Modifier.draggableItem(
        key = "item-1",
        data = "Hello",
        state = dragAndDropState,
        enabled = false, // Only this item is disabled
        draggableContent = { Text("Cannot drag me") },
    ),
)
```

## Observing Drag State

You can observe which drop target is currently hovered using `DragAndDropState.hoveredDropTargetKey`:

```kotlin
val isHovered = dragAndDropState.hoveredDropTargetKey == "target-1"

Box(
    modifier = Modifier
        .border(
            width = 2.dp,
            color = if (isHovered) Color.Blue else Color.Gray,
        )
        .dropTarget(
            key = "target-1",
            state = dragAndDropState,
            onDrop = { /* ... */ },
        )
) {
    Text("Drop here")
}
```

You can also check if any item is being dragged using `DragAndDropState.draggedItem`:

```kotlin
val isDragging = dragAndDropState.draggedItem != null
```

## Full Working Example

```kotlin
@Composable
fun DragAndDropExample() {
    val dragAndDropState = rememberDragAndDropState<Int>()
    var isDropped by remember { mutableStateOf(false) }

    DragAndDropContainer(
        state = dragAndDropState,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize().padding(20.dp),
        ) {
            // Source area
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)),
            ) {
                if (!isDropped) {
                    val isDragging = dragAndDropState.isDragging(1)

                    RedBox(
                        text = "Drag me",
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                            .draggableItem(
                                key = 1,
                                data = 1,
                                state = dragAndDropState,
                                draggableContent = {
                                    RedBox(text = "Drag me")
                                },
                            ),
                    )
                }
            }

            // Target area
            val isHovered = dragAndDropState.hoveredDropTargetKey == "target"
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        width = 2.dp,
                        color = if (isHovered) Color.Blue else Color.Gray,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .dropTarget(
                        key = "target",
                        state = dragAndDropState,
                        onDrop = { isDropped = true },
                    ),
            ) {
                if (isDropped) {
                    RedBox(text = "Dropped!")
                } else {
                    Text("Drop here", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun RedBox(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .background(Color.Red, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White)
    }
}
```

## Alternative: DraggableItem Wrapper Composable

If you prefer wrapping composables over modifier chains, the `DraggableItem` composable offers the same features:

```kotlin
DraggableItem(
    state = dragAndDropState,
    key = "item-1",
    data = "Hello World",
) {
    // isDragging is available in this scope
    Text(
        text = "Drag me",
        modifier = Modifier
            .graphicsLayer {
                alpha = if (isDragging) 0f else 1f
            }
    )
}
```

It accepts the same parameters as the `draggableItem` modifier, with two differences:

- `draggableContent` is optional -- when `null`, the item content itself is used as the drag shadow.
- The `content` lambda runs in a `DraggableItemScope` that provides `key`, `isDragging`, and a scope-based `Modifier.dragHandle()` that doesn't need `key`/`state` arguments.

The same applies to reorder: `ReorderableItem` is the wrapper alternative to the `reorderableItem` modifier -- see [Reorder List](reorder.md).
