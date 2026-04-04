# Drop Strategies

When a dragged item overlaps multiple drop targets, a **drop strategy** determines which target is considered "hovered." Compose DND provides several built-in strategies and allows you to create custom ones.

## What Are Drop Strategies?

During a drag operation, the dragged item may visually overlap more than one drop target at the same time. The drop strategy is the algorithm that decides which single drop target should receive the `onDragEnter`, `onDragExit`, and `onDrop` callbacks.

## Built-in Strategies

### SurfacePercentage (Default)

Selects the drop target that has the highest percentage of its surface covered by the dragged item. This is the default strategy and works well for most use cases.

```kotlin
DraggableItem(
    state = dragAndDropState,
    key = "item-1",
    data = "Hello",
    dropStrategy = DropStrategy.SurfacePercentage,
) {
    // ...
}
```

The percentage is calculated as:

```
overlapping area / dragged item area
```

This means a smaller drop target that is fully covered by the dragged item may be preferred over a larger target that is only partially covered.

### Surface

Selects the drop target with the largest absolute overlapping area (in pixels) with the dragged item, regardless of the target's own size.

```kotlin
DraggableItem(
    state = dragAndDropState,
    key = "item-1",
    data = "Hello",
    dropStrategy = DropStrategy.Surface,
) {
    // ...
}
```

### CenterDistance

Selects the drop target whose center is closest to the center of the dragged item. This strategy ignores overlap area entirely and focuses on proximity.

```kotlin
DraggableItem(
    state = dragAndDropState,
    key = "item-1",
    data = "Hello",
    dropStrategy = DropStrategy.CenterDistance,
) {
    // ...
}
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

## Usage with ReorderableItem

Drop strategies also work with `ReorderableItem`:

```kotlin
ReorderableItem(
    state = reorderState,
    key = item,
    data = item,
    dropStrategy = DropStrategy.CenterDistance,
    onDragEnter = { state ->
        // Reorder logic
    },
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
DraggableItem(
    state = dragAndDropState,
    key = "item-1",
    data = "Hello",
    dropStrategy = MyCustomStrategy,
) {
    // ...
}
```
