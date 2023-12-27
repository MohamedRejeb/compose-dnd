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
import kotlinx.coroutines.launch

internal suspend fun <T> PointerInputScope.detectDragStartGesture(
    key: Any,
    state: DragAndDropState<T>,
    dragAfterLongPress: Boolean,
    scope: CoroutineScope,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Main)
        var drag: PointerInputChange?
        if (dragAfterLongPress)
            drag = awaitLongPressOrCancellation(down.id)
        else
            do {
                drag = awaitPointerSlopOrCancellation(
                    down.id,
                    down.type,
                    triggerOnMainAxisSlop = false
                ) { change, _ ->
                    change.consume()
                }
            } while (drag != null && !drag.isConsumed)

        if (drag != null) {
            val draggableItemState = state.draggableItemMap[key] ?: return@awaitEachGesture

            scope.launch {
                state.handleDragStart(drag.position + draggableItemState.positionInRoot)
            }

            state.pointerId = drag.id
        }
    }
}
