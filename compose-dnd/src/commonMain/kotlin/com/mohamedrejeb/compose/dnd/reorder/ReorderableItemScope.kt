package com.mohamedrejeb.compose.dnd.reorder

import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItemScope

interface ReorderableItemScope: DraggableItemScope

internal class ReorderableItemScopeImpl<T>(
    val state: DragAndDropState<T>,
    override val key: Any,
): ReorderableItemScope {
    override val isDragging: Boolean
        get() = state.draggedItem?.key == key
}

internal class ReorderableItemScopeShadowImpl(
    override val key: Any,
): ReorderableItemScope {
    override val isDragging: Boolean
        get() = false
}
