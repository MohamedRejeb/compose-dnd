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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
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
 * @param dropStrategy - strategy to determine the drop target
 * @param dropAnimationSpec - animation spec for the position drop animation
 * @param sizeDropAnimationSpec - animation spec for the size drop animation
 * @param draggableContent The content of the draggable item
 * @param content - content that will be shown when item is not dragged
 */
@Composable
internal fun <T> CoreDraggableItem(
    modifier: Modifier = Modifier,
    key: Any,
    data: T,
    state: DragAndDropState<T>,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = state.dragAfterLongPress,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    draggableContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val draggableItemState = remember(key) {
        DraggableItemState(
            key = key,
            data = data,
            positionInRoot = Offset.Zero,
            size = Size.Zero,
            dropTargets = dropTargets,
            dropStrategy = dropStrategy,
            dropAnimationSpec = dropAnimationSpec,
            sizeDropAnimationSpec = sizeDropAnimationSpec,
            content = draggableContent,
        )
    }

    LaunchedEffect(draggableItemState, data) {
        draggableItemState.data = data
    }

    LaunchedEffect(draggableItemState, dropTargets) {
        draggableItemState.dropTargets = dropTargets
    }

    LaunchedEffect(draggableItemState, dropStrategy) {
        draggableItemState.dropStrategy = dropStrategy
    }

    LaunchedEffect(draggableItemState, dropAnimationSpec) {
        draggableItemState.dropAnimationSpec = dropAnimationSpec
    }

    LaunchedEffect(draggableItemState, sizeDropAnimationSpec) {
        draggableItemState.sizeDropAnimationSpec = sizeDropAnimationSpec
    }

    LaunchedEffect(draggableItemState, draggableContent) {
        draggableItemState.content = draggableContent
    }

    DisposableEffect(key, state, draggableItemState) {
        state.addDraggableItem(draggableItemState)

        onDispose {
            state.removeDraggableItem(key)
        }
    }

    Box(
        modifier = modifier
            .onPlaced {
                draggableItemState.positionInRoot = it.positionInRoot()
            }
            .onSizeChanged {
                draggableItemState.size = it.toSize()
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
