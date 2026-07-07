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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReorderHysteresisTest {

    private val upDelta = Offset(0f, -4f) // a frame moving up
    private val downDelta = Offset(0f, 4f) // a frame moving down

    /** Track a single [direction] frame then arm, the common setup. */
    private fun ReorderHysteresis.armAfter(
        direction: Offset,
        key: Any = "A",
        at: Offset = Offset.Zero,
    ) {
        trackMovement(direction)
        arm(key, at)
    }

    // -- arming --

    @Test
    fun initialStateHasNoExcludedKey() {
        val hysteresis = ReorderHysteresis()

        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun armAfterMovementSetsExcludedKey() {
        val hysteresis = ReorderHysteresis()

        hysteresis.armAfter(upDelta)

        assertEquals("A", hysteresis.excludedKey)
    }

    @Test
    fun armWithoutTrackedMovementIsIgnored() {
        val hysteresis = ReorderHysteresis()

        hysteresis.arm("A", Offset.Zero) // no trackMovement first -> no direction

        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun trackMovementIgnoresZero() {
        val hysteresis = ReorderHysteresis()

        hysteresis.trackMovement(Offset.Zero)
        hysteresis.arm("A", Offset.Zero)

        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun trackMovementIgnoresUnspecified() {
        val hysteresis = ReorderHysteresis()

        hysteresis.trackMovement(Offset.Unspecified)
        hysteresis.arm("A", Offset.Zero)

        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun armWithUnspecifiedPointerIsIgnored() {
        val hysteresis = ReorderHysteresis()

        hysteresis.trackMovement(upDelta)
        hysteresis.arm("A", Offset.Unspecified)

        assertNull(hysteresis.excludedKey)
    }

    // -- lifting on reverse --

    @Test
    fun reverseBeyondThresholdLiftsExclusion() {
        val hysteresis = ReorderHysteresis() // default 8px reverse distance

        hysteresis.armAfter(upDelta)
        // armed direction is up; move down 9px, past the 8px reverse threshold
        hysteresis.liftIfReversed(Offset(0f, 9f))

        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun reverseExactlyAtThresholdKeepsExclusion() {
        val hysteresis = ReorderHysteresis()

        hysteresis.armAfter(upDelta)
        // projection == -8 is NOT strictly less than -8, so the exclusion holds
        hysteresis.liftIfReversed(Offset(0f, 8f))

        assertEquals("A", hysteresis.excludedKey)
    }

    @Test
    fun reverseBelowThresholdKeepsExclusion() {
        val hysteresis = ReorderHysteresis()

        hysteresis.armAfter(upDelta)
        hysteresis.liftIfReversed(Offset(0f, 5f))

        assertEquals("A", hysteresis.excludedKey)
    }

    @Test
    fun continuingSameDirectionKeepsExclusion() {
        val hysteresis = ReorderHysteresis()

        hysteresis.armAfter(upDelta)
        // keep moving up (same direction) by a large amount
        hysteresis.liftIfReversed(Offset(0f, -100f))

        assertEquals("A", hysteresis.excludedKey)
    }

    @Test
    fun perpendicularMovementKeepsExclusion() {
        val hysteresis = ReorderHysteresis()

        hysteresis.armAfter(upDelta)
        // move sideways; projection onto the vertical arm direction is ~0
        hysteresis.liftIfReversed(Offset(100f, 0f))

        assertEquals("A", hysteresis.excludedKey)
    }

    @Test
    fun liftIfReversedIsNoOpWhenNotArmed() {
        val hysteresis = ReorderHysteresis()

        hysteresis.liftIfReversed(Offset(0f, 1000f))

        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun liftIfReversedIgnoresUnspecifiedPointer() {
        val hysteresis = ReorderHysteresis()

        hysteresis.armAfter(upDelta)
        hysteresis.liftIfReversed(Offset.Unspecified)

        assertEquals("A", hysteresis.excludedKey)
    }

    @Test
    fun reverseIsMeasuredFromTheSwapPointNotOrigin() {
        val hysteresis = ReorderHysteresis()

        // The swap happened at y=200 while moving up.
        hysteresis.armAfter(upDelta, at = Offset(0f, 200f))
        // Moving back down to y=205 is only +5 from the swap point -> keep.
        hysteresis.liftIfReversed(Offset(0f, 205f))
        assertEquals("A", hysteresis.excludedKey)
        // Down to y=210 is +10 from the swap point -> lift.
        hysteresis.liftIfReversed(Offset(0f, 210f))
        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun diagonalDirectionIsNormalizedForReverse() {
        val hysteresis = ReorderHysteresis() // 8px threshold

        // 3-4-5 triangle: distance 5, normalized direction (0.6, 0.8).
        hysteresis.armAfter(Offset(3f, 4f))
        // Reverse along (-0.6, -0.8): displacement (-6, -8) -> projection -10 < -8.
        hysteresis.liftIfReversed(Offset(-6f, -8f))

        assertNull(hysteresis.excludedKey)
    }

    // -- direction smoothing / jitter robustness (#5) --

    @Test
    fun smoothedDirectionSurvivesSmallSidewaysJitter() {
        val hysteresis = ReorderHysteresis()

        // An established upward drag...
        repeat(4) { hysteresis.trackMovement(upDelta) }
        // ...then a small sideways jitter frame right at swap time.
        hysteresis.trackMovement(Offset(2f, 0f))
        hysteresis.arm("A", Offset.Zero)

        // Direction is still essentially vertical, so a downward reverse lifts
        hysteresis.liftIfReversed(Offset(0f, 9f))
        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun singleContraryFrameDoesNotFlipDirection() {
        val hysteresis = ReorderHysteresis()

        repeat(4) { hysteresis.trackMovement(upDelta) }
        hysteresis.trackMovement(downDelta) // one contrary frame
        hysteresis.arm("A", Offset.Zero)

        // Net direction is still up, so dragging back down still lifts.
        hysteresis.liftIfReversed(Offset(0f, 9f))
        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun smoothedDirectionIsPreservedAcrossLift() {
        val hysteresis = ReorderHysteresis()

        repeat(3) { hysteresis.trackMovement(upDelta) }
        hysteresis.arm("A", Offset.Zero)
        hysteresis.liftIfReversed(Offset(0f, 9f)) // reverse -> lift
        assertNull(hysteresis.excludedKey)

        // No new movement tracked; arm still works off the preserved direction.
        hysteresis.arm("B", Offset(0f, 9f))
        assertEquals("B", hysteresis.excludedKey)
    }

    // -- configurable / disable-able distance (#3) --

    @Test
    fun customReverseDistanceFromConstructorIsRespected() {
        val hysteresis = ReorderHysteresis(reverseDistancePx = 20f)

        hysteresis.armAfter(upDelta)
        hysteresis.liftIfReversed(Offset(0f, 15f)) // 15 < 20 -> keep
        assertEquals("A", hysteresis.excludedKey)
        hysteresis.liftIfReversed(Offset(0f, 25f)) // 25 > 20 -> lift
        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun reverseDistanceCanBeChangedAfterConstruction() {
        val hysteresis = ReorderHysteresis()

        hysteresis.reverseDistancePx = 20f
        hysteresis.armAfter(upDelta)
        hysteresis.liftIfReversed(Offset(0f, 15f))
        assertEquals("A", hysteresis.excludedKey)
        hysteresis.liftIfReversed(Offset(0f, 25f))
        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun zeroDistanceDisablesArming() {
        val hysteresis = ReorderHysteresis(reverseDistancePx = 0f)

        hysteresis.armAfter(upDelta)

        assertNull(hysteresis.excludedKey)
    }

    // -- reset --

    @Test
    fun resetClearsExclusion() {
        val hysteresis = ReorderHysteresis()

        hysteresis.armAfter(upDelta)
        hysteresis.reset()

        assertNull(hysteresis.excludedKey)
    }

    @Test
    fun resetClearsSmoothedDirection() {
        val hysteresis = ReorderHysteresis()

        hysteresis.trackMovement(upDelta)
        hysteresis.reset()
        hysteresis.arm("A", Offset.Zero) // no direction after reset

        assertNull(hysteresis.excludedKey)
    }
}
