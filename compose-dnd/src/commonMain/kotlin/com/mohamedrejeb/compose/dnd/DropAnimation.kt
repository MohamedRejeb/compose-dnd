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
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.positionInRoot
import com.mohamedrejeb.compose.dnd.drag.DraggableItemState
import com.mohamedrejeb.compose.dnd.drop.DropTargetState
import com.mohamedrejeb.compose.dnd.utils.MathUtils

// Drop animation: moves the drag shadow (ghost) from the release point onto
// its landing spot, following targets that keep moving while it animates.

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

/** Distance from its final place below which the dragged item counts as settled, in px. */
private const val SETTLE_DISTANCE = 0.5f

/** Consecutive settled frames required before the final landing animation. */
private const val SETTLED_FRAME_COUNT = 2

/** Upper bound on frames spent waiting for the dragged item to settle. */
private const val MAX_SETTLE_FRAMES = 120

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
internal suspend fun <T> DragAndDropState<T>.animateDropOnTarget(
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
        settleOnOwnItem(currentDraggableItem)
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

    awaitDraggedItemSettled(currentDraggableItem)
    animateDropPosition(currentDraggableItem.dropAnimationSpec) {
        ghostReturnTopLeft(currentDraggableItem)
    }
}

/**
 * Settle the ghost on the dragged item itself: follow its live rect,
 * wait out any placement animation still running, then land exactly.
 */
internal suspend fun <T> DragAndDropState<T>.settleOnOwnItem(
    currentDraggableItem: DraggableItemState<T>,
) {
    animateDropPosition(currentDraggableItem.dropAnimationSpec) {
        ghostReturnTopLeft(currentDraggableItem)
    }
    awaitDraggedItemSettled(currentDraggableItem)
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
internal fun <T> DragAndDropState<T>.ownItemBeatsTargetOverlap(
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
 * Animate the ghost onto a target that can keep moving while the
 * animation runs, e.g. items still gliding under animateItem after a
 * reorder. The offset between the ghost and the target is animated to
 * zero in a single uninterrupted animation while the target is re-read
 * every frame, so the ghost follows a moving target smoothly and lands
 * exactly on it. (Cancel-and-restart retargeting would cancel the
 * animation every frame while the target moves, before it can make any
 * progress — freezing the ghost mid-air.)
 */
internal suspend fun <T> DragAndDropState<T>.animateDropPosition(
    animationSpec: AnimationSpec<Offset>,
    targetTopLeft: () -> Offset,
) {
    val startGhost = dragPosition.value
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
 * What is left of that animation is the distance between where the item
 * is and where the layout settles it. Scrolling moves both together, so
 * the drop doesn't stall for as long as the list keeps scrolling. Bounded
 * so a drop can never hang on an app that keeps the item moving.
 */
internal suspend fun <T> DragAndDropState<T>.awaitDraggedItemSettled(
    currentDraggableItem: DraggableItemState<T>,
) {
    fun distanceToSettled(): Float {
        val coordinates = draggableItemMap[currentDraggableItem.key]
            ?.layoutCoordinates
            ?.takeIf { it.isAttached }
            ?: return 0f
        return (coordinates.positionInRoot() - positionOf(coordinates)).getDistance()
    }

    var settledFrames = 0
    var frames = 0
    while (frames < MAX_SETTLE_FRAMES && settledFrames < SETTLED_FRAME_COUNT) {
        withFrameNanos { }
        frames++
        settledFrames = if (distanceToSettled() < SETTLE_DISTANCE) settledFrames + 1 else 0
    }
}

/**
 * Live top-left of the dragged item's own composable, re-read from the
 * registry so an item recreated under the same key mid-animation is
 * still tracked.
 */
internal fun <T> DragAndDropState<T>.ownItemTopLeft(currentDraggableItem: DraggableItemState<T>): Offset =
    draggableItemMap[currentDraggableItem.key]?.livePositionInRoot()
        ?: currentDraggableItem.positionInRoot

internal fun <T> DragAndDropState<T>.liveTargetTopLeft(dropTarget: DropTargetState<T>): Offset =
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
internal fun <T> DragAndDropState<T>.ghostReturnTopLeft(currentDraggableItem: DraggableItemState<T>): Offset {
    val itemTopLeft = ownItemTopLeft(currentDraggableItem)
    val itemSize = draggedItemLiveSize ?: currentDraggableItem.size
    val ghostSize = draggedItemVisibleSize ?: currentDraggableItem.size
    return Offset(
        x = itemTopLeft.x + (itemSize.width - ghostSize.width) / 2f,
        y = itemTopLeft.y + (itemSize.height - ghostSize.height) / 2f,
    )
}
