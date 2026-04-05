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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.LocalDragAndDropInfo
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture

/**
 * Mark this composable as a draggable item.
 *
 * This is a modifier-based alternative to [DraggableItem] that reduces boilerplate.
 * Apply it to any composable to make it draggable.
 *
 * To check if the item is currently being dragged, use [DragAndDropState.isDragging]:
 * ```
 * val isDragging = state.isDragging(key)
 * ```
 *
 * @param key Unique key for this draggable item.
 * @param data Data passed to the drop target on drop.
 * @param state The drag and drop state.
 * @param enabled Whether drag is enabled for this item.
 * @param dragAfterLongPress If true, drag starts after long press; otherwise after press + slop.
 * @param requireFirstDownUnconsumed If true, first down event must be unconsumed.
 * @param dropTargets List of drop target keys this item can be dropped on. Empty = any target.
 * @param dropStrategy Strategy to determine which drop target receives the item.
 * @param dragAxis Constrain movement to [DragAxis.Free], [DragAxis.Horizontal], or [DragAxis.Vertical].
 * @param hasDragHandle If true, drag is initiated from a [dragHandle] modifier instead of the whole item.
 * @param dropAnimationSpec Animation spec for position on drop.
 * @param sizeDropAnimationSpec Animation spec for size on drop.
 * @param draggableContent Content rendered as the drag shadow/overlay while dragging.
 */
fun <T> Modifier.draggableItem(
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
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    draggableContent: @Composable () -> Unit,
): Modifier =
    this
        .then(
            DraggableItemNodeElement(
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
            )
        ).pointerInput(
            key,
            enabled,
            state,
            state.enabled,
            dragAfterLongPress,
            requireFirstDownUnconsumed,
        ) {
            if (!enabled || !state.enabled) return@pointerInput
            val draggableItemState = state.draggableItemMap[key] ?: return@pointerInput
            detectDragStartGesture(
                key = key,
                state = state,
                draggableItemState = draggableItemState,
                enabled = true,
                dragAfterLongPress = dragAfterLongPress,
                requireFirstDownUnconsumed = requireFirstDownUnconsumed,
            )
        }

/**
 * Returns true if an item with the given [key] is currently being dragged.
 */
fun <T> DragAndDropState<T>.isDragging(key: Any): Boolean =
    draggedItem?.key == key

// -- Internal implementation --

private data class DraggableItemNodeElement<T>(
    val key: Any,
    val data: T,
    val state: DragAndDropState<T>,
    val enabled: Boolean,
    val dragAfterLongPress: Boolean,
    val requireFirstDownUnconsumed: Boolean,
    val dropTargets: List<Any>,
    val dropStrategy: DropStrategy,
    val dragAxis: DragAxis,
    val hasDragHandle: Boolean,
    val dropAnimationSpec: AnimationSpec<Offset>,
    val sizeDropAnimationSpec: AnimationSpec<Size>,
    val draggableContent: @Composable () -> Unit,
) : ModifierNodeElement<DraggableItemNode<T>>() {

    override fun create(): DraggableItemNode<T> =
        DraggableItemNode(
            draggableItemState = DraggableItemState(
                key = key,
                data = data,
                positionInRoot = Offset.Zero,
                size = Size.Zero,
                dropTargets = dropTargets,
                dropStrategy = dropStrategy,
                dragAxis = dragAxis,
                hasDragHandle = hasDragHandle,
                dropAnimationSpec = dropAnimationSpec,
                sizeDropAnimationSpec = sizeDropAnimationSpec,
                content = draggableContent,
            ),
            state = state,
            enabled = enabled,
            dragAfterLongPress = dragAfterLongPress,
            requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        )

    override fun update(node: DraggableItemNode<T>) {
        node.apply {
            val isKeyChanged = draggableItemState.key != key

            this.state = state
            this.enabled = enabled
            this.dragAfterLongPress = dragAfterLongPress
            this.requireFirstDownUnconsumed = requireFirstDownUnconsumed

            draggableItemState.key = key
            draggableItemState.data = data
            draggableItemState.dropTargets = dropTargets
            draggableItemState.dropStrategy = dropStrategy
            draggableItemState.dragAxis = dragAxis
            draggableItemState.hasDragHandle = hasDragHandle
            draggableItemState.dropAnimationSpec = dropAnimationSpec
            draggableItemState.sizeDropAnimationSpec = sizeDropAnimationSpec
            draggableItemState.content = draggableContent

            if (isKeyChanged) {
                onDetach()
                onAttach()
            }
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "draggableItem"
        properties["key"] = key
        properties["state"] = state
        properties["enabled"] = enabled
        properties["dragAfterLongPress"] = dragAfterLongPress
        properties["dropTargets"] = dropTargets
        properties["dropStrategy"] = dropStrategy
        properties["dragAxis"] = dragAxis
        properties["hasDragHandle"] = hasDragHandle
    }
}

private class DraggableItemNode<T>(
    val draggableItemState: DraggableItemState<T>,
    var state: DragAndDropState<T>,
    var enabled: Boolean,
    var dragAfterLongPress: Boolean,
    var requireFirstDownUnconsumed: Boolean,
) : Modifier.Node(),
    LayoutAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    private val key get() = draggableItemState.key

    private var isShadow = false

    override fun onAttach() {
        isShadow = currentValueOf(LocalDragAndDropInfo).isShadow

        if (isShadow) return

        state.addDraggableItem(draggableItemState)
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        if (isShadow) return

        state.addDraggableItem(draggableItemState)

        draggableItemState.positionInRoot = coordinates.positionInRoot()
        draggableItemState.size = coordinates.size.toSize()
    }

    override fun onRemeasured(size: IntSize) {
        if (isShadow) return

        draggableItemState.size = size.toSize()
    }

    override fun onReset() {
        if (isShadow) return

        state.removeDraggableItem(key)
    }

    override fun onDetach() {
        if (isShadow) return

        state.removeDraggableItem(key)
    }
}
