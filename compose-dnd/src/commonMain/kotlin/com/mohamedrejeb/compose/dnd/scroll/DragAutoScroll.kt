/*
 * Copyright 2025, Mohamed Ben Rejeb and the Compose Dnd project contributors
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
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * Configuration for auto-scroll behavior during drag.
 *
 * @param minScrollThreshold Minimum distance from the edge of the scrollable container
 *   within which auto-scroll activates. The actual threshold is the larger of this value
 *   and the size of the first/last visible item (when available).
 * @param maxScrollSpeed Maximum scroll speed in pixels per second when the
 *   dragged item is at the very edge.
 */
data class DragAutoScrollConfig(
    val minScrollThreshold: Dp = 48.dp,
    val maxScrollSpeed: Float = 1500f,
)

/**
 * Enables automatic scrolling of a [LazyListState] when a dragged item approaches
 * the edges of the scrollable container.
 *
 * Supports both [LazyColumn][androidx.compose.foundation.lazy.LazyColumn] and
 * [LazyRow][androidx.compose.foundation.lazy.LazyRow] — the scroll axis is
 * determined automatically from [LazyListState.layoutInfo].
 *
 * @param state The drag and drop state to observe.
 * @param lazyListState The lazy list state to scroll.
 * @param config Auto-scroll configuration (threshold, speed, interval).
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
@ExperimentalDndApi
@Composable
fun <T> Modifier.dragAutoScroll(
    state: DragAndDropState<T>,
    lazyListState: LazyListState,
    config: DragAutoScrollConfig = DragAutoScrollConfig(),
): Modifier {
    val density = LocalDensity.current
    val minThresholdPx = with(density) { config.minScrollThreshold.toPx() }

    var containerTopLeft by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(
        state,
        lazyListState,
        minThresholdPx,
        config.maxScrollSpeed,
    ) {
        snapshotFlow {
            if (!state.isActiveDrag) return@snapshotFlow null
            val dragPosition = state.dragPosition.value
            val itemSize = state.currentDraggableItem?.size ?: return@snapshotFlow null
            val orientation = lazyListState.layoutInfo.orientation

            val (itemStart, itemEnd, containerStart, containerEnd) = resolveScrollAxis(
                orientation = orientation,
                dragPosition = dragPosition,
                itemSize = itemSize,
                containerTopLeft = containerTopLeft,
                containerSize = containerSize,
            )

            // Dynamic threshold based on first/last visible item size
            val visibleItems = lazyListState.layoutInfo.visibleItemsInfo
            val startThresholdPx = maxOf(
                minThresholdPx,
                visibleItems.firstOrNull()?.size?.toFloat() ?: minThresholdPx,
            )
            val endThresholdPx = maxOf(
                minThresholdPx,
                visibleItems.lastOrNull()?.size?.toFloat() ?: minThresholdPx,
            )

            computeScrollSpeed(
                itemStart = itemStart,
                itemEnd = itemEnd,
                containerStart = containerStart,
                containerEnd = containerEnd,
                startThresholdPx = startThresholdPx,
                endThresholdPx = endThresholdPx,
                maxScrollSpeed = config.maxScrollSpeed,
            )
        }.collectLatest { scrollSpeed ->
            if (scrollSpeed == null || scrollSpeed == 0f) return@collectLatest

            var lastFrameMs = withFrameMillis { it }
            while (true) {
                val currentFrameMs = withFrameMillis { it }
                val deltaMs = currentFrameMs - lastFrameMs
                lastFrameMs = currentFrameMs
                val scrollAmount = scrollSpeed * deltaMs / 1000f
                lazyListState.scrollBy(scrollAmount)
                state.reevaluateDropTargets()
            }
        }
    }

    // Workaround to fix scroll jump when dragging the first items.
    // When the first visible item is at index 0 or 1 during a drag, LazyList's internal
    // scroll anchoring can cause jumps. Pinning the scroll position prevents this.
    LaunchedEffect(state, lazyListState) {
        snapshotFlow { state.hoveredDropTargetKey }
            .filter { it != null }
            .distinctUntilChanged()
            .collect {
                if (lazyListState.firstVisibleItemIndex == 0 || lazyListState.firstVisibleItemIndex == 1) {
                    lazyListState.scrollToItem(
                        lazyListState.firstVisibleItemIndex,
                        lazyListState.firstVisibleItemScrollOffset,
                    )
                }
            }
    }

    return this.onPlaced { coordinates ->
        containerTopLeft = coordinates.positionInRoot()
        containerSize = coordinates.size.toSize()
    }
}

/**
 * Enables automatic scrolling of a [LazyGridState] when a dragged item approaches
 * the edges of the scrollable container.
 *
 * Supports both [LazyVerticalGrid][androidx.compose.foundation.lazy.grid.LazyVerticalGrid] and
 * [LazyHorizontalGrid][androidx.compose.foundation.lazy.grid.LazyHorizontalGrid] — the scroll
 * axis is determined automatically from [LazyGridState.layoutInfo].
 *
 * @param state The drag and drop state to observe.
 * @param lazyGridState The lazy grid state to scroll.
 * @param config Auto-scroll configuration (threshold, speed, interval).
 */
@ExperimentalDndApi
@Composable
fun <T> Modifier.dragAutoScroll(
    state: DragAndDropState<T>,
    lazyGridState: LazyGridState,
    config: DragAutoScrollConfig = DragAutoScrollConfig(),
): Modifier {
    val density = LocalDensity.current
    val minThresholdPx = with(density) { config.minScrollThreshold.toPx() }

    var containerTopLeft by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(
        state,
        lazyGridState,
        minThresholdPx,
        config.maxScrollSpeed,
    ) {
        snapshotFlow {
            if (!state.isActiveDrag) return@snapshotFlow null
            val dragPosition = state.dragPosition.value
            val itemSize = state.currentDraggableItem?.size ?: return@snapshotFlow null
            val orientation = lazyGridState.layoutInfo.orientation

            val (itemStart, itemEnd, containerStart, containerEnd) = resolveScrollAxis(
                orientation = orientation,
                dragPosition = dragPosition,
                itemSize = itemSize,
                containerTopLeft = containerTopLeft,
                containerSize = containerSize,
            )

            // Dynamic threshold based on first/last visible item size along the scroll axis
            val visibleItems = lazyGridState.layoutInfo.visibleItemsInfo
            val firstItemMainAxisSize = visibleItems.firstOrNull()?.let {
                if (orientation == Orientation.Vertical) it.size.height else it.size.width
            }
            val lastItemMainAxisSize = visibleItems.lastOrNull()?.let {
                if (orientation == Orientation.Vertical) it.size.height else it.size.width
            }

            val startThresholdPx = maxOf(
                minThresholdPx,
                firstItemMainAxisSize?.toFloat() ?: minThresholdPx,
            )
            val endThresholdPx = maxOf(
                minThresholdPx,
                lastItemMainAxisSize?.toFloat() ?: minThresholdPx,
            )

            computeScrollSpeed(
                itemStart = itemStart,
                itemEnd = itemEnd,
                containerStart = containerStart,
                containerEnd = containerEnd,
                startThresholdPx = startThresholdPx,
                endThresholdPx = endThresholdPx,
                maxScrollSpeed = config.maxScrollSpeed,
            )
        }.collectLatest { scrollSpeed ->
            if (scrollSpeed == null || scrollSpeed == 0f) return@collectLatest

            var lastFrameMs = withFrameMillis { it }
            while (true) {
                val currentFrameMs = withFrameMillis { it }
                val deltaMs = currentFrameMs - lastFrameMs
                lastFrameMs = currentFrameMs
                val scrollAmount = scrollSpeed * deltaMs / 1000f
                lazyGridState.scrollBy(scrollAmount)
                state.reevaluateDropTargets()
            }
        }
    }

    // Same first-item workaround as LazyList
    LaunchedEffect(state, lazyGridState) {
        snapshotFlow { state.hoveredDropTargetKey }
            .filter { it != null }
            .distinctUntilChanged()
            .collect {
                if (lazyGridState.firstVisibleItemIndex == 0 || lazyGridState.firstVisibleItemIndex == 1) {
                    lazyGridState.scrollToItem(
                        lazyGridState.firstVisibleItemIndex,
                        lazyGridState.firstVisibleItemScrollOffset,
                    )
                }
            }
    }

    return this.onPlaced { coordinates ->
        containerTopLeft = coordinates.positionInRoot()
        containerSize = coordinates.size.toSize()
    }
}

/**
 * Enables automatic scrolling of a [ScrollState] when a dragged item approaches
 * the edges of the scrollable container.
 *
 * Since [ScrollState] does not expose its orientation, you must provide it explicitly.
 *
 * @param state The drag and drop state to observe.
 * @param scrollState The scroll state to scroll.
 * @param orientation The scroll orientation ([Orientation.Vertical] or [Orientation.Horizontal]).
 * @param config Auto-scroll configuration (threshold, speed, interval).
 */
@ExperimentalDndApi
@Composable
fun <T> Modifier.dragAutoScroll(
    state: DragAndDropState<T>,
    scrollState: ScrollState,
    orientation: Orientation,
    config: DragAutoScrollConfig = DragAutoScrollConfig(),
): Modifier {
    val density = LocalDensity.current
    val minThresholdPx = with(density) { config.minScrollThreshold.toPx() }

    var containerTopLeft by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    LaunchedEffect(
        state,
        scrollState,
        orientation,
        minThresholdPx,
        config.maxScrollSpeed,
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
                    val itemSize = state.currentDraggableItem?.size
                        ?: continue

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

                    // ScrollState has no visible items — use 20% of viewport as
                    // threshold (similar to dnd-kit), with minThreshold as floor
                    val thresholdPx = maxOf(minThresholdPx, viewportPx * 0.2f)

                    val scrollSpeed = computeScrollSpeed(
                        itemStart = itemStart,
                        itemEnd = itemEnd,
                        containerStart = containerStart,
                        containerEnd = containerEnd,
                        startThresholdPx = thresholdPx,
                        endThresholdPx = thresholdPx,
                        maxScrollSpeed = config.maxScrollSpeed,
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

/**
 * Resolves the dragged item and container positions along the scroll axis.
 */
private data class ScrollAxisBounds(
    val itemStart: Float,
    val itemEnd: Float,
    val containerStart: Float,
    val containerEnd: Float,
)

private fun resolveScrollAxis(
    orientation: Orientation,
    dragPosition: Offset,
    itemSize: Size,
    containerTopLeft: Offset,
    containerSize: Size,
): ScrollAxisBounds {
    return if (orientation == Orientation.Vertical) {
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

        else -> 0f
    }
}
