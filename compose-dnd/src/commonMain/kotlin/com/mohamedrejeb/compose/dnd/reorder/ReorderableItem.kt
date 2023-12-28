package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DraggableItemState
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.gesture.detectDragStartGesture

/**
 * Mark this composable as a reorderable item.
 *
 * @param modifier The modifier to be applied to the item.
 * @param state The reorder state.
 * @param key The key used to identify the item.
 * @param data The data associated with the item.
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * @param dropTargets - list of drop targets ids to which this item can be dropped, if empty, item can be dropped to any drop target
 * @param onDrop The action to perform when an item is dropped onto the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragEnter The action to perform when an item is dragged over the target.
 * Accepts the dragged item state as a parameter.
 * @param onDragExit The action to perform when an item is dragged out of the target.
 * Accepts the dragged item state as a parameter.
 * @param dropAnimationSpec - animation spec for the drop animation
 * @param draggableContent The content of the draggable item, if null, the content of the item will be used.
 * @param content The content of the item.
 */
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(key, state, data) {
        state.dndState.draggableItemMap[key]?.data = data
    }

    LaunchedEffect(key, state, dropTargets) {
        state.dndState.draggableItemMap[key]?.dropTargets = dropTargets
    }

    LaunchedEffect(key, state, dropAnimationSpec) {
        state.dndState.draggableItemMap[key]?.dropAnimationSpec = dropAnimationSpec
    }

    DisposableEffect(key, state) {
        onDispose {
            state.dndState.removeDraggableItem(key)
        }
    }

    val reorderableItemScopeImpl = remember(key, state) {
        ReorderableItemScopeImpl(
            key = key,
            state = state.dndState,
        )
    }

    val reorderableItemScopeShadowImpl = remember(key) {
        ReorderableItemScopeShadowImpl(
            key = key,
        )
    }

    Box(
        modifier = Modifier
            .onGloballyPositioned {
                val draggableItemState = DraggableItemState(
                    key = key,
                    data = data,
                    positionInRoot = it.positionInRoot(),
                    size = it.size.toSize(),
                    dropTargets = dropTargets,
                    dropAnimationSpec = dropAnimationSpec,
                    content = draggableContent ?: {
                        with(reorderableItemScopeShadowImpl) {
                            content()
                        }
                    },
                )

                state.dndState.addOrUpdateDraggableItem(
                    state = draggableItemState,
                )
            }
            .pointerInput(key, state) {
                detectDragStartGesture(
                    key = key,
                    state = state.dndState,
                    dragAfterLongPress = dragAfterLongPress,
                    scope = scope,
                )
            }
            .dropTarget(
                key = key,
                state = state.dndState,
                zIndex = zIndex,
                onDrop = onDrop,
                onDragEnter = onDragEnter,
                onDragExit = onDragExit,
            )
            .then(modifier),
    ) {
        with(reorderableItemScopeImpl) {
            content()
        }
    }
}
