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
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
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
 * Apply it to any composable inside a
 * [DragAndDropContainer][com.mohamedrejeb.compose.dnd.DragAndDropContainer] to make it
 * draggable. While it is dragged, [draggableContent] is drawn as the drag shadow that
 * follows the pointer; the item itself stays in place, so hide it or show a placeholder
 * using [DragAndDropState.isDragging]:
 * ```
 * Modifier
 *     .graphicsLayer { alpha = if (state.isDragging(key)) 0f else 1f }
 *     .draggableItem(key = key, data = data, state = state) { ItemCard(data) }
 * ```
 *
 * The item's configuration ([dropTargets], [dropStrategy], [dragAxis], [dropAnimationSpec] and
 * [draggableContent]) is captured when a drag starts and applies until that drag ends.
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
 * @param onDragStart Called when a drag of this item starts.
 * @param onDragEnd Called once the drag of this item is over: after the drop animation and the
 * drop target's `onDrop` when dropped, or after the return animation when canceled or released
 * outside any target. A good place to persist a reordered list.
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
    onDragStart: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnd: (state: DraggedItemState<T>) -> Unit = {},
    draggableContent: @Composable () -> Unit,
): Modifier =
    draggableItemImpl(
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
        dragHandleOwner = null,
        dropAnimationSpec = dropAnimationSpec,
        onDragStart = onDragStart,
        onDragEnd = onDragEnd,
        draggableContent = draggableContent,
    )

internal fun <T> Modifier.draggableItemImpl(
    key: Any,
    data: T,
    state: DragAndDropState<T>,
    enabled: Boolean,
    dragAfterLongPress: Boolean,
    requireFirstDownUnconsumed: Boolean,
    dropTargets: List<Any>,
    dropStrategy: DropStrategy,
    dragAxis: DragAxis,
    hasDragHandle: Boolean,
    dragHandleOwner: DragHandleOwner?,
    dropAnimationSpec: AnimationSpec<Offset>,
    onDragStart: (state: DraggedItemState<T>) -> Unit,
    onDragEnd: (state: DraggedItemState<T>) -> Unit,
    draggableContent: @Composable () -> Unit,
): Modifier =
    this then DraggableItemNodeElement(
        key = key,
        data = data,
        state = state,
        enabled = enabled,
        stateEnabled = state.enabled,
        dragAfterLongPress = dragAfterLongPress,
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        dropTargets = dropTargets,
        dropStrategy = dropStrategy,
        dragAxis = dragAxis,
        hasDragHandle = hasDragHandle,
        dragHandleOwner = dragHandleOwner,
        dropAnimationSpec = dropAnimationSpec,
        onDragStart = onDragStart,
        onDragEnd = onDragEnd,
        draggableContent = draggableContent,
    )

/**
 * Returns true if an item with the given [key] is currently being dragged.
 */
@Deprecated(
    message = "Use the DragAndDropState.isDragging member function, no import needed.",
    level = DeprecationLevel.WARNING,
)
fun <T> DragAndDropState<T>.isDragging(key: Any): Boolean =
    draggedItem?.key == key

// -- Internal implementation --

private data class DraggableItemNodeElement<T>(
    val key: Any,
    val data: T,
    val state: DragAndDropState<T>,
    val enabled: Boolean,
    val stateEnabled: Boolean,
    val dragAfterLongPress: Boolean,
    val requireFirstDownUnconsumed: Boolean,
    val dropTargets: List<Any>,
    val dropStrategy: DropStrategy,
    val dragAxis: DragAxis,
    val hasDragHandle: Boolean,
    val dragHandleOwner: DragHandleOwner?,
    val dropAnimationSpec: AnimationSpec<Offset>,
    val onDragStart: (state: DraggedItemState<T>) -> Unit,
    val onDragEnd: (state: DraggedItemState<T>) -> Unit,
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
                dragHandleOwner = dragHandleOwner,
                dropAnimationSpec = dropAnimationSpec,
                onDragStart = onDragStart,
                onDragEnd = onDragEnd,
                content = draggableContent,
            ),
            state = state,
            enabled = enabled && stateEnabled,
            dragAfterLongPress = dragAfterLongPress,
            requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        )

    override fun update(node: DraggableItemNode<T>) {
        val itemState = node.draggableItemState
        val oldKey = itemState.key
        val oldState = node.state
        val gestureChanged =
            oldKey != key ||
                oldState != state ||
                node.enabled != (enabled && stateEnabled) ||
                node.dragAfterLongPress != dragAfterLongPress ||
                node.requireFirstDownUnconsumed != requireFirstDownUnconsumed

        node.state = state
        node.enabled = enabled && stateEnabled
        node.dragAfterLongPress = dragAfterLongPress
        node.requireFirstDownUnconsumed = requireFirstDownUnconsumed

        itemState.key = key
        itemState.data = data
        itemState.dropTargets = dropTargets
        itemState.dropStrategy = dropStrategy
        itemState.dragAxis = dragAxis
        itemState.hasDragHandle = hasDragHandle
        itemState.dragHandleOwner = dragHandleOwner
        itemState.dropAnimationSpec = dropAnimationSpec
        itemState.onDragStart = onDragStart
        itemState.onDragEnd = onDragEnd
        itemState.content = draggableContent

        if (oldKey != key || oldState != state) {
            node.moveRegistration(oldKey, oldState)
        }
        if (gestureChanged) {
            node.resetGesture()
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "draggableItem"
        properties["key"] = key
        properties["data"] = data
        properties["state"] = state
        properties["enabled"] = enabled
        properties["dragAfterLongPress"] = dragAfterLongPress
        properties["requireFirstDownUnconsumed"] = requireFirstDownUnconsumed
        properties["dropTargets"] = dropTargets
        properties["dropStrategy"] = dropStrategy
        properties["dragAxis"] = dragAxis
        properties["hasDragHandle"] = hasDragHandle
        properties["dropAnimationSpec"] = dropAnimationSpec
    }
}

private class DraggableItemNode<T>(
    val draggableItemState: DraggableItemState<T>,
    var state: DragAndDropState<T>,
    var enabled: Boolean,
    var dragAfterLongPress: Boolean,
    var requireFirstDownUnconsumed: Boolean,
) : DelegatingNode(),
    LayoutAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    private val key get() = draggableItemState.key

    private var isShadow = false

    private val pointerInputNode = delegate(
        SuspendingPointerInputModifierNode {
            if (isShadow) return@SuspendingPointerInputModifierNode
            detectDragStartGesture(
                key = key,
                state = state,
                draggableItemState = draggableItemState,
                enabled = enabled,
                dragAfterLongPress = dragAfterLongPress,
                requireFirstDownUnconsumed = requireFirstDownUnconsumed,
            )
        },
    )

    fun resetGesture() {
        pointerInputNode.resetPointerInputHandler()
    }

    /**
     * Re-register under the current key/state after either changed, e.g. when
     * a lazy layout reuses this node for another item.
     */
    fun moveRegistration(oldKey: Any, oldState: DragAndDropState<T>) {
        if (!isAttached || isShadow) return

        oldState.removeDraggableItem(oldKey, draggableItemState)
        state.addDraggableItem(draggableItemState)
    }

    override fun onAttach() {
        isShadow = currentValueOf(LocalDragAndDropInfo).isShadow

        if (isShadow) return

        state.addDraggableItem(draggableItemState)
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        if (isShadow) return

        state.addDraggableItem(draggableItemState)

        draggableItemState.layoutCoordinates = coordinates
        draggableItemState.positionInRoot = coordinates.positionInRoot()
        draggableItemState.size = coordinates.size.toSize()
    }

    override fun onRemeasured(size: IntSize) {
        if (isShadow) return

        draggableItemState.size = size.toSize()
    }

    override fun onReset() {
        if (isShadow) return

        state.removeDraggableItem(key, draggableItemState)
    }

    override fun onDetach() {
        if (isShadow) return

        state.removeDraggableItem(key, draggableItemState)
    }
}
