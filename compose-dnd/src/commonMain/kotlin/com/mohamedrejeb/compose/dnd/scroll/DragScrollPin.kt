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

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi

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
@ExperimentalDndApi
@Composable
fun <T> Modifier.dragScrollPin(
    state: DragAndDropState<T>,
    lazyListState: LazyListState,
): Modifier {
    // Register a synchronous callback that fires BEFORE onDragEnter/onDragExit.
    // requestScrollToItem tells LazyList to use this position for the next
    // layout pass instead of key-based anchoring — no 1-frame jump.
    DisposableEffect(state, lazyListState) {
        val callback: () -> Unit = {
            lazyListState.requestScrollToItem(
                lazyListState.firstVisibleItemIndex,
                lazyListState.firstVisibleItemScrollOffset,
            )
        }
        state.onBeforeHoverTargetChange.add(callback)
        onDispose {
            state.onBeforeHoverTargetChange.remove(callback)
        }
    }

    return this
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
@ExperimentalDndApi
@Composable
fun <T> Modifier.dragScrollPin(
    state: DragAndDropState<T>,
    lazyGridState: LazyGridState,
): Modifier {
    DisposableEffect(state, lazyGridState) {
        val callback: () -> Unit = {
            lazyGridState.requestScrollToItem(
                lazyGridState.firstVisibleItemIndex,
                lazyGridState.firstVisibleItemScrollOffset,
            )
        }
        state.onBeforeHoverTargetChange.add(callback)
        onDispose {
            state.onBeforeHoverTargetChange.remove(callback)
        }
    }

    return this
}
