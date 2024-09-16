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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.compose.dnd.DragAndDropState

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
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    draggableContent: (@Composable () -> Unit)? = null,
    content: @Composable DraggableItemScope.() -> Unit,
) {
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

    CoreDraggableItem(
        modifier = modifier,
        key = key,
        data = data,
        state = state,
        enabled = enabled,
        dragAfterLongPress = dragAfterLongPress,
        dropTargets = dropTargets,
        dropStrategy = dropStrategy,
        dropAnimationSpec = dropAnimationSpec,
        sizeDropAnimationSpec = sizeDropAnimationSpec,
        draggableContent = draggableContent ?: {
            with(draggableItemScopeShadowImpl) {
                content()
            }
        },
    ) {
        with(draggableItemScopeImpl) {
            content()
        }
    }
}
