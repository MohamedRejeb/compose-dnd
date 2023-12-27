package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi

/**
 * Remember [ReorderState]
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * This parameter is applied to all [ReorderableItem]s. If you want to change it for a specific item, use [ReorderableItem] parameter.
 * @return [ReorderState]
 */
@Composable
fun <T> rememberReorderState(
    dragAfterLongPress: Boolean = false,
): ReorderState<T> {
    return remember {
        ReorderState(
            dragAfterLongPress = dragAfterLongPress,
        )
    }
}

/**
 * State of the reorder
 *
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * This parameter is applied to all [ReorderableItem]s. If you want to change it for a specific item, use [ReorderableItem] parameter.
 */
@Stable
class ReorderState<T>(
    dragAfterLongPress: Boolean = false,
) {
    /**
     * State of the drag and drop
     */
    @ExperimentalDndApi
    val dndState = DragAndDropState<T>(
        dragAfterLongPress = dragAfterLongPress,
    )

    @OptIn(ExperimentalDndApi::class)
    val draggedItem get() = dndState.draggedItem

    @OptIn(ExperimentalDndApi::class)
    val hoveredDropTargetKey get() = dndState.hoveredDropTargetKey
}
