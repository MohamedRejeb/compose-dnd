/*
 * Copyright 2023, Mohamed Ben Rejeb and the Compose Dnd project contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mohamedrejeb.compose.dnd.scroll

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first

/**
 * Enables automatic scrolling of a [LazyListState] when a dragged item approaches
 * the edges of the scrollable container.
 *
 * Supports both [LazyColumn][androidx.compose.foundation.lazy.LazyColumn] and
 * [LazyRow][androidx.compose.foundation.lazy.LazyRow] — the scroll axis is
 * determined automatically from [LazyListState.layoutInfo].
 *
 * Also pins the scroll position while items swap, like [dragScrollPin].
 *
 * @param state The drag and drop state to observe.
 * @param lazyListState The lazy list state to scroll.
 * @param config Auto-scroll configuration (threshold and speed).
 *
 * Example:
 * ```
 * LazyColumn(
 *     state = lazyListState,
 *     modifier = Modifier
 *         .dragAutoScroll(
 *             state = dragAndDropState,
 *             lazyListState = lazyListState,
 *         )
 * )
 * ```
 */
@Composable
fun <T> Modifier.dragAutoScroll(
    state: DragAndDropState<T>,
    lazyListState: LazyListState,
    config: DragAutoScrollConfig = DragAutoScrollConfig(),
): Modifier =
    this
        .dragScrollPin(state = state, lazyListState = lazyListState)
        .lazyLayoutDragAutoScroll(
            state = state,
            scrollableState = lazyListState,
            config = config,
            orientation = { lazyListState.layoutInfo.orientation },
            reverseLayout = { lazyListState.layoutInfo.reverseLayout },
            edgeItemSizes = {
                val items = lazyListState.layoutInfo.visibleItemsInfo
                EdgeItemSizes(first = items.firstOrNull()?.size, last = items.lastOrNull()?.size)
            },
        )

/**
 * Enables automatic scrolling of a [LazyGridState] when a dragged item approaches
 * the edges of the scrollable container.
 *
 * Supports both [LazyVerticalGrid][androidx.compose.foundation.lazy.grid.LazyVerticalGrid] and
 * [LazyHorizontalGrid][androidx.compose.foundation.lazy.grid.LazyHorizontalGrid] — the scroll
 * axis is determined automatically from [LazyGridState.layoutInfo].
 *
 * Also pins the scroll position while items swap, like [dragScrollPin].
 *
 * @param state The drag and drop state to observe.
 * @param lazyGridState The lazy grid state to scroll.
 * @param config Auto-scroll configuration (threshold and speed).
 */
@Composable
fun <T> Modifier.dragAutoScroll(
    state: DragAndDropState<T>,
    lazyGridState: LazyGridState,
    config: DragAutoScrollConfig = DragAutoScrollConfig(),
): Modifier =
    this
        .dragScrollPin(state = state, lazyGridState = lazyGridState)
        .lazyLayoutDragAutoScroll(
            state = state,
            scrollableState = lazyGridState,
            config = config,
            orientation = { lazyGridState.layoutInfo.orientation },
            reverseLayout = { lazyGridState.layoutInfo.reverseLayout },
            edgeItemSizes = {
                val layoutInfo = lazyGridState.layoutInfo
                val items = layoutInfo.visibleItemsInfo
                EdgeItemSizes(
                    first = items.firstOrNull()?.size?.mainAxis(layoutInfo.orientation),
                    last = items.lastOrNull()?.size?.mainAxis(layoutInfo.orientation),
                )
            },
        )

/**
 * Enables automatic scrolling of a [LazyStaggeredGridState] when a dragged item approaches
 * the edges of the scrollable container.
 *
 * Supports both
 * [LazyVerticalStaggeredGrid][androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid]
 * and
 * [LazyHorizontalStaggeredGrid][androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid]
 * — the scroll axis is determined automatically from [LazyStaggeredGridState.layoutInfo].
 *
 * Also pins the scroll position while items swap, like [dragScrollPin].
 *
 * A staggered grid with `reverseLayout = true` is not supported yet.
 *
 * @param state The drag and drop state to observe.
 * @param lazyStaggeredGridState The lazy staggered grid state to scroll.
 * @param config Auto-scroll configuration (threshold and speed).
 */
@Composable
fun <T> Modifier.dragAutoScroll(
    state: DragAndDropState<T>,
    lazyStaggeredGridState: LazyStaggeredGridState,
    config: DragAutoScrollConfig = DragAutoScrollConfig(),
): Modifier =
    this
        .dragScrollPin(state = state, lazyStaggeredGridState = lazyStaggeredGridState)
        .lazyLayoutDragAutoScroll(
            state = state,
            scrollableState = lazyStaggeredGridState,
            config = config,
            orientation = { lazyStaggeredGridState.layoutInfo.orientation },
            // Its layout info doesn't expose reverseLayout yet
            reverseLayout = { false },
            edgeItemSizes = {
                val layoutInfo = lazyStaggeredGridState.layoutInfo
                val items = layoutInfo.visibleItemsInfo
                EdgeItemSizes(
                    first = items.firstOrNull()?.size?.mainAxis(layoutInfo.orientation),
                    last = items.lastOrNull()?.size?.mainAxis(layoutInfo.orientation),
                )
            },
        )

/**
 * Enables automatic scrolling of a [ScrollState] when a dragged item approaches
 * the edges of the scrollable container.
 *
 * Since [ScrollState] does not expose its orientation, you must provide it explicitly.
 * It doesn't expose `reverseScrolling` either, so a scroll modifier with
 * `reverseScrolling = true` is not supported: use a lazy layout with `reverseLayout` instead.
 *
 * @param state The drag and drop state to observe.
 * @param scrollState The scroll state to scroll.
 * @param orientation The scroll orientation ([Orientation.Vertical] or [Orientation.Horizontal]).
 * @param config Auto-scroll configuration (threshold and speed).
 */
@Composable
fun <T> Modifier.dragAutoScroll(
    state: DragAndDropState<T>,
    scrollState: ScrollState,
    orientation: Orientation,
    config: DragAutoScrollConfig = DragAutoScrollConfig(),
): Modifier {
    val density = LocalDensity.current
    val isReversed = isScrollReversed(orientation, reverseLayout = false, LocalLayoutDirection.current)
    val minThresholdPx = with(density) { config.minScrollThreshold.toPx() }
    val maxThresholdPx = with(density) { config.maxScrollThreshold.toPx() }
    val maxScrollSpeedPx = config.maxScrollSpeedPx(density)
    val intentDistancePx = with(density) { AutoScrollIntentDistance.toPx() }

    var containerTopLeft by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(
        state,
        scrollState,
        orientation,
        isReversed,
        minThresholdPx,
        maxThresholdPx,
        maxScrollSpeedPx,
        intentDistancePx,
    ) {
        val intent = AutoScrollIntent(intentDistancePx)

        // Unlike LazyList/Grid where containerTopLeft is stable during scroll,
        // ScrollState's onPlaced position shifts with every scroll when this modifier
        // is inside the scroll container. Using snapshotFlow + collectLatest would
        // cause re-emission on every scroll frame, cancelling the delay and making
        // scroll run at unlimited speed. Instead, while a drag is active we compute
        // the speed fresh each frame as long as it scrolls, and otherwise suspend
        // until the drag position, the container or the scroll bounds change.
        snapshotFlow { state.isActiveDrag }
            .collectLatest { isActive ->
                intent.reset()
                if (!isActive) return@collectLatest

                var lastFrameMs = withFrameMillis { it }

                while (state.isActiveDrag) {
                    val scrollSpeed = scrollStateSpeed(
                        state = state,
                        scrollState = scrollState,
                        orientation = orientation,
                        isReversed = isReversed,
                        intent = intent,
                        containerTopLeft = containerTopLeft,
                        containerSize = containerSize,
                        minThresholdPx = minThresholdPx,
                        maxThresholdPx = maxThresholdPx,
                        maxScrollSpeedPx = maxScrollSpeedPx,
                    )

                    if (scrollSpeed == 0f) {
                        // Idle until the drag, the container or the scroll bounds change
                        snapshotFlow {
                            listOf(
                                state.dragPosition.value,
                                containerTopLeft,
                                containerSize,
                                scrollState.canScrollBackward,
                                scrollState.canScrollForward,
                            )
                        }.drop(1).first()
                        lastFrameMs = withFrameMillis { it }
                        continue
                    }

                    val currentFrameMs = withFrameMillis { it }
                    val deltaMs = currentFrameMs - lastFrameMs
                    lastFrameMs = currentFrameMs

                    scrollState.scrollBy(scrollSpeed * deltaMs / 1000f)
                    state.reevaluateDropTargets()
                }
            }
    }

    return this.onPlaced { coordinates ->
        containerTopLeft = coordinates.positionInRoot()
        containerSize = coordinates.size.toSize()
    }
}

// -- Internal helpers --

/**
 * Scroll speed for a [ScrollState] container, or 0 when the dragged item is
 * outside the container's edge zones or there is nothing left to scroll.
 */
private fun <T> scrollStateSpeed(
    state: DragAndDropState<T>,
    scrollState: ScrollState,
    orientation: Orientation,
    isReversed: Boolean,
    intent: AutoScrollIntent,
    containerTopLeft: Offset,
    containerSize: Size,
    minThresholdPx: Float,
    maxThresholdPx: Float,
    maxScrollSpeedPx: Float,
): Float {
    val dragPosition = state.dragPosition.value
    val itemSize = state.draggedItemVisibleSize ?: return 0f

    if (!hasCrossAxisOverlap(orientation, dragPosition, itemSize, containerTopLeft, containerSize)) {
        return 0f
    }

    // Unlike lazy layouts, this modifier may sit inside the scrolled content,
    // whose reported bounds then span the whole content: derive the viewport
    val viewportPx = scrollState.viewportSize.toFloat()
    val isVertical = orientation == Orientation.Vertical
    val reportedAxisSize = if (isVertical) containerSize.height else containerSize.width
    val scrollCompensation =
        when {
            reportedAxisSize <= viewportPx + 1f -> 0f

            // Mirrored content starts scrolled to its far end
            isReversed -> (scrollState.maxValue - scrollState.value).toFloat()

            else -> scrollState.value.toFloat()
        }

    val viewportTopLeft = if (isVertical) {
        Offset(containerTopLeft.x, containerTopLeft.y + scrollCompensation)
    } else {
        Offset(containerTopLeft.x + scrollCompensation, containerTopLeft.y)
    }
    val viewportSize = if (isVertical) {
        Size(containerSize.width, viewportPx)
    } else {
        Size(viewportPx, containerSize.height)
    }

    // ScrollState has no visible items: use 20% of the viewport
    val zonePx = edgeZoneSize(
        preferredPx = viewportPx * 0.2f,
        minPx = minThresholdPx,
        maxPx = maxThresholdPx,
        viewportPx = viewportPx,
    )

    return edgeScrollSpeed(
        state = state,
        orientation = orientation,
        isReversed = isReversed,
        intent = intent,
        dragPosition = dragPosition,
        itemSize = itemSize,
        containerTopLeft = viewportTopLeft,
        containerSize = viewportSize,
        startZonePx = zonePx,
        endZonePx = zonePx,
        maxScrollSpeedPx = maxScrollSpeedPx,
        canScrollBackward = scrollState.canScrollBackward,
        canScrollForward = scrollState.canScrollForward,
    )
}

/**
 * Speed to scroll the content at, in px per second: positive scrolls forward.
 * Resolves the drag along the scroll axis and maps the edge the item heads to
 * onto the scroll direction, which is flipped in reversed and RTL layouts.
 */
private fun <T> edgeScrollSpeed(
    state: DragAndDropState<T>,
    orientation: Orientation,
    isReversed: Boolean,
    intent: AutoScrollIntent,
    dragPosition: Offset,
    itemSize: Size,
    containerTopLeft: Offset,
    containerSize: Size,
    startZonePx: Float,
    endZonePx: Float,
    maxScrollSpeedPx: Float,
    canScrollBackward: Boolean,
    canScrollForward: Boolean,
): Float {
    val (itemStart, itemEnd, containerStart, containerEnd) = resolveScrollAxis(
        orientation = orientation,
        dragPosition = dragPosition,
        itemSize = itemSize,
        containerTopLeft = containerTopLeft,
        containerSize = containerSize,
    )
    val isVertical = orientation == Orientation.Vertical
    val pointer = state.dragPointerPosition
    val dragAmount = state.draggedItem?.dragAmount ?: Offset.Zero

    val edgeSpeed = computeEdgeScrollSpeed(
        itemStart = itemStart,
        itemEnd = itemEnd,
        pointer = pointer?.let { if (isVertical) it.y else it.x } ?: Float.NaN,
        containerStart = containerStart,
        containerEnd = containerEnd,
        startZonePx = startZonePx,
        endZonePx = endZonePx,
        maxScrollSpeed = maxScrollSpeedPx,
        intent = intent,
        movedTowardEnd = if (isVertical) dragAmount.y else dragAmount.x,
        // The first item sits at the bottom or right edge of a reversed or RTL layout
        canScrollTowardStart = if (isReversed) canScrollForward else canScrollBackward,
        canScrollTowardEnd = if (isReversed) canScrollBackward else canScrollForward,
    )

    return if (isReversed) -edgeSpeed else edgeSpeed
}

/**
 * True when scrolling forward moves the content toward the bottom or right edge:
 * a reversed layout, or a horizontal one in a right-to-left locale.
 */
private fun isScrollReversed(
    orientation: Orientation,
    reverseLayout: Boolean,
    layoutDirection: LayoutDirection,
): Boolean {
    val isMirrored = orientation == Orientation.Horizontal && layoutDirection == LayoutDirection.Rtl
    return reverseLayout != isMirrored
}

/** How far the pointer must head toward an edge to scroll an item picked up next to it. */
private val AutoScrollIntentDistance = 8.dp

/** Main-axis sizes of the first and last visible items of a lazy layout, in px. */
private class EdgeItemSizes(
    val first: Int?,
    val last: Int?,
)

private fun IntSize.mainAxis(orientation: Orientation): Int =
    if (orientation == Orientation.Vertical) height else width

/**
 * Shared auto-scroll for lazy layouts: the container keeps its position while
 * it scrolls, so the scroll speed can be derived from a snapshot of the drag
 * position and re-emitted only when it changes.
 */
@Composable
private fun <T> Modifier.lazyLayoutDragAutoScroll(
    state: DragAndDropState<T>,
    scrollableState: ScrollableState,
    config: DragAutoScrollConfig,
    orientation: () -> Orientation,
    reverseLayout: () -> Boolean,
    edgeItemSizes: () -> EdgeItemSizes,
): Modifier {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val minThresholdPx = with(density) { config.minScrollThreshold.toPx() }
    val maxThresholdPx = with(density) { config.maxScrollThreshold.toPx() }
    val maxScrollSpeedPx = config.maxScrollSpeedPx(density)
    val intentDistancePx = with(density) { AutoScrollIntentDistance.toPx() }
    val currentOrientation by rememberUpdatedState(orientation)
    val currentReverseLayout by rememberUpdatedState(reverseLayout)
    val currentEdgeItemSizes by rememberUpdatedState(edgeItemSizes)

    var containerTopLeft by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(
        state,
        scrollableState,
        layoutDirection,
        minThresholdPx,
        maxThresholdPx,
        maxScrollSpeedPx,
        intentDistancePx,
    ) {
        val intent = AutoScrollIntent(intentDistancePx)

        // Speed to scroll at right now, in px per second; zero when there is nothing to scroll
        val currentScrollSpeed = speed@{
            if (!state.isActiveDrag) {
                intent.reset()
                return@speed 0f
            }
            val dragPosition = state.dragPosition.value
            val itemSize = state.draggedItemVisibleSize ?: return@speed 0f
            val axis = currentOrientation()

            // Don't scroll if the item isn't within this container on the cross axis
            if (!hasCrossAxisOverlap(axis, dragPosition, itemSize, containerTopLeft, containerSize)) {
                return@speed 0f
            }

            // The first item sits at the bottom or right edge of a reversed or RTL layout
            val isReversed = isScrollReversed(axis, currentReverseLayout(), layoutDirection)

            // Each edge zone is as large as the item next to it, within the configured bounds
            val viewportPx = if (axis == Orientation.Vertical) containerSize.height else containerSize.width
            val edgeSizes = currentEdgeItemSizes()
            val startItemSize = if (isReversed) edgeSizes.last else edgeSizes.first
            val endItemSize = if (isReversed) edgeSizes.first else edgeSizes.last

            edgeScrollSpeed(
                state = state,
                orientation = axis,
                isReversed = isReversed,
                intent = intent,
                dragPosition = dragPosition,
                itemSize = itemSize,
                containerTopLeft = containerTopLeft,
                containerSize = containerSize,
                startZonePx = edgeZoneSize(
                    preferredPx = startItemSize?.toFloat() ?: minThresholdPx,
                    minPx = minThresholdPx,
                    maxPx = maxThresholdPx,
                    viewportPx = viewportPx,
                ),
                endZonePx = edgeZoneSize(
                    preferredPx = endItemSize?.toFloat() ?: minThresholdPx,
                    minPx = minThresholdPx,
                    maxPx = maxThresholdPx,
                    viewportPx = viewportPx,
                ),
                maxScrollSpeedPx = maxScrollSpeedPx,
                canScrollBackward = scrollableState.canScrollBackward,
                canScrollForward = scrollableState.canScrollForward,
            )
        }

        // Keyed on whether to scroll at all, not on the speed: the speed changes with every
        // pointer move, and restarting the frame loop each time would break its pacing
        snapshotFlow { currentScrollSpeed() != 0f }
            .collectLatest { shouldScroll ->
                if (!shouldScroll) return@collectLatest

                var lastFrameMs = withFrameMillis { it }
                while (true) {
                    val currentFrameMs = withFrameMillis { it }
                    val deltaMs = currentFrameMs - lastFrameMs
                    lastFrameMs = currentFrameMs

                    val scrollSpeed = currentScrollSpeed()
                    if (scrollSpeed == 0f) continue

                    scrollableState.scrollBy(scrollSpeed * deltaMs / 1000f)
                    state.reevaluateDropTargets()
                }
            }
    }

    return this.onPlaced { coordinates ->
        containerTopLeft = coordinates.positionInRoot()
        containerSize = coordinates.size.toSize()
    }
}

/**
 * Resolves the dragged item and container positions along the scroll axis.
 */
private data class ScrollAxisBounds(
    val itemStart: Float,
    val itemEnd: Float,
    val containerStart: Float,
    val containerEnd: Float,
)

/**
 * Returns true if the dragged item overlaps with the container on the cross axis.
 * For vertical scroll, checks horizontal overlap. For horizontal scroll, checks vertical overlap.
 * This prevents auto-scroll from triggering in sibling containers (e.g., other Kanban columns).
 */
private fun hasCrossAxisOverlap(
    orientation: Orientation,
    dragPosition: Offset,
    itemSize: Size,
    containerTopLeft: Offset,
    containerSize: Size,
): Boolean = if (orientation == Orientation.Vertical) {
    // Vertical scroll — check horizontal overlap
    val itemLeft = dragPosition.x
    val itemRight = dragPosition.x + itemSize.width
    val containerLeft = containerTopLeft.x
    val containerRight = containerTopLeft.x + containerSize.width
    itemRight > containerLeft && itemLeft < containerRight
} else {
    // Horizontal scroll — check vertical overlap
    val itemTop = dragPosition.y
    val itemBottom = dragPosition.y + itemSize.height
    val containerTop = containerTopLeft.y
    val containerBottom = containerTopLeft.y + containerSize.height
    itemBottom > containerTop && itemTop < containerBottom
}

private fun resolveScrollAxis(
    orientation: Orientation,
    dragPosition: Offset,
    itemSize: Size,
    containerTopLeft: Offset,
    containerSize: Size,
): ScrollAxisBounds = if (orientation == Orientation.Vertical) {
        ScrollAxisBounds(
            itemStart = dragPosition.y,
            itemEnd = dragPosition.y + itemSize.height,
            containerStart = containerTopLeft.y,
            containerEnd = containerTopLeft.y + containerSize.height,
        )
    } else {
        ScrollAxisBounds(
            itemStart = dragPosition.x,
            itemEnd = dragPosition.x + itemSize.width,
            containerStart = containerTopLeft.x,
            containerEnd = containerTopLeft.x + containerSize.width,
        )
    }
