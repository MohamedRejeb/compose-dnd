package com.mohamedrejeb.compose.dnd

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DraggableItemScope

@OptIn(ExperimentalDndApi::class)
@Composable
fun <T> ToggleableDragAndDropContainer(
    state: DragAndDropState<T>,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ToggleableDragAndDropScope.() -> Unit,
) {
    val scope = remember(enabled) { ToggleableDragAndDropScope(enabled) }

    if (!enabled) content(scope)
    else {
        DragAndDropContainer(
            state = state,
            modifier = modifier,
            content = { content(scope) },
        )
    }
}

@OptIn(ExperimentalDndApi::class)
class ToggleableDragAndDropScope(
    private val enabled: Boolean,
) {

    @OptIn(ExperimentalDndApi::class)
    @Composable
    fun <T> DraggableItem(
        modifier: Modifier = Modifier,
        key: Any,
        data: T,
        state: DragAndDropState<T>,
        dragAfterLongPress: Boolean = state.dragAfterLongPress,
        dropTargets: List<Any> = emptyList(),
        dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
        draggableContent: (@Composable () -> Unit)? = null,
        content: @Composable DraggableItemScope.() -> Unit,
    ) {
        if (!enabled) {
            val fakeScope = remember {
                object : DraggableItemScope {
                    override val isDragging: Boolean = false
                    override val key: Any = key
                }
            }
            content(fakeScope)
        } else {
            com.mohamedrejeb.compose.dnd.drag.DraggableItem(
                modifier = modifier,
                key = key,
                data = data,
                state = state,
                dragAfterLongPress = dragAfterLongPress,
                dropTargets = dropTargets,
                dropAnimationSpec = dropAnimationSpec,
                draggableContent = draggableContent,
                content = content,
            )
        }
    }
}
