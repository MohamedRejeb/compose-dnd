package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture

/**
 * Wrapper Composable for draggable item.
 *
 * @param modifier - modifier for the DraggableItem composable
 * @param key - unique key for this item
 * @param data - data that will be passed to drop target on drop
 * @param state - state of the drag and drop
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * @param dropTargets - list of drop targets ids to which this item can be dropped, if empty, item can be dropped to any drop target
 * @param dropAnimationSpec - animation spec for the drop animation
 * @param draggableContent The content of the draggable item, if null, the content of the item will be used.
 * @param content - content that will be shown when item is not dragged
 */
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(key, state, data) {
        state.draggableItemMap[key]?.data = data
    }

    LaunchedEffect(key, state, dropTargets) {
        state.draggableItemMap[key]?.dropTargets = dropTargets
    }

    LaunchedEffect(key, state, dropAnimationSpec) {
        state.draggableItemMap[key]?.dropAnimationSpec = dropAnimationSpec
    }

    DisposableEffect(key, state) {
        onDispose {
            state.removeDraggableItem(key)
        }
    }

    val draggableItemScopeImpl = remember(key, state) {
        DraggableItemScopeImpl(
            key = key,
            state = state,
        )
    }

    val draggableItemScopeShadowImpl = remember(key) {
        DraggableItemScopeShadowImpl(
            key = key,
        )
    }

    with(draggableItemScopeImpl) {
        Box(
            modifier = modifier
                .onGloballyPositioned {
                    val draggableItemState = DraggableItemState(
                        key = key,
                        data = data,
                        positionInRoot = it.positionInRoot(),
                        size = it.size.toSize(),
                        dropTargets = dropTargets,
                        dropAnimationSpec = dropAnimationSpec,
                        content = draggableContent ?: {
                            with(draggableItemScopeShadowImpl) {
                                content()
                            }
                        },
                    )

                    state.addOrUpdateDraggableItem(
                        state = draggableItemState,
                    )
                }
                .pointerInput(key, state) {
                    detectDragStartGesture(
                        key = key,
                        state = state,
                        dragAfterLongPress = dragAfterLongPress,
                        scope = scope,
                    )
                },
        ) {
            content()
        }
    }
}
