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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItemState
import com.mohamedrejeb.compose.dnd.utils.awaitPointerSlopOrCancellation

/**
 * Detect the gesture that starts dragging the item registered under [key].
 *
 * @param draggableItemState The state of the item hosting this gesture, or
 * null when the gesture comes from a drag handle. It is only used to skip the
 * item-level gesture while the item uses a drag handle; the item that gets
 * dragged is always resolved from the registry when the drag starts.
 * @param gestureSourcePositionInRoot The root position of the composable that
 * hosts this gesture. When null, the item's own live position is used; a drag
 * handle passes its own position so the drag offset is computed correctly.
 */
internal suspend fun <T> PointerInputScope.detectDragStartGesture(
    key: Any,
    state: DragAndDropState<T>,
    draggableItemState: DraggableItemState<T>?,
    enabled: Boolean,
    dragAfterLongPress: Boolean,
    requireFirstDownUnconsumed: Boolean,
    gestureSourcePositionInRoot: (() -> Offset)? = null,
) {
    if (!enabled) return

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = requireFirstDownUnconsumed, pass = PointerEventPass.Main)

        // With a drag handle, only the handle's own gesture detector starts
        // the drag; the rest of the item stays interactive.
        if (draggableItemState?.usesDragHandle() == true) return@awaitEachGesture

        // Another pointer is already dragging (multi-touch)
        if (!state.canStartDrag()) return@awaitEachGesture

        var drag: PointerInputChange?
        if (dragAfterLongPress) {
            drag = awaitLongPressOrCancellation(down.id)
        } else {
            do {
                drag = awaitPointerSlopOrCancellation(
                    down.id,
                    down.type,
                    triggerOnMainAxisSlop = false,
                ) { change, _ ->
                    change.consume()
                }
            } while (drag != null && !drag.isConsumed)
        }

        if (drag == null || !state.canStartDrag()) return@awaitEachGesture

        // The touched item wins over a stale registration under the same key
        // (e.g. the item was just recreated in another list)
        if (draggableItemState != null) state.addDraggableItem(draggableItemState)
        val itemState = state.draggableItemMap[key] ?: return@awaitEachGesture
        val sourcePositionInRoot = gestureSourcePositionInRoot?.invoke()
            ?: itemState.livePositionInRoot()

        val started = state.handleDragStart(
            key = key,
            offset = drag.position + sourcePositionInRoot,
        )

        if (started) {
            state.pointerId = drag.id
        }
    }
}
