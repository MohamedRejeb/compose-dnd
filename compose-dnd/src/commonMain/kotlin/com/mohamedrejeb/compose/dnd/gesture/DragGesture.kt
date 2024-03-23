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
package com.mohamedrejeb.compose.dnd.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.utils.awaitPointerSlopOrCancellation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal suspend fun <T> PointerInputScope.detectDragStartGesture(
    key: Any,
    state: DragAndDropState<T>,
    enabled: Boolean,
    dragAfterLongPress: Boolean,
) = coroutineScope {
    if (!enabled) return@coroutineScope

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
        var drag: PointerInputChange?
        if (dragAfterLongPress) {
            drag = awaitLongPressOrCancellation(down.id)
        } else {
            do {
                drag = awaitPointerSlopOrCancellation(
                    down.id,
                    down.type,
                    triggerOnMainAxisSlop = false
                ) { change, _ ->
                    change.consume()
                }
            } while (drag != null && !drag.isConsumed)
        }

        if (drag != null) {
            val draggableItemState = state.draggableItemMap[key] ?: return@awaitEachGesture

            launch {
                state.handleDragStart(drag.position + draggableItemState.positionInRoot)
            }

            state.pointerId = drag.id
        }
    }
}
