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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DragAxis
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget

/**
 * Mark this composable as a reorderable item (both draggable and a drop target).
 *
 * This is a modifier-based alternative to [ReorderableItem] that reduces boilerplate.
 * It combines [draggableItem] and [dropTarget] into a single modifier.
 *
 * To check if the item is currently being dragged, use [DragAndDropState.isDragging]:
 * ```
 * val isDragging = state.isDragging(key)
 * ```
 *
 * @param key Unique key for this item.
 * @param data Data passed to callbacks.
 * @param state The drag and drop state.
 * @param enabled Whether drag is enabled for this item.
 * @param dragAfterLongPress If true, drag starts after long press; otherwise after press + slop.
 * @param requireFirstDownUnconsumed If true, first down event must be unconsumed.
 * @param dropTargets List of drop target keys this item can be dropped on. Empty = any target.
 * @param dropStrategy Strategy to determine which drop target receives the item.
 * @param dragAxis Constrain movement to [DragAxis.Free], [DragAxis.Horizontal], or [DragAxis.Vertical].
 * @param hasDragHandle If true, drag is initiated from a drag handle modifier instead of the whole item.
 * @param zIndex Z-index of the drop target for overlap resolution.
 * @param dropAnimationSpec Animation spec for position on drop.
 * @param sizeDropAnimationSpec Animation spec for size on drop.
 * @param dropAlignment Alignment of the dropped item within the target.
 * @param dropOffset Offset applied to the dropped item position.
 * @param dropAnimationEnabled Whether the drop animation is enabled.
 * @param onDrop Called when an item is dropped onto this target.
 * @param onDragEnter Called when an item is dragged over this target.
 * @param onDragExit Called when an item is dragged out of this target.
 * @param draggableContent Content rendered as the drag shadow/overlay while dragging.
 */
fun <T> Modifier.reorderableItem(
    key: Any,
    data: T,
    state: DragAndDropState<T>,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = state.dragAfterLongPress,
    requireFirstDownUnconsumed: Boolean = state.requireFirstDownUnconsumed,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    dragAxis: DragAxis = DragAxis.Free,
    hasDragHandle: Boolean = false,
    zIndex: Float = 0f,
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    dropAlignment: Alignment = Alignment.Center,
    dropOffset: Offset = Offset.Zero,
    dropAnimationEnabled: Boolean = true,
    onDrop: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnter: (state: DraggedItemState<T>) -> Unit = {},
    onDragExit: (state: DraggedItemState<T>) -> Unit = {},
    draggableContent: @Composable () -> Unit,
): Modifier =
    this
        .draggableItem(
            key = key,
            data = data,
            state = state,
            enabled = enabled,
            dragAfterLongPress = dragAfterLongPress,
            requireFirstDownUnconsumed = requireFirstDownUnconsumed,
            dropTargets = dropTargets,
            dropStrategy = dropStrategy,
            dragAxis = dragAxis,
            hasDragHandle = hasDragHandle,
            dropAnimationSpec = dropAnimationSpec,
            sizeDropAnimationSpec = sizeDropAnimationSpec,
            draggableContent = draggableContent,
        ).dropTarget(
            key = key,
            state = state,
            zIndex = zIndex,
            dropAlignment = dropAlignment,
            dropOffset = dropOffset,
            dropAnimationEnabled = dropAnimationEnabled,
            onDrop = onDrop,
            onDragEnter = onDragEnter,
            onDragExit = onDragExit,
        )
