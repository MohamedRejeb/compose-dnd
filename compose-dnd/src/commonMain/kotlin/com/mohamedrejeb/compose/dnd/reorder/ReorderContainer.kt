package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi

/**
 * Container for reorder,
 * All reorderable items should be inside this container
 *
 * @param state The state of the reorder
 * @param modifier [Modifier]
 * @param content content of the container
 */
@OptIn(ExperimentalDndApi::class)
@Composable
fun <T> ReorderContainer(
    state: ReorderState<T>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    DragAndDropContainer(
        state = state.dndState,
        modifier = modifier,
        content = content,
    )
}