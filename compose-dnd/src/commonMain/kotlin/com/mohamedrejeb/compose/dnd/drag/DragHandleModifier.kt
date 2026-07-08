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

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture
import kotlinx.coroutines.delay

/**
 * Marks this composable as the drag handle for the draggable item identified by [key].
 *
 * Use with the [draggableItem] or
 * [reorderableItem][com.mohamedrejeb.compose.dnd.reorder.reorderableItem] modifiers:
 * pass `hasDragHandle = true` to the item modifier, then apply this modifier to the handle
 * composable. Only the handle region initiates drag — the rest of the item remains
 * interactive (clickable, scrollable, etc.).
 *
 * Inside [DraggableItem] or [ReorderableItem][com.mohamedrejeb.compose.dnd.reorder.ReorderableItem]
 * content, use the scope's `Modifier.dragHandle()` instead — it does not need [key] and [state].
 *
 * @param key The key of the draggable item this handle controls.
 * @param state The drag and drop state.
 * @param enabled Whether the drag handle is active.
 * @param dragAfterLongPress If true, drag starts after long press on the handle.
 * @param requireFirstDownUnconsumed If true, the first down event must be unconsumed.
 */
fun <T> Modifier.dragHandle(
    key: Any,
    state: DragAndDropState<T>,
    enabled: Boolean = true,
    dragAfterLongPress: Boolean = false,
    requireFirstDownUnconsumed: Boolean = false,
): Modifier = this then DragHandleModifier(
    key = key,
    state = state,
    enabled = enabled,
    dragAfterLongPress = dragAfterLongPress,
    requireFirstDownUnconsumed = requireFirstDownUnconsumed,
)

/**
 * Creates a modifier that attaches drag gesture detection to a handle region.
 * Tracks the handle's own root position so the drag offset is computed correctly
 * relative to where the user touched, not the item's top-left.
 */
internal fun <T> DragHandleModifier(
    key: Any,
    state: DragAndDropState<T>,
    enabled: Boolean,
    dragAfterLongPress: Boolean,
    requireFirstDownUnconsumed: Boolean,
): Modifier {
    // Keep the coordinates and resolve the position at gesture time. A cached
    // offset goes stale after a reorder because onPlaced does not fire again
    // when only the item moved within the list.
    var handleCoordinates: LayoutCoordinates? = null

    return Modifier
        .onPlaced { handleCoordinates = it }
        .pointerInput(
            key,
            enabled,
            state,
            state.enabled,
            dragAfterLongPress,
            requireFirstDownUnconsumed,
        ) {
            // Wait for the DraggableItemState to be registered by DisposableEffect
            var draggableItemState: DraggableItemState<T>? = null
            while (draggableItemState == null) {
                draggableItemState = state.draggableItemMap[key]
                if (draggableItemState == null) {
                    delay(1)
                }
            }

            detectDragStartGesture(
                key = key,
                state = state,
                draggableItemState = draggableItemState,
                enabled = enabled && state.enabled,
                dragAfterLongPress = dragAfterLongPress,
                requireFirstDownUnconsumed = requireFirstDownUnconsumed,
                isHandle = true,
                gestureSourcePositionInRoot = {
                    handleCoordinates
                        ?.takeIf { it.isAttached }
                        ?.positionInRoot()
                        ?: Offset.Zero
                },
            )
        }
}
