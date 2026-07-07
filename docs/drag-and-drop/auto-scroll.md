# Auto Scroll

When dragging items in a scrollable container, you often need the container to scroll automatically as the user drags near its edges. Compose DND provides the `dragAutoScroll` modifier for this purpose.

!!! warning "Experimental API"
    The auto scroll API is annotated with `@ExperimentalDndApi`. This means it may change in future releases. You must opt in with `@OptIn(ExperimentalDndApi::class)` to use it.

## LazyColumn / LazyRow

Use the `dragAutoScroll` modifier on a `LazyColumn` or `LazyRow` to enable automatic scrolling during drag operations:

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun AutoScrollLazyColumnExample() {
    val reorderState = rememberReorderState<String>()
    val lazyListState = rememberLazyListState()

    ReorderContainer(
        state = reorderState,
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = reorderState.dndState,
                    lazyListState = lazyListState,
                ),
        ) {
            // items...
        }
    }
}
```

## LazyGrid

For `LazyVerticalGrid` or `LazyHorizontalGrid`, use the grid variant:

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun AutoScrollLazyGridExample() {
    val reorderState = rememberReorderState<String>()
    val lazyGridState = rememberLazyGridState()

    ReorderContainer(
        state = reorderState,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = lazyGridState,
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = reorderState.dndState,
                    lazyGridState = lazyGridState,
                ),
        ) {
            // items...
        }
    }
}
```

## ScrollState (Column / Row)

For regular scrollable containers using `ScrollState`, pass the orientation parameter:

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun AutoScrollColumnExample() {
    val reorderState = rememberReorderState<String>()
    val scrollState = rememberScrollState()

    ReorderContainer(
        state = reorderState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .dragAutoScroll(
                    state = reorderState.dndState,
                    scrollState = scrollState,
                    orientation = Orientation.Vertical,
                ),
        ) {
            // items...
        }
    }
}
```

## DragAutoScrollConfig

You can customize the auto scroll behavior using `DragAutoScrollConfig`:

```kotlin
@OptIn(ExperimentalDndApi::class)
val config = DragAutoScrollConfig(
    minScrollThreshold = 48.dp,
    maxScrollThreshold = 160.dp,
    maxScrollSpeed = 1500f,
)

LazyColumn(
    state = lazyListState,
    modifier = Modifier
        .fillMaxSize()
        .dragAutoScroll(
            state = reorderState.dndState,
            lazyListState = lazyListState,
            config = config,
        ),
)
```

### Configuration Options

| Parameter            | Type   | Default  | Description                                                               |
|----------------------|--------|----------|---------------------------------------------------------------------------|
| `minScrollThreshold` | `Dp`   | `48.dp`  | Minimum distance from the edge where scrolling begins. The effective threshold adapts to the first/last visible item size: `clamp(itemSize, minScrollThreshold, maxScrollThreshold)`. |
| `maxScrollThreshold` | `Dp`   | `160.dp` | Maximum distance from the edge for auto-scroll activation. Prevents the adaptive threshold from growing too large with tall items or containers. |
| `maxScrollSpeed`     | `Float`| `1500f`  | The maximum scroll speed in pixels per second at the very edge.           |

## How It Works

The auto scroll mechanism uses a dynamic threshold approach:

1. **Detection** -- When a dragged item is within the scroll threshold distance from the container edge, scrolling begins.
2. **Quadratic Easing** -- Scroll speed increases quadratically as the dragged item moves closer to the edge. This provides a natural feel where small movements near the threshold produce slow scrolling, while pushing toward the edge accelerates.
3. **Frame-Synced** -- Scroll operations are synchronized with the frame rate for smooth, jank-free scrolling.
4. **Bidirectional** -- Scrolling works in both directions: toward the start and toward the end of the container.

## Scroll Pinning (dragScrollPin)

When reorderable items have **different sizes**, swapping two items changes the content size above the viewport. Compose's key-based scroll anchoring then adjusts the scroll position to compensate, which shows up as a visible jump during the drag.

The `dragScrollPin` modifier fixes this by pinning the scroll position right before each reorder swap, so the viewport stays stable:

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun ScrollPinExample() {
    val reorderState = rememberReorderState<String>()
    val lazyListState = rememberLazyListState()

    ReorderContainer(
        state = reorderState,
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .dragScrollPin(
                    state = reorderState.dndState,
                    lazyListState = lazyListState,
                ),
        ) {
            // items with varying heights...
        }
    }
}
```

A `lazyGridState` overload is available for `LazyVerticalGrid` / `LazyHorizontalGrid`.

!!! note
    `dragAutoScroll` already includes the scroll pinning behavior, so you do **not** need both. Use `dragScrollPin` on its own only when you want jump-free reordering without automatic edge scrolling.

## Full Working Example

```kotlin
@OptIn(ExperimentalDndApi::class)
@Composable
fun ReorderWithAutoScroll() {
    val reorderState = rememberReorderState<String>()
    var items by remember {
        mutableStateOf(
            (1..50).map { "Item $it" }
        )
    }
    val lazyListState = rememberLazyListState()

    ReorderContainer(
        state = reorderState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = reorderState.dndState,
                    lazyListState = lazyListState,
                ),
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
                    Card(
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                        ) {
                            Text(text = item)
                        }
                    }
                }
            }
        }
    }
}
```
