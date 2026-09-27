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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import kotlinx.coroutines.flow.collectLatest

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
    val minThresholdPx = with(density) { config.minScrollThreshold.toPx() }
    val maxThresholdPx = with(density) { config.maxScrollThreshold.toPx() }
    val maxScrollSpeedPx = config.maxScrollSpeedPx(density)

    var containerTopLeft by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(
        state,
        scrollState,
        orientation,
        minThresholdPx,
        maxThresholdPx,
        maxScrollSpeedPx,
    ) {
        // Unlike LazyList/Grid where containerTopLeft is stable during scroll,
        // ScrollState's onPlaced position shifts with every scroll when this modifier
        // is inside the scroll container. Using snapshotFlow + collectLatest would
        // cause re-emission on every scroll frame, cancelling the delay and making
        // scroll run at unlimited speed. Instead, we poll: wait for drag to start,
        // then compute speed fresh each frame inside the loop.
        snapshotFlow { state.isActiveDrag }
            .collectLatest { isActive ->
                if (!isActive) return@collectLatest

                var lastFrameMs = withFrameMillis { it }

                while (state.isActiveDrag) {
                    val currentFrameMs = withFrameMillis { it }
                    val deltaMs = currentFrameMs - lastFrameMs
                    lastFrameMs = currentFrameMs

                    val dragPosition = state.dragPosition.value
                    val itemSize = state.draggedItemVisibleSize
                        ?: continue

                    if (!hasCrossAxisOverlap(orientation, dragPosition, itemSize, containerTopLeft, containerSize)) {
                        continue
                    }

                    val viewportPx = scrollState.viewportSize.toFloat()
                    val isVertical = orientation == Orientation.Vertical
                    val reportedAxisSize =
                        if (isVertical) containerSize.height else containerSize.width
                    val scrollCompensation =
                        if (reportedAxisSize > viewportPx + 1f) scrollState.value.toFloat() else 0f

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

                    val (itemStart, itemEnd, containerStart, containerEnd) = resolveScrollAxis(
                        orientation = orientation,
                        dragPosition = dragPosition,
                        itemSize = itemSize,
                        containerTopLeft = viewportTopLeft,
                        containerSize = viewportSize,
                    )

                    // ScrollState has no visible items — use 20% of viewport,
                    // clamped between min and max threshold
                    val thresholdPx = (viewportPx * 0.2f)
                        .coerceIn(minThresholdPx, maxThresholdPx)

                    val scrollSpeed = computeScrollSpeed(
                        itemStart = itemStart,
                        itemEnd = itemEnd,
                        containerStart = containerStart,
                        containerEnd = containerEnd,
                        startThresholdPx = thresholdPx,
                        endThresholdPx = thresholdPx,
                        maxScrollSpeed = maxScrollSpeedPx,
                    )

                    if (scrollSpeed != 0f) {
                        val scrollAmount = scrollSpeed * deltaMs / 1000f
                        scrollState.scrollBy(scrollAmount)
                        state.reevaluateDropTargets()
                    }
                }
            }
    }

    return this.onPlaced { coordinates ->
        containerTopLeft = coordinates.positionInRoot()
        containerSize = coordinates.size.toSize()
    }
}

// -- Internal helpers --

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
    edgeItemSizes: () -> EdgeItemSizes,
): Modifier {
    val density = LocalDensity.current
    val minThresholdPx = with(density) { config.minScrollThreshold.toPx() }
    val maxThresholdPx = with(density) { config.maxScrollThreshold.toPx() }
    val maxScrollSpeedPx = config.maxScrollSpeedPx(density)
    val currentOrientation by rememberUpdatedState(orientation)
    val currentEdgeItemSizes by rememberUpdatedState(edgeItemSizes)

    var containerTopLeft by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(
        state,
        scrollableState,
        minThresholdPx,
        maxThresholdPx,
        maxScrollSpeedPx,
    ) {
        snapshotFlow {
            if (!state.isActiveDrag) return@snapshotFlow null
            val dragPosition = state.dragPosition.value
            val itemSize = state.draggedItemVisibleSize ?: return@snapshotFlow null
            val axis = currentOrientation()

            // Don't scroll if the item isn't within this container on the cross axis
            if (!hasCrossAxisOverlap(axis, dragPosition, itemSize, containerTopLeft, containerSize)) {
                return@snapshotFlow null
            }

            val (itemStart, itemEnd, containerStart, containerEnd) = resolveScrollAxis(
                orientation = axis,
                dragPosition = dragPosition,
                itemSize = itemSize,
                containerTopLeft = containerTopLeft,
                containerSize = containerSize,
            )

            // Dynamic threshold: clamp(itemSize, min, max)
            val edgeSizes = currentEdgeItemSizes()
            val startThresholdPx = (edgeSizes.first?.toFloat() ?: minThresholdPx)
                .coerceIn(minThresholdPx, maxThresholdPx)
            val endThresholdPx = (edgeSizes.last?.toFloat() ?: minThresholdPx)
                .coerceIn(minThresholdPx, maxThresholdPx)

            computeScrollSpeed(
                itemStart = itemStart,
                itemEnd = itemEnd,
                containerStart = containerStart,
                containerEnd = containerEnd,
                startThresholdPx = startThresholdPx,
                endThresholdPx = endThresholdPx,
                maxScrollSpeed = maxScrollSpeedPx,
            )
        }.collectLatest { scrollSpeed ->
            if (scrollSpeed == null || scrollSpeed == 0f) return@collectLatest

            var lastFrameMs = withFrameMillis { it }
            while (true) {
                val currentFrameMs = withFrameMillis { it }
                val deltaMs = currentFrameMs - lastFrameMs
                lastFrameMs = currentFrameMs
                val scrollAmount = scrollSpeed * deltaMs / 1000f
                scrollableState.scrollBy(scrollAmount)
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

/**
 * Computes the scroll speed based on how close the dragged item is to the container edge.
 *
 * Uses a quadratic ease-in curve (ratio^2) so scrolling starts gently and accelerates
 * as the item gets closer to the edge.
 *
 * Returns:
 * - Negative value: scroll toward start (up / left)
 * - Positive value: scroll toward end (down / right)
 * - Zero: no scroll needed
 */
internal fun computeScrollSpeed(
    itemStart: Float,
    itemEnd: Float,
    containerStart: Float,
    containerEnd: Float,
    startThresholdPx: Float,
    endThresholdPx: Float,
    maxScrollSpeed: Float,
): Float {
    // How far into the start threshold zone the item is
    val startOverlap = (containerStart + startThresholdPx) - itemStart
    // How far into the end threshold zone the item is
    val endOverlap = itemEnd - (containerEnd - endThresholdPx)

    return when {
        // Item's start edge is within the start threshold → scroll toward start
        startOverlap > 0f -> {
            val ratio = (startOverlap / startThresholdPx).coerceIn(0f, 1f)
            -maxScrollSpeed * ratio * ratio
        }

        // Item's end edge is within the end threshold → scroll toward end
        endOverlap > 0f -> {
            val ratio = (endOverlap / endThresholdPx).coerceIn(0f, 1f)
            maxScrollSpeed * ratio * ratio
        }

        else -> {
            0f
        }
    }
}
