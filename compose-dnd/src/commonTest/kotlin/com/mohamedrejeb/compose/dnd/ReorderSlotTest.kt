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
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReorderSlotTest {

    /** A vertical list at x 0..100: tops and heights of the own slot and of another slot. */
    private fun reached(
        ownTop: Float,
        ownHeight: Float,
        slotTop: Float,
        slotHeight: Float,
        draggedTop: Float,
        margin: Float = 0f,
    ): Boolean =
        hasReachedSlot(
            ownSlotCenter = Offset(50f, ownTop + ownHeight / 2f),
            slotCenter = Offset(50f, slotTop + slotHeight / 2f),
            draggedTopLeft = Offset(0f, draggedTop),
            draggedSize = Size(100f, ownHeight),
            marginPx = margin,
        )

    @Test
    fun slotBelow_isReachedWhenTheBottomEdgePassesItsMiddle() {
        // Own slot 0..100, the next one 100..300 with its middle at 200
        assertFalse(reached(ownTop = 0f, ownHeight = 100f, slotTop = 100f, slotHeight = 200f, draggedTop = 99f))
        assertTrue(reached(ownTop = 0f, ownHeight = 100f, slotTop = 100f, slotHeight = 200f, draggedTop = 100f))
    }

    @Test
    fun slotAbove_isReachedWhenTheTopEdgePassesItsMiddle() {
        // Own slot 200..300, the previous one 0..200 with its middle at 100
        assertFalse(reached(ownTop = 200f, ownHeight = 100f, slotTop = 0f, slotHeight = 200f, draggedTop = 101f))
        assertTrue(reached(ownTop = 200f, ownHeight = 100f, slotTop = 0f, slotHeight = 200f, draggedTop = 100f))
    }

    @Test
    fun afterTheSwap_theSamePositionDoesNotReachBack_whateverTheSizes() {
        val sizes = listOf(40f, 100f, 250f)
        for (own in sizes) {
            for (other in sizes) {
                // Before: own slot at 0, the other one below it. Dragged just far enough to swap
                val draggedTop = other / 2f
                assertTrue(
                    reached(ownTop = 0f, ownHeight = own, slotTop = own, slotHeight = other, draggedTop = draggedTop),
                    "A $own item should reach the $other slot below it",
                )
                // After: the other item is on top, the own slot below it
                assertFalse(
                    reached(ownTop = other, ownHeight = own, slotTop = 0f, slotHeight = other, draggedTop = draggedTop + 1f),
                    "A $own item must not swap back with the $other item",
                )
            }
        }
    }

    @Test
    fun margin_delaysTheSwapAndWidensTheWayBack() {
        assertFalse(reached(ownTop = 0f, ownHeight = 100f, slotTop = 100f, slotHeight = 100f, draggedTop = 53f, margin = 4f))
        assertTrue(reached(ownTop = 0f, ownHeight = 100f, slotTop = 100f, slotHeight = 100f, draggedTop = 54f, margin = 4f))
        // Swapped: undone only once the top edge is 4 px above the other middle, 8 px back in all
        assertFalse(reached(ownTop = 100f, ownHeight = 100f, slotTop = 0f, slotHeight = 100f, draggedTop = 47f, margin = 4f))
        assertTrue(reached(ownTop = 100f, ownHeight = 100f, slotTop = 0f, slotHeight = 100f, draggedTop = 46f, margin = 4f))
    }

    @Test
    fun slotInTheNextColumn_isReachedWhenTheSideEdgePassesItsMiddle() {
        fun reachedAt(draggedLeft: Float) = hasReachedSlot(
            ownSlotCenter = Offset(50f, 50f),
            slotCenter = Offset(170f, 50f),
            draggedTopLeft = Offset(draggedLeft, 0f),
            draggedSize = Size(100f, 100f),
            marginPx = 0f,
        )
        assertFalse(reachedAt(69f))
        assertTrue(reachedAt(70f))
    }

    @Test
    fun slotAtTheSamePlace_isNotReached() {
        assertFalse(
            hasReachedSlot(
                ownSlotCenter = Offset(50f, 50f),
                slotCenter = Offset(50f, 50f),
                draggedTopLeft = Offset.Zero,
                draggedSize = Size(100f, 100f),
                marginPx = 0f,
            ),
        )
    }
}
