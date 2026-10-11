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

/** Fallback in px; [rememberDragAndDropState] overrides it with a density-converted Dp. */
internal const val DefaultReorderHysteresisDistancePx = 8f

/** Two slots moved relative to each other when their offset changed by more than this, in px. */
internal const val SlotMoveTolerancePx = 0.5f

/**
 * True when the dragged item has moved far enough toward another slot of a
 * reorderable layout to take it: its leading edge, on the way from its own slot
 * to that slot, has passed the middle of that slot by [marginPx].
 *
 * Unlike comparing overlaps or center distances, this stays true after the two
 * items have swapped, whatever their sizes: the slot left behind then lies on the
 * other side, and its middle is out of reach by the spacing between the two items
 * plus twice the margin. So the same position never asks for the swap to be undone.
 *
 * For a slot in another row or column the leading edge is measured along the line
 * between the two slot centers, so it also works in grids and between lists.
 */
internal fun hasReachedSlot(
    ownSlotCenter: Offset,
    slotCenter: Offset,
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
    // How far the dragged item extends from its center toward the slot
    val reach = (abs(direction.x) * draggedSize.width + abs(direction.y) * draggedSize.height) / 2f
    val centerPastSlot = (draggedCenter - slotCenter).let { it.x * direction.x + it.y * direction.y }

    return centerPastSlot + reach >= marginPx
}
