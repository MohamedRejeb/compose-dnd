package com.mohamedrejeb.compose.dnd.drag

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
}

internal class DraggableItemScopeImpl<T>(
    val state: DragAndDropState<T>,
    override val key: Any,
): DraggableItemScope {
    override val isDragging: Boolean
        get() = state.draggedItem?.key == key
}

internal class DraggableItemScopeShadowImpl(
    override val key: Any,
): DraggableItemScope {
    override val isDragging: Boolean
        get() = false
}
