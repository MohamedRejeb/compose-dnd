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
package com.mohamedrejeb.compose.dnd.drop

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState

/**
 * Mark this composable as a drop target.
 *
 * @param key The key used to identify the drop target.
 * @param zIndex The z-index of the drop target.
 * @param state The drag and drop state.
 * @param dropAlignment The alignment of the dropped item.
 * @param dropOffset The offset of the dropped item.
 * @param onDrop The action to perform when an item is dropped onto the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragEnter The action to perform when an item is dragged over the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragExit The action to perform when an item is dragged out of the target.
 * Accepts the dragged item state as a parameter.
 */
fun <T> Modifier.dropTarget(
    key: Any,
    state: DragAndDropState<T>,
    zIndex: Float = 0f,
    dropAlignment: Alignment = Alignment.Center,
    dropOffset: Offset = Offset.Zero,
    dropAnimationEnabled: Boolean = true,
    onDrop: (state: DraggedItemState<T>) -> Unit = {},
    onDragEnter: (state: DraggedItemState<T>) -> Unit = {},
    onDragExit: (state: DraggedItemState<T>) -> Unit = {},
): Modifier = this then DropTargetNodeElement(
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

private data class DropTargetNodeElement<T>(
    val key: Any,
    val state: DragAndDropState<T>,
    val zIndex: Float,
    val dropAlignment: Alignment,
    val dropOffset: Offset,
    val dropAnimationEnabled: Boolean,
    val onDrop: (state: DraggedItemState<T>) -> Unit,
    val onDragEnter: (state: DraggedItemState<T>) -> Unit,
    val onDragExit: (state: DraggedItemState<T>) -> Unit,
) : ModifierNodeElement<DropTargetNode<T>>() {
    override fun create(): DropTargetNode<T> = DropTargetNode(
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

    override fun update(node: DropTargetNode<T>) {
        node.key = key
        node.state = state
        node.zIndex = zIndex
        node.dropAlignment = dropAlignment
        node.dropOffset = dropOffset
        node.dropAnimationEnabled = dropAnimationEnabled
        node.onDrop = onDrop
        node.onDragEnter = onDragEnter
        node.onDragExit = onDragExit
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "DropTarget"
        properties["key"] = key
        properties["zIndex"] = zIndex
        properties["dropAlignment"] = dropAlignment
        properties["dropOffset"] = dropOffset
        properties["dropAnimationEnabled"] = dropAnimationEnabled
    }
}

private data class DropTargetNode<T>(
    var key: Any,
    var state: DragAndDropState<T>,
    var zIndex: Float,
    var dropAlignment: Alignment,
    var dropOffset: Offset,
    var dropAnimationEnabled: Boolean,
    var onDrop: (state: DraggedItemState<T>) -> Unit,
    var onDragEnter: (state: DraggedItemState<T>) -> Unit,
    var onDragExit: (state: DraggedItemState<T>) -> Unit,
) : Modifier.Node(), LayoutAwareModifierNode {
    override fun onPlaced(coordinates: LayoutCoordinates) {
        val size = coordinates.size.toSize()
        val topLeft = coordinates.positionInRoot()
        val dropTargetState = DropTargetState(
            key = key,
            zIndex = zIndex,
            size = size,
            topLeft = topLeft,
            dropAlignment = dropAlignment,
            dropOffset = dropOffset,
            dropAnimationEnabled = dropAnimationEnabled,
            onDrop = onDrop,
            onDragEnter = onDragEnter,
            onDragExit = onDragExit,
        )
        state.addDropTarget(dropTargetState)
    }

    override fun onReset() {
        state.removeDropTarget(key)
    }

    override fun onDetach() {
        state.removeDropTarget(key)
    }
}
