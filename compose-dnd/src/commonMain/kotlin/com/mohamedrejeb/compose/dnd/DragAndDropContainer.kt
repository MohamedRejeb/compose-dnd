package com.mohamedrejeb.compose.dnd

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.mohamedrejeb.compose.dnd.drag.DraggedItemShadow
import com.mohamedrejeb.compose.dnd.utils.fastForEach
import kotlinx.coroutines.launch

/**
 * Container for drag and drop,
 * All draggable items and drop targets should be inside this container
 *
 * @param state The state of the drag and drop
 * @param modifier [Modifier]
 * @param content content of the container
 */
@Composable
fun <T> DragAndDropContainer(
    state: DragAndDropState<T>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val positionInRoot = remember {
        mutableStateOf(Offset.Zero)
    }

    Box(
        modifier = modifier
            .onGloballyPositioned {
                positionInRoot.value = it.positionInRoot()
            }
            .pointerInput(state, state.pointerId) {
                awaitEachGesture {
                    if (state.pointerId == null)
                        awaitPointerEvent()

                    state.pointerId?.let { pointerId ->
                        if (
                            drag(pointerId) {
                                scope.launch {
                                    state.handleDrag(it.position + positionInRoot.value)
                                }
                                it.consume()
                            }
                        ) {
                            // consume up if we quit drag gracefully with the up
                            currentEvent.changes.fastForEach {
                                if (it.changedToUp()) it.consume()
                            }
                            scope.launch {
                                state.handleDragEnd()
                            }
                            state.pointerId = null
                        } else {
                            scope.launch {
                                state.handleDragCancel()
                            }
                            state.pointerId = null
                        }
                    }
                }
            },
    ) {
        content()

        DraggedItemShadow(
            state = state,
        )
    }
}
