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
package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.mohamedrejeb.compose.dnd.DefaultReorderHysteresisDistance
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi

/**
 * Remember [ReorderState]
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press.
 * This parameter is applied to all [ReorderableItem]s. If you want to change it for a specific item, use [ReorderableItem] parameter.
 * @param requireFirstDownUnconsumed if true, the first down event must be unconsumed to start the drag.
 * @param reorderHysteresisDistance how far the cursor must move back, opposite the swap direction,
 * before a just-swapped reorder target can be re-entered. `0.dp` disables it.
 * @return [ReorderState]
 */
@OptIn(ExperimentalDndApi::class)
@Composable
fun <T> rememberReorderState(
    dragAfterLongPress: Boolean = false,
    requireFirstDownUnconsumed: Boolean = false,
    reorderHysteresisDistance: Dp = DefaultReorderHysteresisDistance,
): ReorderState<T> {
    val density = LocalDensity.current
    val state = remember(dragAfterLongPress, requireFirstDownUnconsumed) {
        ReorderState<T>(
            dragAfterLongPress = dragAfterLongPress,
            requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        )
    }
    state.dndState.reorderHysteresisDistancePx = with(density) { reorderHysteresisDistance.toPx() }
    return state
}

/**
 * State of the reorder
 *
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * @param requireFirstDownUnconsumed if true, the first down event must be unconsumed to start the drag
 * This parameter is applied to all [ReorderableItem]s. If you want to change it for a specific item, use [ReorderableItem] parameter.
 */
@Stable
class ReorderState<T>(
    dragAfterLongPress: Boolean = false,
    requireFirstDownUnconsumed: Boolean = false,
) {
    /**
     * State of the drag and drop
     */
    @ExperimentalDndApi
    val dndState = DragAndDropState<T>(
        dragAfterLongPress = dragAfterLongPress,
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
    )

    @OptIn(ExperimentalDndApi::class)
    val draggedItem get() = dndState.draggedItem

    @OptIn(ExperimentalDndApi::class)
    val hoveredDropTargetKey get() = dndState.hoveredDropTargetKey
}
