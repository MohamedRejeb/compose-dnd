# Auto Scroll

When dragging items in a scrollable container, you often need the container to scroll automatically as the user drags near its edges. Compose DND provides the `dragAutoScroll` modifier for this purpose.

<video class="demo-phone" src="../../videos/auto-scroll.mp4" autoplay loop muted playsinline></video>

## LazyColumn / LazyRow

Use the `dragAutoScroll` modifier on a `LazyColumn` or `LazyRow` to enable automatic scrolling during drag operations:

```kotlin
@Composable
fun AutoScrollLazyColumnExample() {
    val dndState = rememberDragAndDropState<String>()
    val lazyListState = rememberLazyListState()

    DragAndDropContainer(
        state = dndState,
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = dndState,
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
@Composable
fun AutoScrollLazyGridExample() {
    val dndState = rememberDragAndDropState<String>()
    val lazyGridState = rememberLazyGridState()

    DragAndDropContainer(
        state = dndState,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = lazyGridState,
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = dndState,
                    lazyGridState = lazyGridState,
                ),
        ) {
            // items...
        }
    }
}
```

## LazyVerticalStaggeredGrid / LazyHorizontalStaggeredGrid

For a masonry-style staggered grid, use the `lazyStaggeredGridState` overload:

```kotlin
@Composable
fun AutoScrollStaggeredGridExample() {
    val dndState = rememberDragAndDropState<String>()
    val staggeredGridState = rememberLazyStaggeredGridState()

    DragAndDropContainer(
        state = dndState,
    ) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = staggeredGridState,
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = dndState,
                    lazyStaggeredGridState = staggeredGridState,
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
@Composable
fun AutoScrollColumnExample() {
    val dndState = rememberDragAndDropState<String>()
    val scrollState = rememberScrollState()

    DragAndDropContainer(
        state = dndState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .dragAutoScroll(
                    state = dndState,
                    scrollState = scrollState,
                    orientation = Orientation.Vertical,
                ),
        ) {
            // items...
        }
    }
}
```

## Reversed and Right-to-Left Layouts

Auto scroll follows the layout: a lazy list or grid with `reverseLayout = true`, and any horizontal layout in a right-to-left locale, scrolls toward the edge the dragged item is held at.

Two cases are not supported yet: a staggered grid with `reverseLayout = true`, and a `ScrollState` used with `reverseScrolling = true`.

## DragAutoScrollConfig

You can customize the auto scroll behavior using `DragAutoScrollConfig`:

```kotlin
val config = DragAutoScrollConfig(
    minScrollThreshold = 48.dp,
    maxScrollThreshold = 160.dp,
    maxScrollSpeed = 600.dp,
)

LazyColumn(
    state = lazyListState,
    modifier = Modifier
        .fillMaxSize()
        .dragAutoScroll(
            state = dndState,
            lazyListState = lazyListState,
            config = config,
        ),
)
```

### Configuration Options

| Parameter            | Type | Default  | Description                                                               |
|----------------------|------|----------|---------------------------------------------------------------------------|
| `minScrollThreshold` | `Dp` | `DragAutoScrollConfig.DefaultMinScrollThreshold` (`48.dp`)  | Minimum distance from the edge where scrolling begins. The effective threshold adapts to the first/last visible item size: `clamp(itemSize, minScrollThreshold, maxScrollThreshold)`, and never more than a quarter of the viewport. |
| `maxScrollThreshold` | `Dp` | `DragAutoScrollConfig.DefaultMaxScrollThreshold` (`160.dp`) | Maximum distance from the edge for auto-scroll activation. Prevents the adaptive threshold from growing too large with tall items or containers. |
| `maxScrollSpeed`     | `Dp` | `DragAutoScrollConfig.DefaultMaxScrollSpeed` (`600.dp`)     | The maximum scroll speed, as a distance scrolled per second at the very edge. Being a `Dp`, the scroll feels the same on every screen density. |

`DragAutoScrollConfig` is a regular class, not a `data class`, but it still exposes `copy()`:

```kotlin
val fasterConfig = DragAutoScrollConfig().copy(maxScrollSpeed = 900.dp)
```

!!! warning "maxScrollSpeed used to be a Float"
    Before `1.0.0-alpha01`, `maxScrollSpeed` was a `Float` in pixels per second, which scrolled at different physical speeds on different screen densities. It is now a `Dp`. A deprecated factory taking `maxScrollSpeed: Float` is kept for migration -- see the [migration guide](../migration-1.0.md#dragautoscrollconfig-maxscrollspeed-is-now-a-dp).

## How It Works

The auto scroll mechanism uses a dynamic threshold approach:

1. **Detection** -- When an edge of the dragged item is within the scroll threshold distance from the container edge, scrolling begins.
2. **Large items follow the pointer** -- An item too large to fit between the two edge zones (a Kanban column on a phone) would touch both all the time. Its edges are followed no further from the pointer than half the free space, so for large items the pointer decides when to scroll.
3. **No scroll on pick-up** -- An item picked up next to an edge doesn't scroll the list under your finger. It starts scrolling once the pointer moves toward that edge by `8.dp`, or once the item leaves the edge zone and comes back.
4. **Quadratic Easing** -- Scroll speed increases quadratically as the dragged item moves closer to the edge. This provides a natural feel where small movements near the threshold produce slow scrolling, while pushing toward the edge accelerates.
5. **Frame-Synced** -- Scroll operations are synchronized with the frame rate for smooth, jank-free scrolling.
6. **Bidirectional** -- Scrolling works in both directions: toward the start and toward the end of the container.
5. **Bounded** -- Auto-scroll stays idle once the container can't scroll further in that direction, instead of spinning frames against the end of the list.

## Scroll Pinning (dragScrollPin)

When reorderable items have **different sizes**, swapping two items changes the content size above the viewport. Compose's key-based scroll anchoring then adjusts the scroll position to compensate, which shows up as a visible jump during the drag.

The `dragScrollPin` modifier fixes this by pinning the scroll position right before each reorder swap, so the viewport stays stable:

```kotlin
@Composable
fun ScrollPinExample() {
    val dndState = rememberDragAndDropState<String>()
    val lazyListState = rememberLazyListState()

    DragAndDropContainer(
        state = dndState,
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .dragScrollPin(
                    state = dndState,
                    lazyListState = lazyListState,
                ),
        ) {
            // items with varying heights...
        }
    }
}
```

`lazyGridState` and `lazyStaggeredGridState` overloads are available for `LazyVerticalGrid` / `LazyHorizontalGrid` and `LazyVerticalStaggeredGrid` / `LazyHorizontalStaggeredGrid`.

!!! note
    `dragAutoScroll` already includes the scroll pinning behavior, so you do **not** need both. Use `dragScrollPin` on its own only when you want jump-free reordering without automatic edge scrolling.

## Full Working Example

```kotlin
@Composable
fun ReorderWithAutoScroll() {
    val dndState = rememberDragAndDropState<String>()
    var items by remember {
        mutableStateOf(
            (1..50).map { "Item $it" }
        )
    }
    val lazyListState = rememberLazyListState()

    DragAndDropContainer(
        state = dndState,
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        LazyColumn(
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = dndState,
                    lazyListState = lazyListState,
                ),
        ) {
            items(items, key = { it }) { item ->
                val isDragging = dndState.isDragging(item)

                ItemCard(
                    text = item,
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = if (isDragging) 0f else 1f
                        }
                        .reorderableItem(
                            key = item,
                            data = item,
                            state = dndState,
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
                                ItemCard(text = item)
                            },
                        )
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun ItemCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.height(56.dp),
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Text(text = text)
        }
    }
}
```
