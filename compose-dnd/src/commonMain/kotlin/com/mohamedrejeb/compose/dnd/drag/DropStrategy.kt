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

    object Surface: DropStrategy {
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

    object SurfacePercentage: DropStrategy {
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

    object CenterDistance: DropStrategy {
        override fun <T> getHoveredDropTarget(
            draggedItemTopLeft: Offset,
            draggedItemSize: Size,
            dropTargets: List<DropTargetState<T>>,
        ): DropTargetState<T>? =
            dropTargets
                .minByOrNull {
                    println("it.key: ${it.key}")
                    println("draggedItemTopLeft: $draggedItemTopLeft")
                    println("draggedItemSize: $draggedItemSize")
                    println("it.topLeft: ${it.topLeft}")
                    println("it.size: ${it.size}")
                    val p1 = Offset(
                        x = draggedItemTopLeft.x + draggedItemSize.width / 2f,
                        y = draggedItemTopLeft.y + draggedItemSize.height / 2f,
                    )
                    val p2 = Offset(
                        x = it.topLeft.x + it.size.width / 2f,
                        y = it.topLeft.y + it.size.height / 2f,
                    )

                    println("p1: $p1")
                    println("p2: $p2")

                    MathUtils.distance2(p1 = p1, p2 = p2).also {
                        println("distance2: $it")
                    }
                }
    }

}
