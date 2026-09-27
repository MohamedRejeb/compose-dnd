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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drag.DraggableItemState
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drag.applyConstraint
import com.mohamedrejeb.compose.dnd.drop.DropTargetState
import com.mohamedrejeb.compose.dnd.utils.MathUtils
import kotlinx.coroutines.coroutineScope
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
 * Minimum displacement of the dragged item relative to the hovered sibling
 * slot, in px, that identifies a hover reorder during the drop animation.
 */
private const val REORDER_DETECTION_DISTANCE = 1f

/**
 * Frames granted after the drop animation for a pending reorder
 * recomposition to surface before concluding nothing moved.
 */
private const val REORDER_DETECTION_EXTRA_FRAMES = 2

/** Per-frame movement below which the dragged item counts as settled, in px. */
private const val SETTLE_DISTANCE = 0.5f

/** Consecutive settled frames required before the final landing animation. */
private const val SETTLED_FRAME_COUNT = 2

/** Upper bound on frames spent waiting for the dragged item to settle. */
private const val MAX_SETTLE_FRAMES = 120

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
    private val dropTargetMap = mutableMapOf<Any, DropTargetState<T>>()

    /**
     * Key of the [DropTargetState] that is currently hovered
     */
    var hoveredDropTargetKey by mutableStateOf<Any?>(null)
        internal set

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

    internal fun removeDropTarget(key: Any) {
        dropTargetMap.remove(key)
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

    internal fun removeDraggableItem(key: Any) {
        draggableItemMap.remove(key)
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

    internal val dragPosition: MutableState<Offset> = mutableStateOf(Offset.Zero)
    internal val dragPositionAnimatable: Animatable<Offset, AnimationVector2D> = Animatable(Offset.Zero, Offset.VectorConverter)

    /**
     * Handle drag start method is called when drag starts
     * - It updates [DragAndDropState.currentDraggableItem]
     * - It updates [DragAndDropState.draggedItem]
     * @param offset - offset of the drag start position
     */
    internal suspend fun handleDragStart(
        key: Any,
        offset: Offset,
    ) = coroutineScope {
        val draggableItemState = draggableItemMap[key] ?: return@coroutineScope

        launch {
            dragPositionAnimatable.snapTo(Offset.Zero)
        }

        dragPosition.value = draggableItemState.positionInRoot

        isActiveDrag = true
        dragStartPositionInRoot = draggableItemState.positionInRoot
        reorderHysteresis.reset()
        dragStartOffset = offset
        draggedItemShadowSize = null
        currentDraggableItem = draggableItemState.copy()
        draggedItem = DraggedItemState(
            key = draggableItemState.key,
            data = draggableItemState.data,
            dragAmount = Offset.Zero,
        )
    }

    /**
     * Handle drag method is called when drag is in progress
     * - It updates [DragAndDropState.draggedItem]
     * - It updates [hoveredDropTargetKey] if needed
     * - It calls [DropTargetState.onDragEnter] and [DropTargetState.onDragExit] if needed
     * @param offset - offset of the drag position
     */
    internal suspend fun handleDrag(
        offset: Offset,
    ) = coroutineScope {
        // reevaluateDropTargets can fire before any real pointer event arrived
        if (offset.isUnspecified) return@coroutineScope

        val movementDelta = if (lastPointerOffset.isSpecified) {
            offset - lastPointerOffset
        } else {
            Offset.Zero
        }
        lastPointerOffset = offset
        reorderHysteresis.trackMovement(movementDelta)

        val currentDraggableItem = currentDraggableItem ?: return@coroutineScope
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
    internal suspend fun reevaluateDropTargets() {
        if (draggedItem == null) return
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

        val dropTarget = dropTargetMap.values.find { it.key == hoveredDropTargetKey }

        if (dropTarget == null) {
            animateDropPosition(currentDraggableItem.dropAnimationSpec) {
                ghostReturnTopLeft(currentDraggableItem)
            }
        } else if (dropTarget.dropAnimationEnabled) {
            animateDropOnTarget(dropTarget, currentDraggableItem)
        }

        draggedItem?.let {
            dropTarget?.onDrop?.invoke(it)
        }
        clearDragState()
    }

    /**
     * Animate the ghost onto the hovered drop target.
     *
     * Dropping onto another draggable item's slot (a reorder sibling) is
     * special-cased: when the app reorders the list in onDragEnter, the
     * sibling that triggered the reorder glides away from the slot the user
     * dropped on while the dragged item takes its place, so following the
     * sibling's live bounds would land the ghost one slot off. The reorder is
     * detected as the dragged item moving *relative* to the sibling
     * (scrolling moves both together and doesn't trigger it). Once detected,
     * the ghost settles on the slot bounds captured at release, waits for the
     * dragged item's placement animation (e.g. animateItem) to finish
     * underneath it, and finally lands exactly on the item.
     *
     * When no reorder is detected — plain container targets, or apps that
     * only reorder in onDrop — the ghost follows the target's live bounds as
     * before.
     */
    private suspend fun animateDropOnTarget(
        dropTarget: DropTargetState<T>,
        currentDraggableItem: DraggableItemState<T>,
    ) {
        val ghostSize = draggedItemVisibleSize ?: currentDraggableItem.size
        val isSiblingItemSlot =
            dropTarget.key != currentDraggableItem.key && dropTarget.key in draggableItemMap

        if (
            isSiblingItemSlot &&
            ownItemBeatsTargetOverlap(dropTarget, currentDraggableItem, ghostSize)
        ) {
            // The hover reorder already played out before the release: the
            // dragged item, not the hovered sibling, occupies the slot under
            // the ghost — settle on the item directly.
            settleOnOwnItem(dropTarget, currentDraggableItem)
            return
        }

        // Slot bounds as the user saw them on release: after a hover reorder
        // the sibling glides away from them, but the dragged item glides in.
        val releaseAnchor = dropTarget.getDropTopLeft(ghostSize)
        val ownStart = ownItemTopLeft(currentDraggableItem)
        val targetStart = liveTargetTopLeft(dropTarget)
        var reorderedOnHover = false

        fun detectHoverReorder() {
            if (!isSiblingItemSlot || reorderedOnHover) return
            val ownMotion = ownItemTopLeft(currentDraggableItem) - ownStart
            val targetMotion = liveTargetTopLeft(dropTarget) - targetStart
            reorderedOnHover =
                (ownMotion - targetMotion).getDistance() > REORDER_DETECTION_DISTANCE
        }

        animateDropPosition(currentDraggableItem.dropAnimationSpec) {
            detectHoverReorder()
            if (reorderedOnHover) {
                releaseAnchor
            } else {
                dropTarget.layoutCoordinates
                    ?.takeIf { it.isAttached }
                    ?.let { dropTarget.topLeft = it.positionInRoot() }
                dropTarget.getDropTopLeft(ghostSize)
            }
        }

        if (!isSiblingItemSlot) return

        // The release can come before the reorder's recomposition has run at
        // all — give it a couple of frames before concluding nothing moved.
        repeat(REORDER_DETECTION_EXTRA_FRAMES) {
            withFrameNanos { }
            detectHoverReorder()
        }
        if (!reorderedOnHover) return

        awaitDraggedItemSettled(currentDraggableItem, dropTarget)
        animateDropPosition(currentDraggableItem.dropAnimationSpec) {
            ghostReturnTopLeft(currentDraggableItem)
        }
    }

    /**
     * Settle the ghost on the dragged item itself: follow its live rect,
     * wait out any placement animation still running, then land exactly.
     */
    private suspend fun settleOnOwnItem(
        dropTarget: DropTargetState<T>,
        currentDraggableItem: DraggableItemState<T>,
    ) {
        animateDropPosition(currentDraggableItem.dropAnimationSpec) {
            ghostReturnTopLeft(currentDraggableItem)
        }
        awaitDraggedItemSettled(currentDraggableItem, dropTarget)
        animateDropPosition(currentDraggableItem.dropAnimationSpec) {
            ghostReturnTopLeft(currentDraggableItem)
        }
    }

    /**
     * True when the dragged item's own rect overlaps the released ghost more
     * than the hovered target does — the released slot already belongs to the
     * dragged item, so the hover reorder has already (at least partly) played
     * out. The target wins ties, keeping reorder-on-drop apps on the
     * follow-the-target behavior.
     */
    private fun ownItemBeatsTargetOverlap(
        dropTarget: DropTargetState<T>,
        currentDraggableItem: DraggableItemState<T>,
        ghostSize: Size,
    ): Boolean {
        val releaseTopLeft = dragPosition.value
        val ownOverlap = MathUtils.overlappingArea(
            topLeft1 = releaseTopLeft,
            size1 = ghostSize,
            topLeft2 = ownItemTopLeft(currentDraggableItem),
            size2 = draggedItemLiveSize ?: currentDraggableItem.size,
        )
        val targetOverlap = MathUtils.overlappingArea(
            topLeft1 = releaseTopLeft,
            size1 = ghostSize,
            topLeft2 = liveTargetTopLeft(dropTarget),
            size2 = dropTarget.size,
        )
        return ownOverlap > targetOverlap
    }

    /**
     * Handle drop method is called when drag is canceled
     * - It performs animation to the dragged item original position
     * - It clears the drag state
     */
    internal suspend fun handleDragCancel() {
        isActiveDrag = false
        val currentDraggableItem = currentDraggableItem ?: return

        animateDropPosition(currentDraggableItem.dropAnimationSpec) {
            ghostReturnTopLeft(currentDraggableItem)
        }

        clearDragState()
    }

    /**
     * Animate the ghost onto a target that can keep moving while the
     * animation runs, e.g. items still gliding under animateItem after a
     * reorder. The offset between the ghost and the target is animated to
     * zero in a single uninterrupted animation while the target is re-read
     * every frame, so the ghost follows a moving target smoothly and lands
     * exactly on it. (Cancel-and-restart retargeting would cancel the
     * animation every frame while the target moves, before it can make any
     * progress — freezing the ghost mid-air.)
     */
    private suspend fun animateDropPosition(
        animationSpec: AnimationSpec<Offset>,
        targetTopLeft: () -> Offset,
    ) {
        val startGhost = dragPosition.value + dragPositionAnimatable.value
        val delta = Animatable(startGhost - targetTopLeft(), Offset.VectorConverter)
        delta.animateTo(Offset.Zero, animationSpec) {
            dragPosition.value = targetTopLeft() + value
        }
        dragPosition.value = targetTopLeft()
    }

    /**
     * Wait until the dragged item's post-reorder placement animation has
     * finished, so the final landing animation targets a settled position.
     *
     * Motion is measured relative to the hovered sibling: the reorder's
     * placement animations move the two apart, while scrolling moves both
     * together. Waiting for absolute stillness would stall the drop for as
     * long as the user keeps scrolling. Bounded so a drop can never hang on
     * an app that keeps the item moving.
     */
    private suspend fun awaitDraggedItemSettled(
        currentDraggableItem: DraggableItemState<T>,
        dropTarget: DropTargetState<T>,
    ) {
        fun relativeTopLeft() = ownItemTopLeft(currentDraggableItem) - liveTargetTopLeft(dropTarget)

        var previous = relativeTopLeft()
        var settledFrames = 0
        var frames = 0
        while (frames < MAX_SETTLE_FRAMES && settledFrames < SETTLED_FRAME_COUNT) {
            withFrameNanos { }
            frames++
            val current = relativeTopLeft()
            settledFrames =
                if ((current - previous).getDistance() < SETTLE_DISTANCE) settledFrames + 1 else 0
            previous = current
        }
    }

    private fun liveItemPosition(itemState: DraggableItemState<T>?, fallback: Offset): Offset =
        itemState?.let { item ->
            item.layoutCoordinates
                ?.takeIf { it.isAttached }
                ?.positionInRoot()
                ?: item.positionInRoot
        } ?: fallback

    /**
     * Live top-left of the dragged item's own composable, re-read from the
     * registry so an item recreated under the same key mid-animation is
     * still tracked.
     */
    private fun ownItemTopLeft(currentDraggableItem: DraggableItemState<T>): Offset =
        liveItemPosition(
            itemState = draggableItemMap[currentDraggableItem.key],
            fallback = currentDraggableItem.positionInRoot,
        )

    private fun liveTargetTopLeft(dropTarget: DropTargetState<T>): Offset =
        dropTarget.layoutCoordinates
            ?.takeIf { it.isAttached }
            ?.positionInRoot()
            ?: dropTarget.topLeft

    /**
     * Top-left the ghost should land on when it settles on the item itself
     * (no drop target, canceled drag, or a hover reorder): the ghost centered
     * on the live item rect, so a size difference between the ghost and the
     * item still reads as landing on the item.
     */
    private fun ghostReturnTopLeft(currentDraggableItem: DraggableItemState<T>): Offset {
        val itemTopLeft = ownItemTopLeft(currentDraggableItem)
        val itemSize = draggedItemLiveSize ?: currentDraggableItem.size
        val ghostSize = draggedItemVisibleSize ?: currentDraggableItem.size
        return Offset(
            x = itemTopLeft.x + (itemSize.width - ghostSize.width) / 2f,
            y = itemTopLeft.y + (itemSize.height - ghostSize.height) / 2f,
        )
    }

    /**
     * Clear the drag state after drag is finished
     */
    private suspend fun clearDragState() {
        isActiveDrag = false
        currentDraggableItem = null
        draggedItem = null
        draggedItemShadowSize = null
        hoveredDropTargetKey = null
        lastPointerOffset = Offset.Unspecified
        dragStartOffset = Offset.Zero
        dragStartPositionInRoot = Offset.Zero
        reorderHysteresis.reset()
        dragPosition.value = Offset.Zero
        dragPositionAnimatable.snapTo(Offset.Zero)
    }
}
