package com.mohamedrejeb.compose.dnd.drop

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState

internal class DropTargetState<T>(
    val key: Any,
    val zIndex: Float,

    var size: Size,
    var topLeft: Offset,

    var dropAlignment: Alignment,
    var dropOffset: Offset,
    var dropAnimationEnabled: Boolean,

    val onDrop: (state: DraggedItemState<T>) -> Unit,
    val onDragEnter: (state: DraggedItemState<T>) -> Unit,
    val onDragExit: (state: DraggedItemState<T>) -> Unit,
) {
    fun getDropTopLeft(droppedItemSize: Size): Offset =
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
