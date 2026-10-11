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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot

internal class DraggableItemState<T>(
    var key: Any,
    var data: T,
    var dropTargets: List<Any> = emptyList(),
    var dropStrategy: DropStrategy,
    var dragAxis: DragAxis = DragAxis.Free,
    var hasDragHandle: Boolean = false,

    /** False when the item itself is disabled; a drag handle must not drag it either. */
    var enabled: Boolean = true,

    var dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),

    var positionInRoot: Offset,
    var size: Size,

    var dragHandleOwner: DragHandleOwner? = null,

    var onDragStart: (DraggedItemState<T>) -> Unit = {},
    var onDragEnd: (DraggedItemState<T>) -> Unit = {},

    var content: @Composable () -> Unit,
) {
    /**
     * True if only a drag handle starts the drag: either declared through
     * [hasDragHandle], or a scoped `dragHandle()` was applied in the content.
     */
    fun usesDragHandle(): Boolean = hasDragHandle || dragHandleOwner?.hasDragHandle == true

    // Live coordinates, positionInRoot alone goes stale while animateItem
    // is gliding the item because layer frames do not trigger onPlaced
    var layoutCoordinates: LayoutCoordinates? = null

    /**
     * Current top-left in root coordinates, read from the live layout
     * coordinates when attached so it is never stale after a reorder.
     */
    fun livePositionInRoot(): Offset =
        layoutCoordinates
            ?.takeIf { it.isAttached }
            ?.positionInRoot()
            ?: positionInRoot

    fun copy(): DraggableItemState<T> =
        DraggableItemState(
            key = key,
            data = data,
            dropTargets = dropTargets,
            dropStrategy = dropStrategy,
            dragAxis = dragAxis,
            hasDragHandle = hasDragHandle,
            enabled = enabled,
            dragHandleOwner = dragHandleOwner,
            dropAnimationSpec = dropAnimationSpec,
            positionInRoot = positionInRoot,
            size = size,
            onDragStart = onDragStart,
            onDragEnd = onDragEnd,
            content = content,
        )
}

/**
 * Scope of a composable draggable item whose content may apply a scoped
 * `dragHandle()` modifier. Read by the gesture detector at gesture time, since
 * the content composes after the item's own modifier.
 */
internal interface DragHandleOwner {
    val hasDragHandle: Boolean
}

/**
 * Counts the scoped `dragHandle()` modifiers currently in the composition, so
 * the item goes back to dragging as a whole once its handle is removed.
 */
internal class DragHandleCounter : DragHandleOwner {
    private var attachedHandles = 0

    override val hasDragHandle: Boolean
        get() = attachedHandles > 0

    fun onHandleAttached() {
        attachedHandles++
    }

    fun onHandleDetached() {
        attachedHandles--
    }
}
