# Conditional Drop

By default, any dragged item can be dropped on any drop target. **Conditional drop** lets you control which drop targets accept which items, enabling scenarios such as typed drop zones, capacity limits, or permission-based dropping.

## Using canDrop

The `canDrop` parameter on the `dropTarget` modifier accepts a lambda that receives the `DraggedItemState` and returns a `Boolean`. If it returns `false`, the drop target will not respond to that dragged item.

```kotlin
Box(
    modifier = Modifier
        .dropTarget(
            key = "numbers-only",
            state = dragAndDropState,
            canDrop = { state ->
                // Only accept items whose data is a number
                state.data is Int
            },
            onDrop = { state ->
                println("Accepted: ${state.data}")
            },
        )
) {
    Text("Numbers only")
}
```

!!! warning "Experimental API"
    The `canDrop` parameter is annotated with `@ExperimentalDndApi`. You must opt in with `@OptIn(ExperimentalDndApi::class)` to use it.

## Usage Scenarios

### Typed Drop Zones

Accept only specific data types in different zones:

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun TypedDropZonesExample() {
    val dragAndDropState = rememberDragAndDropState<Any>()

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
                        canDrop = { it.data is String },
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
                        canDrop = { it.data is Int },
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
@OptIn(ExperimentalDndApi::class)
val maxItems = 5
var droppedItems by remember { mutableStateOf(listOf<String>()) }

Box(
    modifier = Modifier
        .dropTarget(
            key = "limited-zone",
            state = dragAndDropState,
            canDrop = { droppedItems.size < maxItems },
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
@OptIn(ExperimentalDndApi::class)
Box(
    modifier = Modifier
        .dropTarget(
            key = item.id,
            state = dragAndDropState,
            canDrop = { state -> state.key != item.id },
            onDrop = { state ->
                // Handle drop from a different item
            },
        )
)
```

## Interaction with dropTargets Parameter

The `canDrop` parameter on the drop target is different from the `dropTargets` parameter on the `draggableItem` modifier:

| Feature        | `dropTargets` (on draggableItem)       | `canDrop` (on dropTarget)                |
|----------------|----------------------------------------|------------------------------------------|
| Defined on     | The dragged item                       | The drop target                          |
| Filters by     | Drop target keys (allowlist)           | Custom logic based on dragged item state |
| Use case       | "This item can only go to zones A, B"  | "This zone only accepts certain items"   |

Both can be combined. The dragged item first checks its `dropTargets` list, and then the drop target checks its `canDrop` lambda.

## Visual Feedback

Combine `canDrop` with `hoveredDropTargetKey` to provide visual feedback:

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun ConditionalDropWithFeedback() {
    val dragAndDropState = rememberDragAndDropState<String>()
    val isHovered = dragAndDropState.hoveredDropTargetKey == "target-1"

    Box(
        modifier = Modifier
            .border(
                width = 2.dp,
                color = if (isHovered) Color.Green else Color.Gray,
            )
            .dropTarget(
                key = "target-1",
                state = dragAndDropState,
                canDrop = { state ->
                    state.data.length <= 10 // Only accept short strings
                },
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
    When `canDrop` returns `false`, the drop target will not trigger `onDragEnter`, `onDragExit`, or `onDrop` for that item. The `hoveredDropTargetKey` will not be set to this target's key, so visual hover feedback will not activate.
