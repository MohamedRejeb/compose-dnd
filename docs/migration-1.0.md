# Migrating to 1.0

`1.0.0-alpha01` unifies the API around a single `DragAndDropState` and removes the last experimental opt-ins. Nothing was removed outright -- the old reorder wrapper API still compiles, marked `@Deprecated`, so you can migrate at your own pace module by module.

!!! tip
    Most deprecated symbols carry a `replaceWith`, so much of this migration is "apply the quick fix" in IntelliJ / Android Studio (Alt+Enter -> Replace with...) rather than hand-editing.

## ReorderState to DragAndDropState

`ReorderState`, `rememberReorderState`, `ReorderContainer`, and the `ReorderableItem(state: ReorderState, ...)` overload are deprecated. Reorder with the same `DragAndDropState` and `DragAndDropContainer` used for plain drag and drop -- there is now only one state type in the whole library.

**Before:**

```kotlin
val reorderState = rememberReorderState<String>()

ReorderContainer(
    state = reorderState,
) {
    LazyColumn {
        items(items, key = { it }) { item ->
            ReorderableItem(
                state = reorderState,
                key = item,
                data = item,
                onDragEnter = { state ->
                    items = items.toMutableList().apply {
                        val index = indexOf(item)
                        if (index == -1) return@ReorderableItem
                        remove(state.data)
                        add(index, state.data)
                    }
                },
            ) {
                Text(item)
            }
        }
    }
}
```

**After:**

```kotlin
val dndState = rememberDragAndDropState<String>()

DragAndDropContainer(
    state = dndState,
) {
    LazyColumn {
        items(items, key = { it }) { item ->
            ReorderableItem(
                state = dndState,
                key = item,
                data = item,
                onDragEnter = { state ->
                    items = items.toMutableList().apply {
                        val index = indexOf(item)
                        if (index == -1) return@ReorderableItem
                        remove(state.data)
                        add(index, state.data)
                    }
                },
            ) {
                Text(item)
            }
        }
    }
}
```

If you were using the `reorderableItem` modifier rather than the `ReorderableItem` composable, nothing changes -- it already takes a `DragAndDropState` and was never affected by this deprecation.

If your code read `reorderState.dndState` to pass the underlying state to a modifier like `dragAutoScroll`, just pass `dndState` directly now -- there is no wrapper to unwrap.

## isDragging: Drop the Import

`DragAndDropState.isDragging(key)` is now a member function. The old top-level extension in `com.mohamedrejeb.compose.dnd.drag` is deprecated; a member function of the same name and signature shadows it, so existing calls keep compiling and resolve to the member automatically. The only cleanup needed is removing the now-unused import.

**Before:**

```kotlin
import com.mohamedrejeb.compose.dnd.drag.isDragging

val isDragging = dragAndDropState.isDragging("item-1")
```

**After:**

```kotlin
// no import needed

val isDragging = dragAndDropState.isDragging("item-1")
```

While you're there, consider `isDropTargetHovered(key)`, the new equivalent of `hoveredDropTargetKey == key`:

```kotlin
// Before
val isHovered = dragAndDropState.hoveredDropTargetKey == "target-1"

// After
val isHovered = dragAndDropState.isDropTargetHovered("target-1")
```

## ExperimentalDndApi: Opt-Ins Are Gone

`dragAutoScroll` and `dragScrollPin` are no longer annotated `@ExperimentalDndApi`, and nothing in the public API requires opt-in anymore. (Axis lock and `canDrop` never required it, even though older docs said so.) Remove the annotation and the import if nothing else in your file still needs it.

**Before:**

```kotlin
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi

@OptIn(ExperimentalDndApi::class)
@Composable
fun ReorderableGrid() {
    val dndState = rememberDragAndDropState<String>()
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        modifier = Modifier.dragAutoScroll(
            state = dndState,
            lazyGridState = lazyGridState,
        ),
    ) {
        // ...
    }
}
```

**After:**

```kotlin
@Composable
fun ReorderableGrid() {
    val dndState = rememberDragAndDropState<String>()
    val lazyGridState = rememberLazyGridState()

    LazyVerticalGrid(
        modifier = Modifier.dragAutoScroll(
            state = dndState,
            lazyGridState = lazyGridState,
        ),
    ) {
        // ...
    }
}
```

`ExperimentalDndApi` itself still exists in the library for now, in case a future API needs it, but nothing currently uses it.

## DragAutoScrollConfig: maxScrollSpeed Is Now a Dp

`maxScrollSpeed` used to be a `Float` in pixels per second, which meant the same config scrolled at different physical speeds on different screen densities. It is now a `Dp`, so it feels the same everywhere.

**Before:**

```kotlin
val config = DragAutoScrollConfig(
    minScrollThreshold = 48.dp,
    maxScrollThreshold = 160.dp,
    maxScrollSpeed = 1500f, // px/s
)
```

**After:**

```kotlin
val config = DragAutoScrollConfig(
    minScrollThreshold = 48.dp,
    maxScrollThreshold = 160.dp,
    maxScrollSpeed = 600.dp, // dp/s -- the new default
)
```

If you can't convert your tuned pixel value to `Dp` right away, a deprecated factory overload accepting `maxScrollSpeed: Float` is kept for migration -- it behaves exactly as before, so it's a safe stopgap while you pick a good `Dp` value.

## DragAutoScrollConfig Is No Longer a data class

`DragAutoScrollConfig` is a regular class now (its internals need a bit more than a data class' generated methods can offer), but it still exposes a `copy()` function with the same parameter names (`maxScrollSpeed` is a `Dp` there too). The only thing that breaks is destructuring with `componentN()`, such as `val (a, b, c) = config` -- use named properties or `copy()` instead.

**Before:**

```kotlin
val (minThreshold, maxThreshold, speed) = config
val faster = config.copy(maxScrollSpeed = speed * 1.5f)
```

**After:**

```kotlin
val minThreshold = config.minScrollThreshold
val maxThreshold = config.maxScrollThreshold
val speed = config.maxScrollSpeed

val faster = config.copy(maxScrollSpeed = config.maxScrollSpeed * 1.5f)
```

`copy()` keeps its shape, so code that builds a config from a base one is unaffected unless it passes `maxScrollSpeed` as a `Float`.

## DraggableItem and ReorderableItem: Parameter Order

The `DraggableItem` and `ReorderableItem` composables now start with `key, data, state, modifier`, the same order as the `draggableItem` and `reorderableItem` modifiers. `ReorderableItem` also takes `zIndex` after `dragAxis`.

Calls using named arguments compile unchanged. Only calls passing these arguments positionally need to change:

=== "Before"

    ```kotlin
    ReorderableItem(Modifier.fillMaxWidth(), dndState, item.id, item) {
        ItemRow(item)
    }
    ```

=== "After"

    ```kotlin
    ReorderableItem(
        key = item.id,
        data = item,
        state = dndState,
        modifier = Modifier.fillMaxWidth(),
    ) {
        ItemRow(item)
    }
    ```

## DropStrategy: SurfacePercentage and the Default

`DropStrategy.SurfacePercentage` used to divide the overlap by the dragged item's area. That value is the same for every target, so it always picked the same target as `DropStrategy.Surface`. It now divides by each drop target's own area: the target covered the most, as a share of its size, wins.

The default `dropStrategy` changed from `SurfacePercentage` to `Surface`, so items relying on the default hover exactly as before.

You only need to act if you pass `dropStrategy = DropStrategy.SurfacePercentage` explicitly **and** your drop targets have different sizes: switch to `DropStrategy.Surface` to keep the old behavior.

## What's New (Not a Breaking Change)

These are additive and don't require any changes to existing code:

- `DragAndDropState.cancelDrag()` to abort a drag programmatically -- see [Overview: Cancelling a Drag Programmatically](drag-and-drop/overview.md#cancelling-a-drag-programmatically).
- `onDragStart` / `onDragEnd` callbacks on every draggable item -- see [Overview: Drag Lifecycle](drag-and-drop/overview.md#drag-lifecycle-ondragstart-and-ondragend).
- `canDrop` on `reorderableItem` / `ReorderableItem`, for pinned or locked rows -- see [Reorder List: Pinned or Locked Items](drag-and-drop/reorder.md#pinned-or-locked-items-candrop).
- `dragAutoScroll` / `dragScrollPin` overloads for `LazyStaggeredGridState` -- see [Auto Scroll](drag-and-drop/auto-scroll.md).
- New [Drag to Select and Multi-Select](drag-and-drop/multi-select.md) and [Free-Form Placement](drag-and-drop/free-form.md) guides.

See the [CHANGELOG](https://github.com/MohamedRejeb/compose-dnd/blob/main/CHANGELOG.md) for the full list of changes in `1.0.0-alpha01`.
