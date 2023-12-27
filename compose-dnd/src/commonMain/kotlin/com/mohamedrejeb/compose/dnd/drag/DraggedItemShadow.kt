package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
                with (density) {
                    state.currentDraggableItem?.size?.toDpSize() ?: DpSize.Zero
                }
            )
            .onGloballyPositioned {
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
