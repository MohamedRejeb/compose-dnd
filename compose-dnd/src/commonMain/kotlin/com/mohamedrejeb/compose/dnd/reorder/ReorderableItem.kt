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
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DragAxis
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.draggableItemImpl
import com.mohamedrejeb.compose.dnd.drop.dropTarget

/**
 * Mark this composable as a reorderable item: both draggable and a drop target.
 *
 * Composable alternative to the [reorderableItem] modifier: [content] gets a
 * [ReorderableItemScope] exposing [ReorderableItemScope.isDragging] and a
 * [ReorderableItemScope.dragHandle] modifier that needs no key or state.
 *
 * @param key The key used to identify the item.
 * @param data The data associated with the item.
 * @param state The drag and drop state.
 * @param modifier The modifier to be applied to the item.
 * @param enabled Whether the reorder is enabled.
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * @param requireFirstDownUnconsumed if true, the first down event must be unconsumed to start the drag
 * @param dropTargets - list of drop targets ids to which this item can be dropped, if empty, item can be dropped to any drop target
 * @param dropStrategy - strategy to determine the drop target
 * @param dragAxis - axis constraint for drag movement
 * @param zIndex The z-index of the item.
 * @param dropAnimationSpec - animation spec for the drop animation
 * @param dropAlignment Alignment of the dropped item within this item.
 * @param dropOffset Offset applied to the dropped item position.
 * @param dropAnimationEnabled Whether the drop animation is enabled.
 * @param canDrop Whether this item currently accepts other items as a drop target. When false,
 * dragged items pass over it without hovering it, so it is never swapped.
 * @param onDrop The action to perform when an item is dropped onto the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragEnter The action to perform when an item is dragged over the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragExit The action to perform when an item is dragged out of the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragStart Called when a drag of this item starts.
 * @param onDragEnd Called once the drag of this item is over, after the drop or return animation.
 * @param draggableContent The content of the draggable item, if null, the content of the item will be used.
 * @param content The content of the item.
 */
@Composable
fun <T> ReorderableItem(
    key: Any,
    data: T,
    state: DragAndDropState<T>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = state.dragAfterLongPress,
    requireFirstDownUnconsumed: Boolean = state.requireFirstDownUnconsumed,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.Surface,
    dragAxis: DragAxis = DragAxis.Free,
    zIndex: Float = 0f,
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    dropAlignment: Alignment = Alignment.Center,
    dropOffset: Offset = Offset.Zero,
    dropAnimationEnabled: Boolean = true,
    canDrop: Boolean = true,
    onDrop: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnter: (state: DraggedItemState<T>) -> Unit = {},
    onDragExit: (state: DraggedItemState<T>) -> Unit = {},
    onDragStart: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnd: (state: DraggedItemState<T>) -> Unit = {},
    draggableContent: (@Composable () -> Unit)? = null,
    content: @Composable ReorderableItemScope.() -> Unit,
) {
    val reorderableItemScopeImpl = remember(key, state) {
        ReorderableItemScopeImpl(
            key = key,
            state = state,
        )
    }

    val reorderableItemScopeShadowImpl = remember(key) {
        ReorderableItemScopeShadowImpl(
            key = key,
        )
    }

    Box(
        modifier = modifier
            .draggableItemImpl(
                key = key,
                data = data,
                state = state,
                enabled = enabled,
                dragAfterLongPress = dragAfterLongPress,
                requireFirstDownUnconsumed = requireFirstDownUnconsumed,
                dropTargets = dropTargets,
                dropStrategy = dropStrategy,
                dragAxis = dragAxis,
                hasDragHandle = false,
                dragHandleOwner = reorderableItemScopeImpl,
                dropAnimationSpec = dropAnimationSpec,
                onDragStart = onDragStart,
                onDragEnd = onDragEnd,
                draggableContent = draggableContent ?: {
                    with(reorderableItemScopeShadowImpl) {
                        content()
                    }
                },
            ).dropTarget(
                key = key,
                state = state,
                zIndex = zIndex,
                dropAlignment = dropAlignment,
                dropOffset = dropOffset,
                dropAnimationEnabled = dropAnimationEnabled,
                canDrop = canDrop,
                onDrop = onDrop,
                onDragEnter = onDragEnter,
                onDragExit = onDragExit,
            ),
    ) {
        with(reorderableItemScopeImpl) {
            content()
        }
    }
}

/**
 * Mark this composable as a reorderable item.
 */
@Deprecated(
    message = "ReorderState is deprecated, pass a DragAndDropState instead.",
    replaceWith = ReplaceWith(
        "ReorderableItem(key = key, data = data, state = state.dndState, modifier = modifier, zIndex = zIndex, " +
            "enabled = enabled, dragAfterLongPress = dragAfterLongPress, " +
            "requireFirstDownUnconsumed = requireFirstDownUnconsumed, dropTargets = dropTargets, " +
            "dropStrategy = dropStrategy, dragAxis = dragAxis, onDrop = onDrop, onDragEnter = onDragEnter, " +
            "onDragExit = onDragExit, dropAnimationSpec = dropAnimationSpec, " +
            "draggableContent = draggableContent, content = content)",
    ),
)
@Composable
fun <T> ReorderableItem(
    modifier: Modifier = Modifier,
    state: ReorderState<T>,
    key: Any,
    data: T,
    zIndex: Float = 0f,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = state.dndState.dragAfterLongPress,
    requireFirstDownUnconsumed: Boolean = state.dndState.requireFirstDownUnconsumed,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.Surface,
    dragAxis: DragAxis = DragAxis.Free,
    onDrop: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnter: (state: DraggedItemState<T>) -> Unit = {},
    onDragExit: (state: DraggedItemState<T>) -> Unit = {},
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    draggableContent: (@Composable () -> Unit)? = null,
    content: @Composable ReorderableItemScope.() -> Unit,
) {
    ReorderableItem(
        modifier = modifier,
        state = state.dndState,
        key = key,
        data = data,
        zIndex = zIndex,
        enabled = enabled,
        dragAfterLongPress = dragAfterLongPress,
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        dropTargets = dropTargets,
        dropStrategy = dropStrategy,
        dragAxis = dragAxis,
        dropAnimationSpec = dropAnimationSpec,
        onDrop = onDrop,
        onDragEnter = onDragEnter,
        onDragExit = onDragExit,
        draggableContent = draggableContent,
        content = content,
    )
}
