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

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.abs
import kotlin.math.min

/** Fallback in px; [rememberDragAndDropState] overrides it with a density-converted Dp. */
internal const val DefaultReorderHysteresisDistancePx = 8f

/** Two slots moved relative to each other when their offset changed by more than this, in px. */
internal const val SlotMoveTolerancePx = 0.5f

/** Share of the narrower of the two that the dragged item must overlap a slot by, across the way to it. */
private const val MinSidewaysOverlap = 0.5f

/**
 * True when the dragged item has moved far enough toward another slot of a
 * reorderable layout to take it:
 * - its leading edge, on the way from its own slot to that slot, has passed the
 *   middle of that slot by [marginPx],
 * - and it is still at least half over that slot across that way, so a card pulled
 *   sideways out of its column no longer swaps with the cards it leaves behind.
 *
 * Unlike comparing overlaps or center distances, this stays true after the two
 * items have swapped, whatever their sizes: the slot left behind then lies on the
 * other side, and its middle is out of reach by the spacing between the two items
 * plus twice the margin. So the same position never asks for the swap to be undone.
 *
 * For a slot in another row or column both are measured along and across the line
 * between the two slot centers, so it also works in grids and between lists.
 */
internal fun hasReachedSlot(
    ownSlotCenter: Offset,
    slotCenter: Offset,
    slotSize: Size,
    draggedTopLeft: Offset,
    draggedSize: Size,
    marginPx: Float,
): Boolean {
    val toSlot = slotCenter - ownSlotCenter
    val distance = toSlot.getDistance()
    // Both slots at the same place, e.g. an item inserted over one that has yet to move
    if (distance == 0f) return false

    val direction = toSlot / distance
    val draggedCenter = draggedTopLeft + Offset(draggedSize.width / 2f, draggedSize.height / 2f)
    val fromSlot = draggedCenter - slotCenter

    // How far the dragged item extends from its center toward the slot
    val reach = draggedSize.extentAlong(direction) / 2f
    val centerPastSlot = fromSlot.x * direction.x + fromSlot.y * direction.y
    if (centerPastSlot + reach < marginPx) return false

    // How much the two overlap across the way from one slot to the other
    val across = Offset(-direction.y, direction.x)
    val draggedAcross = draggedSize.extentAlong(across)
    val slotAcross = slotSize.extentAlong(across)
    val centersApart = abs(fromSlot.x * across.x + fromSlot.y * across.y)
    val overlap = (draggedAcross + slotAcross) / 2f - centersApart

    return overlap >= min(draggedAcross, slotAcross) * MinSidewaysOverlap
}

/** Length of the shadow a box of this size casts on a line running along [direction]. */
private fun Size.extentAlong(direction: Offset): Float =
    abs(direction.x) * width + abs(direction.y) * height
