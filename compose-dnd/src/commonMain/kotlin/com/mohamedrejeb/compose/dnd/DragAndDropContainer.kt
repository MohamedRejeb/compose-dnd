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

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.util.fastFirstOrNull
import com.mohamedrejeb.compose.dnd.drag.DraggedItemShadow
import kotlinx.coroutines.launch

/**
 * Container for drag and drop.
 *
 * All draggable items and drop targets sharing [state] must be placed inside
 * this container: it tracks the drag gesture once an item starts it, and draws
 * the drag shadow on top of its content.
 *
 * @param state The state of the drag and drop
 * @param modifier [Modifier]
 * @param enabled Whether drag and drop is enabled. Disabling the container
 * while a drag is in progress cancels that drag.
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

    DisposableEffect(state, scope) {
        state.containerScope = scope
        onDispose {
            if (state.containerScope === scope) {
                state.containerScope = null
                // Nothing is left to follow the pointer or animate the drop
                state.abortDrag()
            }
        }
    }

    SideEffect {
        state.enabled = enabled
        // Disabled mid-drag: end the drag instead of leaving it stuck
        if (!enabled) state.cancelDrag()
    }

    Box(
        modifier = modifier
            .onPlaced {
                positionInRoot.value = it.positionInRoot()
            }.pointerInput(enabled, state) {
                if (!enabled) return@pointerInput

                awaitPointerEventScope {
                    // Not awaitEachGesture: that waits for every pointer to lift between
                    // drags, so a finger resting on the container would block the next one
                    while (true) {
                        trackDrag(
                            state = state,
                            containerPositionInRoot = { positionInRoot.value },
                            onDragEnd = { scope.launch { state.handleDragEnd() } },
                            onDragCancel = { scope.launch { state.handleDragCancel() } },
                        )
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

/**
 * Follow the pointer that started a drag until it is released.
 *
 * A drag past the touch slop starts while its item handles an event of the Main
 * pass, which reaches children before this container: the container notices it
 * on that very event. A long press starts the drag between two events instead.
 *
 * Once the drag is on, the container follows the pointer in the Initial pass,
 * which reaches it before its children: a scrollable or clickable under the
 * pointer never gets to act on a move or on the release of a drag.
 */
private suspend fun <T> AwaitPointerEventScope.trackDrag(
    state: DragAndDropState<T>,
    containerPositionInRoot: () -> Offset,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
) {
    var event = currentEvent
    // The item consumed the event that started the drag itself
    var isStartEvent = state.pointerId != null
    while (state.pointerId == null) {
        event = awaitPointerEvent(PointerEventPass.Initial)
        // Started by a long press since the last event
        if (state.pointerId != null) break
        event = awaitPointerEvent(PointerEventPass.Main)
        isStartEvent = state.pointerId != null
    }
    val pointerId = state.pointerId ?: return

    while (true) {
        // canceled programmatically: stop following this pointer
        if (state.pointerId != pointerId) return

        val change = event.changes.fastFirstOrNull { it.id == pointerId }
        when {
            change == null -> {
                state.pointerId = null
                onDragCancel()
                return
            }

            // A gesture canceled by the system comes as an up that is already consumed
            change.changedToUpIgnoreConsumed() && change.isConsumed -> {
                state.pointerId = null
                onDragCancel()
                return
            }

            change.changedToUpIgnoreConsumed() -> {
                // consume the up so a clickable under the pointer doesn't fire
                change.consume()
                state.pointerId = null
                onDragEnd()
                return
            }

            change.isConsumed && !isStartEvent -> {
                state.pointerId = null
                onDragCancel()
                return
            }

            else -> {
                if (change.positionChanged()) {
                    state.handleDrag(change.position + containerPositionInRoot())
                }
                // Also an event without movement: left unconsumed, a scrollable under
                // the pointer would take it as its own drag and scroll
                change.consume()
            }
        }

        isStartEvent = false
        event = awaitPointerEvent(PointerEventPass.Initial)
    }
}
