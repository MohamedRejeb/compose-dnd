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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import com.mohamedrejeb.compose.dnd.DragAndDropState

@Composable
internal fun <T> DraggedItemShadow(
    state: DragAndDropState<T>,
) {
    val density = LocalDensity.current
    val draggedItemPositionInRoot = remember {
        mutableStateOf(Offset.Zero)
    }

    Box(
        modifier = Modifier
            .size(
                with(density) {
                    state.dragSizeAnimatable.value.toDpSize()
                }
            )
            .onPlaced {
                draggedItemPositionInRoot.value = it.positionInRoot()
            }
            .graphicsLayer {
                val dragPositionX = state.dragPositionAnimatable.value.x + state.dragPosition.value.x
                val dragPositionY = state.dragPositionAnimatable.value.y + state.dragPosition.value.y
                translationX = dragPositionX - draggedItemPositionInRoot.value.x
                translationY = dragPositionY - draggedItemPositionInRoot.value.y
            },
    ) {
        state.currentDraggableItem?.content?.invoke()
    }
}
