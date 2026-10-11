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

import com.mohamedrejeb.compose.dnd.scroll.AutoScrollIntent
import com.mohamedrejeb.compose.dnd.scroll.computeEdgeScrollSpeed
import com.mohamedrejeb.compose.dnd.scroll.edgeZoneSize
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutoScrollSpeedTest {

    private val intent = AutoScrollIntent(intentDistancePx = IntentDistance)

    /** A container from 0 to [container] with [Zone]-wide edge zones. */
    private fun speed(
        itemStart: Float,
        itemEnd: Float,
        pointer: Float = (itemStart + itemEnd) / 2f,
        movedTowardEnd: Float = 0f,
        container: Float = Container,
        canScrollTowardStart: Boolean = true,
        canScrollTowardEnd: Boolean = true,
    ): Float =
        computeEdgeScrollSpeed(
            itemStart = itemStart,
            itemEnd = itemEnd,
            pointer = pointer,
            containerStart = 0f,
            containerEnd = container,
            startZonePx = Zone,
            endZonePx = Zone,
            maxScrollSpeed = MaxSpeed,
            intent = intent,
            movedTowardEnd = movedTowardEnd,
            canScrollTowardStart = canScrollTowardStart,
            canScrollTowardEnd = canScrollTowardEnd,
        )

    @Test
    fun edgeZone_isCappedToAQuarterOfTheViewport() {
        assertEquals(100f, edgeZoneSize(preferredPx = 300f, minPx = 48f, maxPx = 160f, viewportPx = 400f))
        assertEquals(160f, edgeZoneSize(preferredPx = 300f, minPx = 48f, maxPx = 160f, viewportPx = 1000f))
        assertEquals(48f, edgeZoneSize(preferredPx = 10f, minPx = 48f, maxPx = 160f, viewportPx = 1000f))
    }

    @Test
    fun itemInTheMiddle_doesNotScroll() {
        assertEquals(0f, speed(itemStart = 450f, itemEnd = 550f))
    }

    @Test
    fun smallItem_scrollsAsSoonAsItsEdgeReachesAZone() {
        assertEquals(0f, speed(itemStart = 450f, itemEnd = 550f))

        // The pointer is still far from the zone, the item's edge is halfway in
        val towardEnd = speed(itemStart = 850f, itemEnd = 950f, movedTowardEnd = 400f)
        assertEquals(MaxSpeed * 0.25f, towardEnd)

        val towardStart = speed(itemStart = 0f, itemEnd = 100f, movedTowardEnd = -450f)
        assertEquals(-MaxSpeed, towardStart)
    }

    @Test
    fun itemWiderThanTheFreeSpace_doesNotScrollWhileThePointerIsInTheMiddle() {
        // 400 wide container: 100 + 200 free + 100. The 280 wide item overlaps both zones.
        assertEquals(0f, speed(itemStart = 60f, itemEnd = 340f, pointer = 200f, container = 400f))
    }

    @Test
    fun itemWiderThanTheFreeSpace_scrollsOnceThePointerHeadsToAnEdge() {
        assertEquals(0f, speed(itemStart = 60f, itemEnd = 340f, pointer = 200f, container = 400f))

        val towardEnd =
            speed(itemStart = 110f, itemEnd = 390f, pointer = 250f, movedTowardEnd = 50f, container = 400f)
        assertEquals(MaxSpeed * 0.25f, towardEnd)
    }

    @Test
    fun itemPickedUpInsideAZone_waitsForAMoveTowardThatEdge() {
        // Grabbed near the end edge: no scroll under the finger on pick-up
        assertEquals(0f, speed(itemStart = 880f, itemEnd = 980f))
        assertEquals(0f, speed(itemStart = 884f, itemEnd = 984f, movedTowardEnd = 4f))

        assertTrue(speed(itemStart = 888f, itemEnd = 988f, movedTowardEnd = IntentDistance) > 0f)
        // Once armed it keeps scrolling, even if the pointer drifts back a little
        assertTrue(speed(itemStart = 882f, itemEnd = 982f, movedTowardEnd = 2f) > 0f)
    }

    @Test
    fun itemPickedUpInsideAZone_scrollsAfterLeavingAndComingBack() {
        assertEquals(0f, speed(itemStart = 880f, itemEnd = 980f))
        assertEquals(0f, speed(itemStart = 700f, itemEnd = 800f, movedTowardEnd = -180f))

        assertTrue(speed(itemStart = 870f, itemEnd = 970f, movedTowardEnd = -10f) > 0f)
    }

    @Test
    fun zoneThatCannotScroll_leavesTheOtherOneWorking() {
        // Wide item at the start of the list, pointer heading to the end edge
        val towardEnd = speed(
            itemStart = 90f,
            itemEnd = 370f,
            pointer = 290f,
            movedTowardEnd = 60f,
            container = 400f,
            canScrollTowardStart = false,
        )

        assertTrue(towardEnd > 0f, "Should scroll toward the end, was $towardEnd")
    }

    @Test
    fun nothingLeftToScroll_staysIdle() {
        val towardEnd = speed(itemStart = 900f, itemEnd = 1000f, movedTowardEnd = 400f, canScrollTowardEnd = false)

        assertEquals(0f, towardEnd)
    }

    @Test
    fun reset_forgetsTheIntentOfThePreviousDrag() {
        assertEquals(0f, speed(itemStart = 880f, itemEnd = 980f))
        assertTrue(speed(itemStart = 890f, itemEnd = 990f, movedTowardEnd = 10f) > 0f)

        intent.reset()

        assertEquals(0f, speed(itemStart = 880f, itemEnd = 980f))
    }

    private companion object {
        const val Container = 1000f
        const val Zone = 100f
        const val MaxSpeed = 600f
        const val IntentDistance = 8f
    }
}
