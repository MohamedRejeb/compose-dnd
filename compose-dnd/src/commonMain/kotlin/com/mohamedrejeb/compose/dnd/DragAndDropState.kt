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
package com.mohamedrejeb.compose.dnd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drag.DraggableItemState
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drag.applyConstraint
import com.mohamedrejeb.compose.dnd.drop.DropTargetState
import com.mohamedrejeb.compose.dnd.utils.MathUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch

/**
 * Default reorder hysteresis distance for [rememberDragAndDropState].
 *
 * After a reorder swap, the cursor has to move back at least this far in the
 * opposite direction before the swapped target can be re-entered and the swap
 * undone. This prevents a slow drag from triggering the same swap repeatedly.
 */
val DefaultReorderHysteresisDistance: Dp = 8.dp

/**
 * Remember [DragAndDropState]
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * This parameter is applied to all [DraggableItem]s. If you want to change it for a specific item, use [DraggableItem] parameter.
 * @param requireFirstDownUnconsumed if true, the first down event should be unconsumed
 * @param reorderHysteresisDistance how far the cursor must move back, opposite the swap direction,
 * before a just-swapped reorder target can be re-entered. `0.dp` disables it.
 * @param T type of the data that is dragged
 * @return [DragAndDropState]
 * @see DragAndDropState
 */
@Composable
fun <T> rememberDragAndDropState(
    dragAfterLongPress: Boolean = false,
    requireFirstDownUnconsumed: Boolean = false,
    reorderHysteresisDistance: Dp = DefaultReorderHysteresisDistance,
): DragAndDropState<T> {
    val density = LocalDensity.current
    val state = remember { DragAndDropState<T>() }
    state.dragAfterLongPress = dragAfterLongPress
    state.requireFirstDownUnconsumed = requireFirstDownUnconsumed
    state.reorderHysteresisDistancePx = with(density) { reorderHysteresisDistance.toPx() }
    return state
}

/**
 * State of the drag and drop
 * @param dragAfterLongPress if true, drag will start after long press, otherwise drag will start after simple press
 * This parameter is applied to all [DraggableItem]s. If you want to change it for a specific item, use [DraggableItem] parameter.
 * @param requireFirstDownUnconsumed if true, the first down event should be unconsumed
 * @param T type of the data that is dragged
 */
@Stable
class DragAndDropState<T>(
    dragAfterLongPress: Boolean = false,
    requireFirstDownUnconsumed: Boolean = false,
) {
    internal var dragAfterLongPress by mutableStateOf(dragAfterLongPress)
    internal var requireFirstDownUnconsumed by mutableStateOf(requireFirstDownUnconsumed)

    /**
     * If true, drag and drop is enabled
     */
    internal var enabled by mutableStateOf(true)

    // Drop Target

    /**
     * Map of [DropTargetState] by key
     */
    internal val dropTargetMap = mutableMapOf<Any, DropTargetState<T>>()

    /**
     * Key of the drop target the dragged item is currently hovering, or null
     * when no drag is in progress or no target is hovered.
     */
    var hoveredDropTargetKey by mutableStateOf<Any?>(null)
        internal set

    /**
     * Returns true if the item with the given [key] is currently being dragged.
     *
     * Reads snapshot state, so a composable or `graphicsLayer` block calling it
     * is updated when a drag starts or ends:
     * ```
     * Modifier.graphicsLayer { alpha = if (state.isDragging(item.id)) 0f else 1f }
     * ```
     */
    fun isDragging(key: Any): Boolean = draggedItem?.key == key

    /**
     * Returns true if the drop target with the given [key] is currently hovered
     * by the dragged item — handy for highlighting the target that will receive
     * the drop.
     */
    fun isDropTargetHovered(key: Any): Boolean = hoveredDropTargetKey == key

    /**
     * Cancel the drag in progress, if any: the drag shadow animates back to the
     * dragged item, no drop target receives `onDrop`, and the item's
     * `onDragEnd` fires once the animation ends. Useful to abort a drag on a
     * back press, an Escape key or when the dragged data goes away.
     *
     * Does nothing when no drag is in progress or once the item was released.
     */
    fun cancelDrag() {
        if (!isActiveDrag || isCancelRequested) return
        isCancelRequested = true
        pointerId = null
        val scope = containerScope
        if (scope == null) {
            isActiveDrag = false
            clearDragState()
        } else {
            scope.launch { handleDragCancel() }
        }
    }

    /**
     * Scope of the [DragAndDropContainer] hosting this state, used to run the
     * cancel animation of [cancelDrag].
     */
    internal var containerScope: CoroutineScope? = null

    /**
     * True between [cancelDrag] and the cancel animation starting: the drag
     * is still active (so no new drag can start and clobber it), but it no
     * longer reacts to hover and a second [cancelDrag] is ignored.
     */
    private var isCancelRequested = false

    /**
     * Listeners invoked synchronously **before** [hoveredDropTargetKey] changes
     * and before [DropTargetState.onDragEnter] / [DropTargetState.onDragExit] fire.
     *
     * Used by [com.mohamedrejeb.compose.dnd.scroll.dragScrollPin] to snapshot
     * scroll positions before a reorder mutates the list.
     */
    internal val onBeforeHoverTargetChange = mutableListOf<() -> Unit>()

    /**
     * Add or update [DropTargetState] in [dropTargetMap]
     */
    internal fun addDropTarget(dropTargetState: DropTargetState<T>) {
        if (dropTargetMap[dropTargetState.key] == dropTargetState) {
            return
        }

        dropTargetMap[dropTargetState.key] = dropTargetState
    }

    /**
     * Remove [dropTargetState] from [dropTargetMap], unless another target has
     * registered under the same key in the meantime (e.g. an item recreated in
     * another list is attached before the old one is detached).
     */
    internal fun removeDropTarget(key: Any, dropTargetState: DropTargetState<T>) {
        if (dropTargetMap[key] === dropTargetState) {
            dropTargetMap.remove(key)
        }
    }

    // Draggable Item

    /**
     * Map of [DraggableItemState] by key
     */
    internal val draggableItemMap = mutableMapOf<Any, DraggableItemState<T>>()

    /**
     * Item that is currently dragged [DraggableItemState], null if no item is dragged
     */
    internal var currentDraggableItem by mutableStateOf<DraggableItemState<T>?>(null)

    /**
     * State of the item that is currently dragged [DraggedItemState], null if no item is dragged
     */
    var draggedItem by mutableStateOf<DraggedItemState<T>?>(null)
        internal set

    /**
     * Size of the item that is currently dragged, read live from the registered
     * item state so mid-drag resizes (e.g. a card that collapses when picked up)
     * are taken into account. Falls back to the size captured at drag start when
     * the item has left the composition, or when it was recreated under the same
     * key (e.g. moved to a sibling container) and hasn't been measured yet.
     */
    internal val draggedItemLiveSize: Size?
        get() = currentDraggableItem?.let { item ->
            draggableItemMap[item.key]
                ?.size
                ?.takeIf { it != Size.Zero }
                ?: item.size
        }

    /**
     * Measured size of the drag shadow's content, written by
     * [com.mohamedrejeb.compose.dnd.drag.DraggedItemShadow].
     */
    internal var draggedItemShadowSize by mutableStateOf<Size?>(null)

    /**
     * Size of the ghost the user actually sees while dragging: the measured
     * drag shadow content, falling back to the size captured at drag start
     * until the shadow is first measured.
     *
     * Hover and drop-alignment math must use this instead of
     * [draggedItemLiveSize]: apps may recreate the item at a different size in
     * reaction to onDragEnter/onDragExit (e.g. moving it between differently
     * sized containers), so feeding the live item size back into hover would
     * oscillate. The shadow renders the draggableContent captured at drag
     * start, which stops following the item once it moves to another call
     * site, so its size stays stable across such enter/exit recreations.
     * (A persistent draggableContent that resizes itself off hover state
     * directly can still feed back — that is the app's choice.)
     */
    internal val draggedItemVisibleSize: Size?
        get() = currentDraggableItem?.let { item ->
            draggedItemShadowSize
                ?.takeIf { it != Size.Zero }
                ?: item.size
        }

    internal var pointerId by mutableStateOf<PointerId?>(null)

    /**
     * True while the pointer is actively dragging. Set to false immediately when
     * the drag ends or is canceled, before any drop animation starts.
     * Used by auto-scroll to stop scrolling as soon as the user releases.
     */
    internal var isActiveDrag by mutableStateOf(false)

    /**
     * Add or update [DraggableItemState]
     *
     * @param state - new state
     */
    internal fun addDraggableItem(
        state: DraggableItemState<T>
    ) {
        if (draggableItemMap[state.key] == state) {
            return
        }

        draggableItemMap[state.key] = state
    }

    /**
     * Remove [state] from [draggableItemMap], unless another item has
     * registered under the same key in the meantime.
     */
    internal fun removeDraggableItem(key: Any, state: DraggableItemState<T>) {
        if (draggableItemMap[key] === state) {
            draggableItemMap.remove(key)
        }
    }

    private var dragStartPositionInRoot: Offset = Offset.Zero
    private var dragStartOffset: Offset = Offset.Zero
    private var lastPointerOffset: Offset = Offset.Unspecified

    private val reorderHysteresis = ReorderHysteresis()

    internal var reorderHysteresisDistancePx: Float
        get() = reorderHysteresis.reverseDistancePx
        set(value) {
            reorderHysteresis.reverseDistancePx = value
        }

    /**
     * Top-left of the drag shadow (ghost) in root coordinates.
     */
    internal val dragPosition: MutableState<Offset> = mutableStateOf(Offset.Zero)

    /**
     * Coroutine running the current drop (or cancel) animation, null when idle.
     */
    private var dropJob: Job? = null

    /**
     * Completes the in-flight drop immediately: fires its callbacks and clears
     * the drag state. Null when no drop animation is running.
     */
    private var finishDrop: (() -> Unit)? = null

    /**
     * Returns true if a new drag may start: no other pointer is dragging.
     * A drop animation still in flight doesn't block a new drag — it is
     * completed instantly by [handleDragStart].
     */
    internal fun canStartDrag(): Boolean = pointerId == null && !isActiveDrag

    /**
     * Start dragging the item registered under [key].
     * - Completes any drop animation still running from the previous drag
     * - Updates [currentDraggableItem] and [draggedItem]
     * @param offset pointer position in root coordinates
     * @return false if no item is registered under [key]
     */
    internal fun handleDragStart(
        key: Any,
        offset: Offset,
    ): Boolean {
        val draggableItemState = draggableItemMap[key] ?: return false

        finishPendingDrop()

        val positionInRoot = draggableItemState.livePositionInRoot()
        dragPosition.value = positionInRoot

        isActiveDrag = true
        dragStartPositionInRoot = positionInRoot
        reorderHysteresis.reset()
        dragStartOffset = offset
        lastPointerOffset = Offset.Unspecified
        draggedItemShadowSize = null
        currentDraggableItem = draggableItemState.copy()

        val newDraggedItem = DraggedItemState(
            key = draggableItemState.key,
            data = draggableItemState.data,
            dragAmount = Offset.Zero,
        )
        draggedItem = newDraggedItem
        draggableItemState.onDragStart(newDraggedItem)
        return true
    }

    /**
     * Handle drag method is called when drag is in progress
     * - It updates [DragAndDropState.draggedItem]
     * - It updates [hoveredDropTargetKey] if needed
     * - It calls [DropTargetState.onDragEnter] and [DropTargetState.onDragExit] if needed
     * @param offset - offset of the drag position
     */
    internal fun handleDrag(
        offset: Offset,
    ) {
        // Events can still arrive after release, while the drop animates
        if (!isActiveDrag || isCancelRequested) return
        // reevaluateDropTargets can fire before any real pointer event arrived
        if (offset.isUnspecified) return

        val movementDelta = if (lastPointerOffset.isSpecified) {
            offset - lastPointerOffset
        } else {
            Offset.Zero
        }
        lastPointerOffset = offset
        reorderHysteresis.trackMovement(movementDelta)

        val currentDraggableItem = currentDraggableItem ?: return
        val dropTargetIds = currentDraggableItem.dropTargets

        reorderHysteresis.liftIfReversed(offset)

        val rawDragAmount = offset - dragStartOffset
        val dragAmount = currentDraggableItem.dragAxis.applyConstraint(rawDragAmount)
        val newTopLeft = dragStartPositionInRoot + dragAmount
        val draggedItemSize = draggedItemVisibleSize ?: currentDraggableItem.size
        val hysteresisExcludedKey = reorderHysteresis.excludedKey
        val hoveredDropTargets =
            dropTargetMap.values
                .filter {
                    it.key != hysteresisExcludedKey &&
                        MathUtils.isRectangleIntersected(
                            topLeft1 = newTopLeft,
                            size1 = draggedItemSize,
                            topLeft2 = it.topLeft,
                            size2 = it.size,
                        ) &&
                        (dropTargetIds.isEmpty() || it.key in dropTargetIds) &&
                        it.canDrop
                }.groupBy { it.zIndex }
                .maxByOrNull { it.key }
                ?.value
                .orEmpty()

        val hoveredDropTarget =
            currentDraggableItem.dropStrategy.getHoveredDropTarget(
                draggedItemTopLeft = newTopLeft,
                draggedItemSize = draggedItemSize,
                dropTargets = hoveredDropTargets,
            )

        val newDraggedItemState = draggedItem?.copy(
            dragAmount = dragAmount,
        )

        if (hoveredDropTarget?.key != hoveredDropTargetKey && newDraggedItemState != null) {
            onBeforeHoverTargetChange.forEach { it.invoke() }
            dropTargetMap.values
                .find { it.key == hoveredDropTargetKey }
                ?.onDragExit
                ?.invoke(newDraggedItemState)
            hoveredDropTarget?.onDragEnter?.invoke(newDraggedItemState)

            // Arm only on a reorder swap: reorderableItem registers the dragged
            // item's own slot as a drop target, plain drop targets never do.
            val newKey = hoveredDropTarget?.key
            val ownSlotIntersects =
                hoveredDropTargets.any { it.key == currentDraggableItem.key }
            if (
                newKey != null &&
                newKey != currentDraggableItem.key &&
                ownSlotIntersects
            ) {
                reorderHysteresis.arm(
                    key = newKey,
                    pointerOffset = offset,
                )
            }
        }

        dragPosition.value = newTopLeft

        hoveredDropTargetKey = hoveredDropTarget?.key
        draggedItem = newDraggedItemState
    }

    /**
     * Re-evaluate drop targets using the last known pointer position.
     * Called by auto-scroll to update hover state when the list scrolls
     * but the pointer hasn't moved.
     */
    internal fun reevaluateDropTargets() {
        handleDrag(lastPointerOffset)
    }

    /**
     * Handle drop method is called when drag is finished
     * - It performs animation to the final position
     * - It calls [DropTargetState.onDrop] if the item is dropped on [DropTargetState]
     * - It clears the drag state
     *
     * The ghost keeps its visible size during the drop animation — only its
     * position animates. Size differences between the ghost and the item that
     * lands in the target are absorbed by the position anchor instead: the
     * ghost is aligned within the target at its own size, so with the default
     * [androidx.compose.ui.Alignment.Companion.Center] alignment its center
     * travels to the target's center.
     */
    internal suspend fun handleDragEnd() {
        isActiveDrag = false
        val currentDraggableItem = currentDraggableItem ?: return
        val draggedItem = draggedItem ?: return

        val dropTarget = hoveredDropTargetKey?.let { dropTargetMap[it] }

        runDrop(
            onFinish = {
                dropTarget?.onDrop?.invoke(draggedItem)
                dispatchDragEnd(currentDraggableItem, draggedItem)
            },
        ) {
            if (dropTarget == null) {
                animateDropPosition(currentDraggableItem.dropAnimationSpec) {
                    ghostReturnTopLeft(currentDraggableItem)
                }
            } else if (dropTarget.dropAnimationEnabled) {
                animateDropOnTarget(dropTarget, currentDraggableItem)
            }
        }
    }

    /**
     * Handle drop method is called when drag is canceled
     * - It performs animation to the dragged item original position
     * - It clears the drag state
     */
    internal suspend fun handleDragCancel() {
        isActiveDrag = false
        val currentDraggableItem = currentDraggableItem ?: return
        val draggedItem = draggedItem ?: return

        runDrop(
            onFinish = { dispatchDragEnd(currentDraggableItem, draggedItem) },
        ) {
            animateDropPosition(currentDraggableItem.dropAnimationSpec) {
                ghostReturnTopLeft(currentDraggableItem)
            }
        }
    }

    /**
     * Run a drop (or cancel) [animation], then [onFinish] and clear the drag
     * state. A drag starting before the animation ends calls
     * [finishPendingDrop], which runs [onFinish] right away and cancels the
     * animation, so the new drag never sees stale state.
     */
    private suspend fun runDrop(
        onFinish: () -> Unit,
        animation: suspend () -> Unit,
    ) {
        val job = currentCoroutineContext()[Job]
        var finished = false
        val finish = {
            if (!finished) {
                finished = true
                onFinish()
                clearDragState()
            }
        }
        dropJob = job
        finishDrop = finish
        try {
            animation()
        } finally {
            if (dropJob === job) {
                dropJob = null
                finishDrop = null
            }
            finish()
        }
    }

    /**
     * Complete the drop animation still running from the previous drag, if
     * any: its callbacks fire immediately and the animation is canceled.
     */
    private fun finishPendingDrop() {
        val finish = finishDrop ?: return
        val job = dropJob
        dropJob = null
        finishDrop = null
        finish()
        job?.cancel()
    }

    /**
     * Invoke the dragged item's onDragEnd, preferring the live registration
     * so the latest lambda runs even if the item recomposed during the drag.
     */
    private fun dispatchDragEnd(
        currentDraggableItem: DraggableItemState<T>,
        draggedItem: DraggedItemState<T>,
    ) {
        val itemState = draggableItemMap[currentDraggableItem.key] ?: currentDraggableItem
        itemState.onDragEnd(draggedItem)
    }

    /**
     * Clear the drag state after drag is finished
     */
    private fun clearDragState() {
        isActiveDrag = false
        isCancelRequested = false
        currentDraggableItem = null
        draggedItem = null
        draggedItemShadowSize = null
        hoveredDropTargetKey = null
        lastPointerOffset = Offset.Unspecified
        dragStartOffset = Offset.Zero
        dragStartPositionInRoot = Offset.Zero
        reorderHysteresis.reset()
        dragPosition.value = Offset.Zero
    }
}
