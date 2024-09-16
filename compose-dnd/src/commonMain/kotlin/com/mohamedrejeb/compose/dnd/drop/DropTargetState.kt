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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState

class DropTargetState<T> internal constructor(
    key: Any,
    zIndex: Float,

    size: Size,
    topLeft: Offset,

    internal var dropAlignment: Alignment,
    internal var dropOffset: Offset,
    internal var dropAnimationEnabled: Boolean,

    internal var onDrop: (state: DraggedItemState<T>) -> Unit,
    internal var onDragEnter: (state: DraggedItemState<T>) -> Unit,
    internal var onDragExit: (state: DraggedItemState<T>) -> Unit,
) {
    var key: Any = key
        internal set

    var zIndex: Float = zIndex
        internal set

    var size: Size = size
        internal set

    var topLeft: Offset = topLeft
        internal set

    internal fun getDropTopLeft(droppedItemSize: Size): Offset =
        topLeft + dropOffset + when (dropAlignment) {
            Alignment.TopStart ->
                Offset.Zero

            Alignment.TopCenter ->
                Offset(
                    x = size.width / 2f - droppedItemSize.width / 2f,
                    y = 0f,
                )

            Alignment.TopEnd ->
                Offset(
                    x = size.width - droppedItemSize.width,
                    y = 0f,
                )

            Alignment.CenterStart ->
                Offset(
                    x = 0f,
                    y = size.height / 2f - droppedItemSize.height / 2f,
                )

            Alignment.Center ->
                Offset(
                    x = size.width / 2f - droppedItemSize.width / 2f,
                    y = size.height / 2f - droppedItemSize.height / 2f,
                )

            Alignment.CenterEnd ->
                Offset(
                    x = size.width - droppedItemSize.width,
                    y = size.height / 2f - droppedItemSize.height / 2f,
                )

            Alignment.BottomStart ->
                Offset(
                    x = 0f,
                    y = size.height - droppedItemSize.height,
                )

            Alignment.BottomCenter ->
                Offset(
                    x = size.width / 2f - droppedItemSize.width / 2f,
                    y = size.height - droppedItemSize.height,
                )

            Alignment.BottomEnd ->
                Offset(
                    x = size.width - droppedItemSize.width,
                    y = size.height - droppedItemSize.height,
                )

            else ->
                Offset.Zero
        }
}
