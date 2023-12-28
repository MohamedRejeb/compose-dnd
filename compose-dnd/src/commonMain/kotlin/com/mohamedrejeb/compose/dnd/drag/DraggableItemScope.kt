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
) : DraggableItemScope {
    override val isDragging: Boolean
        get() = state.draggedItem?.key == key
}

internal class DraggableItemScopeShadowImpl(
    override val key: Any,
) : DraggableItemScope {
    override val isDragging: Boolean
        get() = false
}
