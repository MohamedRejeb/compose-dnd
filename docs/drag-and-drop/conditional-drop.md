# Conditional Drop

By default, any dragged item can be dropped on any drop target. **Conditional drop** lets you control which drop targets accept which items, enabling scenarios such as typed drop zones, capacity limits, or permission-based dropping.

<video class="demo-phone" src="../../videos/conditional-drop.mp4" autoplay loop muted playsinline></video>

## Using canDrop

The `canDrop` parameter on the `dropTarget` modifier is a plain `Boolean`, evaluated by Compose like any other modifier argument. Because it is just a boolean expression, it can read live state -- including `state.draggedItem?.data` -- to accept or reject whichever item is currently being dragged:

```kotlin
Box(
    modifier = Modifier
        .dropTarget(
            key = "numbers-only",
            state = dragAndDropState,
            // Only accept items whose data is a number
            canDrop = dragAndDropState.draggedItem?.data is Int,
            onDrop = { state ->
                println("Accepted: ${state.data}")
            },
        )
) {
    Text("Numbers only")
}
```

When `canDrop` is `false`, the target is excluded from hover detection entirely: it will not trigger `onDragEnter`, `onDragExit`, or `onDrop` for that item, and `hoveredDropTargetKey` never points to it.

## Usage Scenarios

### Typed Drop Zones

Accept only specific data types in different zones:

```kotlin
@Composable
fun TypedDropZonesExample() {
    val dragAndDropState = rememberDragAndDropState<Any>()
    val draggedData = dragAndDropState.draggedItem?.data

    DragAndDropContainer(state = dragAndDropState) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Only accepts strings
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp)
                    .dropTarget(
                        key = "text-zone",
                        state = dragAndDropState,
                        canDrop = draggedData is String,
                        onDrop = { /* handle text drop */ },
                    )
            ) {
                Text("Text Zone")
            }

            // Only accepts integers
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp)
                    .dropTarget(
                        key = "number-zone",
                        state = dragAndDropState,
                        canDrop = draggedData is Int,
                        onDrop = { /* handle number drop */ },
                    )
            ) {
                Text("Number Zone")
            }
        }
    }
}
```

### Capacity Limits

Prevent dropping when a zone is full:

```kotlin
val maxItems = 5
var droppedItems by remember { mutableStateOf(listOf<String>()) }

Box(
    modifier = Modifier
        .dropTarget(
            key = "limited-zone",
            state = dragAndDropState,
            canDrop = droppedItems.size < maxItems,
            onDrop = { state ->
                droppedItems = droppedItems + state.data
            },
        )
) {
    Text("${droppedItems.size} / $maxItems")
}
```

### Preventing Self-Drop

Prevent an item from being dropped back onto itself:

```kotlin
Box(
    modifier = Modifier
        .dropTarget(
            key = item.id,
            state = dragAndDropState,
            canDrop = dragAndDropState.draggedItem?.key != item.id,
            onDrop = { state ->
                // Handle drop from a different item
            },
        )
)
```

## Interaction with dropTargets Parameter

The `canDrop` parameter on the drop target is different from the `dropTargets` parameter on the `draggableItem` modifier:

| Feature        | `dropTargets` (on draggableItem)       | `canDrop` (on dropTarget)                |
|----------------|-----------------------------------------|-------------------------------------------|
| Defined on     | The dragged item                       | The drop target                          |
| Filters by     | Drop target keys (allowlist)           | Custom boolean expression on live state  |
| Use case       | "This item can only go to zones A, B"  | "This zone only accepts certain items"   |

Both can be combined. The dragged item first checks its `dropTargets` list, and then the drop target checks its `canDrop` value.

## Visual Feedback

Combine `canDrop` with `isDropTargetHovered` to provide visual feedback:

```kotlin
@Composable
fun ConditionalDropWithFeedback() {
    val dragAndDropState = rememberDragAndDropState<String>()
    val isHovered = dragAndDropState.isDropTargetHovered("target-1")
    val draggedText = dragAndDropState.draggedItem?.data

    Box(
        modifier = Modifier
            .border(
                width = 2.dp,
                color = if (isHovered) Color.Green else Color.Gray,
            )
            .dropTarget(
                key = "target-1",
                state = dragAndDropState,
                // Only accept short strings
                canDrop = draggedText == null || draggedText.length <= 10,
                onDrop = { state ->
                    println("Dropped: ${state.data}")
                },
            )
    ) {
        Text("Short strings only")
    }
}
```

!!! note
    When `canDrop` is `false`, the drop target will not trigger `onDragEnter`, `onDragExit`, or `onDrop` for that item. `hoveredDropTargetKey` will not be set to this target's key, so visual hover feedback will not activate either.

!!! tip
    `reorderableItem` and `ReorderableItem` have their own `canDrop` parameter for keeping specific rows out of a reorder -- for example pinned or locked items. See [Reorder List: Pinned or Locked Items](reorder.md#pinned-or-locked-items-candrop).
