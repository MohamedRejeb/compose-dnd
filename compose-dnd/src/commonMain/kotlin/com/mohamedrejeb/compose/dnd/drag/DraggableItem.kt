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
package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture

/**
 * Wrapper Composable for draggable item.
 *
 * @param modifier - modifier for the DraggableItem composable
 * @param key - unique key for this item
 * @param data - data that will be passed to drop target on drop
 * @param state - state of the drag and drop
 * @param enabled - whether the drag and drop is enabled
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * @param dropTargets - list of drop targets ids to which this item can be dropped, if empty, item can be dropped to any drop target
 * @param dropAnimationSpec - animation spec for the drop animation
 * @param draggableContent The content of the draggable item, if null, the content of the item will be used.
 * @param content - content that will be shown when item is not dragged
 */
@Composable
fun <T> DraggableItem(
    modifier: Modifier = Modifier,
    key: Any,
    data: T,
    state: DragAndDropState<T>,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = state.dragAfterLongPress,
    dropTargets: List<Any> = emptyList(),
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    draggableContent: (@Composable () -> Unit)? = null,
    content: @Composable DraggableItemScope.() -> Unit,
) {
    LaunchedEffect(key, state, data) {
        state.draggableItemMap[key]?.data = data
    }

    LaunchedEffect(key, state, dropTargets) {
        state.draggableItemMap[key]?.dropTargets = dropTargets
    }

    LaunchedEffect(key, state, dropAnimationSpec) {
        state.draggableItemMap[key]?.dropAnimationSpec = dropAnimationSpec
    }

    DisposableEffect(key, state) {
        onDispose {
            state.removeDraggableItem(key)
        }
    }

    val draggableItemScopeImpl = remember(key, state) {
        DraggableItemScopeImpl(
            key = key,
            state = state,
        )
    }

    val draggableItemScopeShadowImpl = remember(key) {
        DraggableItemScopeShadowImpl(
            key = key,
        )
    }

    with(draggableItemScopeImpl) {
        Box(
            modifier = modifier
                .onGloballyPositioned {
                    val draggableItemState = DraggableItemState(
                        key = key,
                        data = data,
                        positionInRoot = it.positionInRoot(),
                        size = it.size.toSize(),
                        dropTargets = dropTargets,
                        dropAnimationSpec = dropAnimationSpec,
                        content = draggableContent ?: {
                            with(draggableItemScopeShadowImpl) {
                                content()
                            }
                        },
                    )

                    state.addOrUpdateDraggableItem(
                        state = draggableItemState,
                    )
                }
                .pointerInput(enabled, key, state, state.enabled) {
                    detectDragStartGesture(
                        key = key,
                        state = state,
                        enabled = enabled && state.enabled,
                        dragAfterLongPress = dragAfterLongPress,
                    )
                },
        ) {
            content()
        }
    }
}
