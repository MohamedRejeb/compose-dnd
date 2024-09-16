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

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.CoreDraggableItem
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drop.dropTarget

/**
 * Mark this composable as a reorderable item.
 *
 * @param modifier The modifier to be applied to the item.
 * @param state The reorder state.
 * @param key The key used to identify the item.
 * @param data The data associated with the item.
 * @param zIndex The z-index of the item.
 * @param enabled Whether the reorder is enabled.
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * @param dropTargets - list of drop targets ids to which this item can be dropped, if empty, item can be dropped to any drop target
 * @param dropStrategy - strategy to determine the drop target
 * @param onDrop The action to perform when an item is dropped onto the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragEnter The action to perform when an item is dragged over the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragExit The action to perform when an item is dragged out of the target.
 * Accepts the dragged item state as a parameter.
 * @param dropAnimationSpec - animation spec for the drop animation
 * @param sizeDropAnimationSpec - animation spec for the size drop animation
 * @param draggableContent The content of the draggable item, if null, the content of the item will be used.
 * @param content The content of the item.
 */
@OptIn(ExperimentalDndApi::class)
@Composable
fun <T> ReorderableItem(
    modifier: Modifier = Modifier,
    state: ReorderState<T>,
    key: Any,
    data: T,
    zIndex: Float = 0f,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = state.dndState.dragAfterLongPress,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    onDrop: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnter: (state: DraggedItemState<T>) -> Unit = {},
    onDragExit: (state: DraggedItemState<T>) -> Unit = {},
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    draggableContent: (@Composable () -> Unit)? = null,
    content: @Composable ReorderableItemScope.() -> Unit,
) {
    val reorderableItemScopeImpl = remember(key, state) {
        ReorderableItemScopeImpl(
            key = key,
            state = state.dndState,
        )
    }

    val reorderableItemScopeShadowImpl = remember(key) {
        ReorderableItemScopeShadowImpl(
            key = key,
        )
    }

    CoreDraggableItem(
        modifier = modifier
            .dropTarget(
                key = key,
                state = state.dndState,
                zIndex = zIndex,
                onDrop = onDrop,
                onDragEnter = onDragEnter,
                onDragExit = onDragExit,
            ),
        key = key,
        data = data,
        state = state.dndState,
        enabled = enabled,
        dragAfterLongPress = dragAfterLongPress,
        dropTargets = dropTargets,
        dropStrategy = dropStrategy,
        dropAnimationSpec = dropAnimationSpec,
        sizeDropAnimationSpec = sizeDropAnimationSpec,
        draggableContent = draggableContent ?: {
            with(reorderableItemScopeShadowImpl) {
                content()
            }
        },
    ) {
        with(reorderableItemScopeImpl) {
            content()
        }
    }
}
