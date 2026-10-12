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

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.mohamedrejeb.compose.dnd.DragAndDropState

/**
 * Pins the scroll position of a [LazyListState] during an active drag.
 *
 * When items with different sizes swap positions, Compose's key-based scroll anchoring
 * adjusts the viewport to compensate for the size change, causing a visual jump.
 * This modifier uses [LazyListState.requestScrollToItem] to override anchoring
 * **before** layout runs, eliminating the jump entirely (zero-frame fix).
 *
 * Use this modifier on any [LazyColumn][androidx.compose.foundation.lazy.LazyColumn] or
 * [LazyRow][androidx.compose.foundation.lazy.LazyRow] that supports reordering.
 * [dragAutoScroll] already includes this behavior, so you do **not** need both.
 *
 * @param state The drag and drop state to observe.
 * @param lazyListState The lazy list state whose scroll position should be pinned.
 */
@Composable
fun <T> Modifier.dragScrollPin(
    state: DragAndDropState<T>,
    lazyListState: LazyListState,
): Modifier =
    pinScrollBeforeHoverChange(state, lazyListState) { _, _ ->
        lazyListState.requestScrollToItem(
            lazyListState.firstVisibleItemIndex,
            lazyListState.firstVisibleItemScrollOffset,
        )
    }

/**
 * Pins the scroll position of a [LazyGridState] during an active drag.
 *
 * Grid equivalent of [dragScrollPin] for [LazyListState].
 * [dragAutoScroll] already includes this behavior, so you do **not** need both.
 *
 * @param state The drag and drop state to observe.
 * @param lazyGridState The lazy grid state whose scroll position should be pinned.
 */
@Composable
fun <T> Modifier.dragScrollPin(
    state: DragAndDropState<T>,
    lazyGridState: LazyGridState,
): Modifier =
    pinScrollBeforeHoverChange(state, lazyGridState) { _, _ ->
        lazyGridState.requestScrollToItem(
            lazyGridState.firstVisibleItemIndex,
            lazyGridState.firstVisibleItemScrollOffset,
        )
    }

/**
 * Pins the scroll position of a [LazyStaggeredGridState] during an active drag.
 *
 * Staggered grid equivalent of [dragScrollPin] for [LazyListState].
 * [dragAutoScroll] already includes this behavior, so you do **not** need both.
 *
 * A staggered grid keeps one scroll offset per lane, and pinning resets them all to the
 * offset of the first visible item. So the position is only pinned when the swap moves
 * that first item, which is when the grid would follow it; any other swap leaves the
 * scroll position alone.
 *
 * @param state The drag and drop state to observe.
 * @param lazyStaggeredGridState The lazy staggered grid state whose scroll position should be pinned.
 */
@Composable
fun <T> Modifier.dragScrollPin(
    state: DragAndDropState<T>,
    lazyStaggeredGridState: LazyStaggeredGridState,
): Modifier =
    pinScrollBeforeHoverChange(state, lazyStaggeredGridState) { draggedKey, enteredKey ->
        if (!lazyStaggeredGridState.movesFirstVisibleItem(draggedKey, enteredKey)) {
            return@pinScrollBeforeHoverChange
        }
        lazyStaggeredGridState.requestScrollToItem(
            lazyStaggeredGridState.firstVisibleItemIndex,
            lazyStaggeredGridState.firstVisibleItemScrollOffset,
        )
    }

/**
 * Whether moving the dragged item to the slot of the entered one changes the index of
 * the first visible item, the one the grid anchors its scroll position to.
 *
 * True when it can't be told, e.g. when the slot of the dragged item is not visible.
 */
private fun LazyStaggeredGridState.movesFirstVisibleItem(draggedKey: Any, enteredKey: Any?): Boolean {
    if (enteredKey == null) return false

    val visibleItems = layoutInfo.visibleItemsInfo
    val draggedIndex = visibleItems.firstOrNull { it.key == draggedKey }?.index ?: return true
    val enteredIndex = visibleItems.firstOrNull { it.key == enteredKey }?.index ?: return true

    return minOf(draggedIndex, enteredIndex) <= firstVisibleItemIndex
}

/**
 * Register [pin] to run synchronously **before** onDragEnter/onDragExit fire, so
 * the lazy layout uses the pinned position for the next layout pass instead of
 * key-based anchoring — no 1-frame jump.
 */
@Composable
private fun <T> Modifier.pinScrollBeforeHoverChange(
    state: DragAndDropState<T>,
    lazyState: Any,
    pin: (draggedKey: Any, enteredKey: Any?) -> Unit,
): Modifier {
    DisposableEffect(state, lazyState) {
        state.onBeforeHoverTargetChange.add(pin)
        onDispose {
            state.onBeforeHoverTargetChange.remove(pin)
        }
    }

    return this
}
