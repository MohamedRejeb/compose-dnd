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
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture
import kotlinx.coroutines.delay

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
    var handlePositionInRoot: Offset = Offset.Zero

    return Modifier
        .onPlaced { handlePositionInRoot = it.positionInRoot() }
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
                gestureSourcePositionInRoot = { handlePositionInRoot },
            )
        }
}
