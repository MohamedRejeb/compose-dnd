package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState

@OptIn(ExperimentalDndApi::class)
@Composable
fun <T> ToggleableReorderContainer(
    state: ReorderState<T>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ToggleableReorderContainerScope.() -> Unit,
) {
    val scope = remember(enabled) { ToggleableReorderContainerScope(enabled) }

    if (!enabled) content(scope)
    else {
        ReorderContainer(
            state = state,
            modifier = modifier,
            content = { content(scope) },
        )
    }
}

@OptIn(ExperimentalDndApi::class)
class ToggleableReorderContainerScope(
    private val enabled: Boolean,
) {

    @OptIn(ExperimentalDndApi::class)
    @Composable
    fun <T> ReorderableItem(
        modifier: Modifier = Modifier,
        state: ReorderState<T>,
        key: Any,
        data: T,
        zIndex: Float = 0f,
        dragAfterLongPress: Boolean = state.dndState.dragAfterLongPress,
        dropTargets: List<Any> = emptyList(),
        onDrop: (state: DraggedItemState<T>) -> Unit = {},
        onDragEnter: (state: DraggedItemState<T>) -> Unit = {},
        onDragExit: (state: DraggedItemState<T>) -> Unit = {},
        dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
        draggableContent: (@Composable () -> Unit)? = null,
        content: @Composable ReorderableItemScope.() -> Unit,
    ) {
        if (!enabled) {
            val fakeScope = remember {
                object : ReorderableItemScope {
                    override val isDragging: Boolean = false
                    override val key: Any = key
                }
            }
            content(fakeScope)
        } else {
            com.mohamedrejeb.compose.dnd.reorder.ReorderableItem(
                modifier = modifier,
                state = state,
                key = key,
                data = data,
                zIndex = zIndex,
                dragAfterLongPress = dragAfterLongPress,
                dropTargets = dropTargets,
                onDrop = onDrop,
                onDragEnter = onDragEnter,
                onDragExit = onDragExit,
                dropAnimationSpec = dropAnimationSpec,
                draggableContent = draggableContent,
                content = content,
            )
        }
    }
}
