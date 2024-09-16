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
package com.mohamedrejeb.compose.dnd

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
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
 * @param enabled whether the drag and drop is enabled
 * @param content content of the container
 */
@Composable
fun <T> DragAndDropContainer(
    state: DragAndDropState<T>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val positionInRoot = remember {
        mutableStateOf(Offset.Zero)
    }

    LaunchedEffect(enabled) {
        state.enabled = enabled
    }

    Box(
        modifier = modifier
            .onPlaced {
                positionInRoot.value = it.positionInRoot()
            }
            .pointerInput(enabled, state, state.pointerId) {
                if (!enabled) return@pointerInput

                awaitEachGesture {
                    if (state.pointerId == null) {
                        awaitPointerEvent()
                    }

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
