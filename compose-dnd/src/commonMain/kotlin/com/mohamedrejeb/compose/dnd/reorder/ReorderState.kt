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

/**
 * Remember [ReorderState]
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press.
 * This parameter is applied to all [ReorderableItem]s. If you want to change it for a specific item, use [ReorderableItem] parameter.
 * @param requireFirstDownUnconsumed if true, the first down event must be unconsumed to start the drag.
 * @param reorderHysteresisDistance how far the drag must come back after a reorder swap
 * before that swap is undone. `0.dp` leaves only the spacing between the items as play.
 */
@Deprecated(
    message = "ReorderState is deprecated, reorder with a DragAndDropState instead.",
    replaceWith = ReplaceWith(
        "rememberDragAndDropState<T>(dragAfterLongPress, requireFirstDownUnconsumed, reorderHysteresisDistance)",
        "com.mohamedrejeb.compose.dnd.rememberDragAndDropState",
    ),
)
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
@Deprecated(
    message = "ReorderState is deprecated, reorder with a DragAndDropState instead: " +
        "rememberDragAndDropState, DragAndDropContainer and the reorderableItem modifier " +
        "or ReorderableItem composable.",
    replaceWith = ReplaceWith(
        "DragAndDropState<T>",
        "com.mohamedrejeb.compose.dnd.DragAndDropState",
    ),
)
@Stable
class ReorderState<T>(
    dragAfterLongPress: Boolean = false,
    requireFirstDownUnconsumed: Boolean = false,
) {
    /**
     * State of the drag and drop
     */
    val dndState = DragAndDropState<T>(
        dragAfterLongPress = dragAfterLongPress,
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
    )

    val draggedItem get() = dndState.draggedItem

    val hoveredDropTargetKey get() = dndState.hoveredDropTargetKey
}
