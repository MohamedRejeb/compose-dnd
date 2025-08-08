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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture

@Composable
fun <T> Modifier.draggable(
    key: Any,
    data: T,
    state: DragAndDropState<T>,
    enabled: Boolean = true,
    hideOnDrag: Boolean = true,
    dragAfterLongPress: Boolean = state.dragAfterLongPress,
    requireFirstDownUnconsumed: Boolean = state.requireFirstDownUnconsumed,
    dropTargets: List<Any> = emptyList(),
    dropStrategy: DropStrategy = DropStrategy.SurfacePercentage,
    dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),
    draggableContent: (@Composable () -> Unit)? = null,
): Modifier {
    val graphicsLayer = rememberGraphicsLayer()

    val draggableItemState = remember {
        DraggableItemState(
            key = key,
            data = data,
            dropTargets = dropTargets,
            dropStrategy = dropStrategy,
            dropAnimationSpec = dropAnimationSpec,
            sizeDropAnimationSpec = sizeDropAnimationSpec,
            positionInRoot = Offset.Zero,
            size = Size.Zero,
            content = draggableContent ?: {},
        )
    }

    return (this then DraggableNodeElement(
        key = key,
        data = data,
        state = state,
        draggableItemState = draggableItemState,
        enabled = enabled,
        dragAfterLongPress = dragAfterLongPress,
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        dropTargets = dropTargets,
        dropStrategy = dropStrategy,
        dropAnimationSpec = dropAnimationSpec,
        sizeDropAnimationSpec = sizeDropAnimationSpec,
        draggableContent = draggableContent,
    )).pointerInput(
        key,
        enabled,
        state,
        state.enabled,
        draggableItemState,
        dragAfterLongPress,
        requireFirstDownUnconsumed,
    ) {
        detectDragStartGesture(
            key = key,
            state = state,
            draggableItemState = draggableItemState,
            graphicsLayer = graphicsLayer,
            enabled = enabled && state.enabled,
            dragAfterLongPress = dragAfterLongPress,
            requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        )
    }
        .drawWithContent {
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }

            drawLayer(graphicsLayer)
        }
        .graphicsLayer {
            alpha = if (hideOnDrag && state.draggedItem?.key == key) 0f else 1f
        }
}

private data class DraggableNodeElement<T>(
    val key: Any,
    val data: T,
    val state: DragAndDropState<T>,
    val draggableItemState: DraggableItemState<T>,
    val enabled: Boolean,
    val dragAfterLongPress: Boolean,
    val requireFirstDownUnconsumed: Boolean,
    val dropTargets: List<Any>,
    val dropStrategy: DropStrategy,
    val dropAnimationSpec: AnimationSpec<Offset>,
    val sizeDropAnimationSpec: AnimationSpec<Size>,
    val draggableContent: (@Composable () -> Unit)?,
) : ModifierNodeElement<DraggableNode<T>>() {
    override fun create(): DraggableNode<T> =
        DraggableNode(
            draggableItemState = draggableItemState,
            state = state,
        )

    override fun update(node: DraggableNode<T>) {
        node.apply {
            this.state = state

            val isKeyChanged = draggableItemState.key != key

            draggableItemState.key = key
            draggableItemState.data = data
            draggableItemState.dropTargets = dropTargets
            draggableItemState.dropStrategy = dropStrategy
            draggableItemState.dropAnimationSpec = dropAnimationSpec
            draggableItemState.sizeDropAnimationSpec = sizeDropAnimationSpec

            if (isKeyChanged) {
                onDetach()
                onAttach()
            }
        }
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "Draggable"
        properties["key"] = key
        properties["data"] = data
        properties["state"] = state
        properties["enabled"] = enabled
        properties["dragAfterLongPress"] = dragAfterLongPress
        properties["requireFirstDownUnconsumed"] = requireFirstDownUnconsumed
        properties["dropTargets"] = dropTargets
        properties["dropStrategy"] = dropStrategy
        properties["dropAnimationSpec"] = dropAnimationSpec
        properties["sizeDropAnimationSpec"] = sizeDropAnimationSpec
        properties["draggableContent"] = draggableContent
    }
}

private data class DraggableNode<T>(
    val draggableItemState: DraggableItemState<T>,
    var state: DragAndDropState<T>,
) : Modifier.Node(),
    LayoutAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    private val key get() = draggableItemState.key

    override fun onAttach() {
        state.addDraggableItem(draggableItemState)
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        state.addDraggableItem(draggableItemState)

        val size = coordinates.size.toSize()
        val topLeft = coordinates.positionInRoot()

        draggableItemState.size = size
        draggableItemState.positionInRoot = topLeft
    }

    override fun onRemeasured(size: IntSize) {
        draggableItemState.size = size.toSize()
    }

    override fun onReset() {
        state.removeDropTarget(key)
    }

    override fun onDetach() {
        state.removeDropTarget(key)
    }

}
