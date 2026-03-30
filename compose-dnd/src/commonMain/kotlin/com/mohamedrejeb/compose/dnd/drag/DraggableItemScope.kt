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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mohamedrejeb.compose.dnd.DragAndDropState

/**
 * Draggable item scope
 *
 * @property key The key used to identify the item.
 * @property isDragging True if the item is currently being dragged.
 */
interface DraggableItemScope {
    val key: Any
    val isDragging: Boolean

    /**
     * Mark this composable as the drag handle for the item.
     * When a drag handle is used, only the handle region initiates drag — the rest of the item
     * remains interactive (clickable, scrollable, etc.).
     *
     * @param enabled Whether the drag handle is active.
     * @param dragAfterLongPress If true, drag starts after long press on the handle.
     * @param requireFirstDownUnconsumed If true, the first down event must be unconsumed.
     */
    fun Modifier.dragHandle(
        enabled: Boolean = true,
        dragAfterLongPress: Boolean = false,
        requireFirstDownUnconsumed: Boolean = false,
    ): Modifier
}

internal class DraggableItemScopeImpl<T>(
    val state: DragAndDropState<T>,
    override val key: Any,
) : DraggableItemScope {
    override val isDragging: Boolean
        get() = state.draggedItem?.key == key

    internal var hasDragHandle by mutableStateOf(false)
    internal var draggableItemState: DraggableItemState<T>? = null

    override fun Modifier.dragHandle(
        enabled: Boolean,
        dragAfterLongPress: Boolean,
        requireFirstDownUnconsumed: Boolean,
    ): Modifier {
        hasDragHandle = true
        draggableItemState?.hasDragHandle = true
        return DragHandleModifier(
            key = key,
            state = state,
            enabled = enabled,
            dragAfterLongPress = dragAfterLongPress,
            requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        ).let { this then it }
    }
}

internal class DraggableItemScopeShadowImpl(
    override val key: Any,
) : DraggableItemScope {
    override val isDragging: Boolean
        get() = false

    override fun Modifier.dragHandle(
        enabled: Boolean,
        dragAfterLongPress: Boolean,
        requireFirstDownUnconsumed: Boolean,
    ): Modifier = this
}
