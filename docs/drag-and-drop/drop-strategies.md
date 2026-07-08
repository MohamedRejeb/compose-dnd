# Drop Strategies

When a dragged item overlaps multiple drop targets, a **drop strategy** determines which target is considered "hovered." Compose DND provides several built-in strategies and allows you to create custom ones.

<video src="../../videos/drop-strategies.mp4" autoplay loop muted playsinline width="720"></video>

## What Are Drop Strategies?

During a drag operation, the dragged item may visually overlap more than one drop target at the same time. The drop strategy is the algorithm that decides which single drop target should receive the `onDragEnter`, `onDragExit`, and `onDrop` callbacks.

## Built-in Strategies

### SurfacePercentage (Default)

Selects the drop target that has the highest percentage of its surface covered by the dragged item. This is the default strategy and works well for most use cases.

```kotlin
Text(
    text = "Hello",
    modifier = Modifier.draggableItem(
        key = "item-1",
        data = "Hello",
        state = dragAndDropState,
        dropStrategy = DropStrategy.SurfacePercentage,
        draggableContent = {
            Text(text = "Hello")
        },
    ),
)
```

The percentage is calculated as:

```
overlapping area / dragged item area
```

This means a smaller drop target that is fully covered by the dragged item may be preferred over a larger target that is only partially covered.

### Surface

Selects the drop target with the largest absolute overlapping area (in pixels) with the dragged item, regardless of the target's own size.

```kotlin
Text(
    text = "Hello",
    modifier = Modifier.draggableItem(
        key = "item-1",
        data = "Hello",
        state = dragAndDropState,
        dropStrategy = DropStrategy.Surface,
        draggableContent = {
            Text(text = "Hello")
        },
    ),
)
```

### CenterDistance

Selects the drop target whose center is closest to the center of the dragged item. This strategy ignores overlap area entirely and focuses on proximity.

```kotlin
Text(
    text = "Hello",
    modifier = Modifier.draggableItem(
        key = "item-1",
        data = "Hello",
        state = dragAndDropState,
        dropStrategy = DropStrategy.CenterDistance,
        draggableContent = {
            Text(text = "Hello")
        },
    ),
)
```

## Comparison

| Strategy            | Algorithm                         | Best For                                       |
|---------------------|-----------------------------------|-------------------------------------------------|
| `SurfacePercentage` | Max overlapping area / item area  | General purpose, mixed-size targets             |
| `Surface`           | Max absolute overlapping area     | Same-size targets, grid layouts                 |
| `CenterDistance`    | Min distance between centers      | Precise targeting, small targets                |

## Z-Index

All built-in strategies account for the `zIndex` property of drop targets. When drop targets overlap visually, the one with a higher `zIndex` takes priority:

```kotlin
Box(
    modifier = Modifier
        .dropTarget(
            key = "background-target",
            state = dragAndDropState,
            zIndex = 0f, // Lower priority
            onDrop = { /* ... */ },
        )
) {
    Box(
        modifier = Modifier
            .dropTarget(
                key = "foreground-target",
                state = dragAndDropState,
                zIndex = 1f, // Higher priority
                onDrop = { /* ... */ },
            )
    )
}
```

## Usage with reorderableItem

Drop strategies also work with the `reorderableItem` modifier:

```kotlin
Card(
    modifier = Modifier.reorderableItem(
        key = item,
        data = item,
        state = dndState,
        dropStrategy = DropStrategy.CenterDistance,
        onDragEnter = { state ->
            // Reorder logic
        },
        draggableContent = {
            // Drag shadow content
        },
    ),
) {
    // ...
}
```

## Custom Drop Strategy

You can create a custom drop strategy by implementing the `DropStrategy` interface:

```kotlin
object MyCustomStrategy : DropStrategy {
    override fun <T> getHoveredDropTarget(
        draggedItemTopLeft: Offset,
        draggedItemSize: Size,
        dropTargets: List<DropTargetState<T>>,
    ): DropTargetState<T>? {
        // Your custom logic here
        return dropTargets.firstOrNull()
    }
}
```

Then use it:

```kotlin
Text(
    text = "Hello",
    modifier = Modifier.draggableItem(
        key = "item-1",
        data = "Hello",
        state = dragAndDropState,
        dropStrategy = MyCustomStrategy,
        draggableContent = {
            Text(text = "Hello")
        },
    ),
)
```
