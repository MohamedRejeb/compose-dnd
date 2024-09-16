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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.compose.dnd.drop.DropTargetState
import com.mohamedrejeb.compose.dnd.utils.MathUtils

interface DropStrategy {

    fun <T> getHoveredDropTarget(
        draggedItemTopLeft: Offset,
        draggedItemSize: Size,
        dropTargets: List<DropTargetState<T>>,
    ): DropTargetState<T>?

    object Surface : DropStrategy {
        override fun <T> getHoveredDropTarget(
            draggedItemTopLeft: Offset,
            draggedItemSize: Size,
            dropTargets: List<DropTargetState<T>>,
        ): DropTargetState<T>? =
            dropTargets
                .maxByOrNull {
                    val maxOverlappingArea = draggedItemSize.width * draggedItemSize.height

                    MathUtils.overlappingArea(
                        topLeft1 = draggedItemTopLeft,
                        size1 = draggedItemSize,
                        topLeft2 = it.topLeft,
                        size2 = it.size,
                    ) + it.zIndex * maxOverlappingArea
                }
    }

    object SurfacePercentage : DropStrategy {
        override fun <T> getHoveredDropTarget(
            draggedItemTopLeft: Offset,
            draggedItemSize: Size,
            dropTargets: List<DropTargetState<T>>,
        ): DropTargetState<T>? =
            dropTargets
                .maxByOrNull {
                    val maxOverlappingArea = draggedItemSize.width * draggedItemSize.height

                    MathUtils.overlappingArea(
                        topLeft1 = draggedItemTopLeft,
                        size1 = draggedItemSize,
                        topLeft2 = it.topLeft,
                        size2 = it.size,
                    ) / maxOverlappingArea + it.zIndex
                }
    }

    object CenterDistance : DropStrategy {
        override fun <T> getHoveredDropTarget(
            draggedItemTopLeft: Offset,
            draggedItemSize: Size,
            dropTargets: List<DropTargetState<T>>,
        ): DropTargetState<T>? =
            dropTargets
                .minByOrNull {
                    val p1 = Offset(
                        x = draggedItemTopLeft.x + draggedItemSize.width / 2f,
                        y = draggedItemTopLeft.y + draggedItemSize.height / 2f,
                    )
                    val p2 = Offset(
                        x = it.topLeft.x + it.size.width / 2f,
                        y = it.topLeft.y + it.size.height / 2f,
                    )

                    MathUtils.distance2(p1 = p1, p2 = p2)
                }
    }
}
