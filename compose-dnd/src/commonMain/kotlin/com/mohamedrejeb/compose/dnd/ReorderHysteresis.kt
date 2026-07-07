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
import androidx.compose.ui.geometry.isUnspecified

/**
 * Directional hysteresis for post-reorder hover resolution: after a swap the
 * entered target is excluded from hover candidates until the cursor moves
 * [reverseDistancePx] opposite the swap direction, so a slow drag can't
 * oscillate the same swap while a deliberate drag-back can still undo it.
 * The swap direction is an EMA of per-frame movement to absorb jitter.
 */
internal class ReorderHysteresis(
    reverseDistancePx: Float = DEFAULT_REVERSE_DISTANCE_PX,
    private val directionSmoothing: Float = DEFAULT_DIRECTION_SMOOTHING,
) {
    /** Reverse distance in pixels; `<= 0` disables the hysteresis. */
    var reverseDistancePx: Float = reverseDistancePx

    /** Key of the currently-excluded drop target, or `null`. */
    var excludedKey: Any? = null
        private set

    /** Pointer position captured at arm time (the swap point). */
    private var enterOffset: Offset = Offset.Unspecified

    /** Normalized swap direction captured at arm time. */
    private var enterDirection: Offset = Offset.Zero

    /** Exponential moving average of recent per-frame movement. */
    private var smoothedDirection: Offset = Offset.Zero

    /**
     * Feed per-frame pointer movement; zero/unspecified deltas are ignored so
     * a paused pointer doesn't decay the tracked direction.
     */
    fun trackMovement(movementDelta: Offset) {
        if (movementDelta.isUnspecified || movementDelta == Offset.Zero) return

        smoothedDirection =
            if (smoothedDirection == Offset.Zero) {
                movementDelta
            } else {
                smoothedDirection + (movementDelta - smoothedDirection) * directionSmoothing
            }
    }

    /**
     * Exclude [key], recording the swap point and current direction.
     * No-op when disabled or when no movement has been tracked yet.
     */
    fun arm(key: Any, pointerOffset: Offset) {
        if (reverseDistancePx <= 0f) return
        if (pointerOffset.isUnspecified) return

        val distance = smoothedDirection.getDistance()
        if (distance <= 0f) return

        excludedKey = key
        enterOffset = pointerOffset
        enterDirection = smoothedDirection / distance
    }

    /** Lift the exclusion once [pointerOffset] moved past [reverseDistancePx] against the armed direction. */
    fun liftIfReversed(pointerOffset: Offset) {
        if (excludedKey == null || enterOffset.isUnspecified || pointerOffset.isUnspecified) {
            return
        }

        val displacement = pointerOffset - enterOffset
        val projection =
            displacement.x * enterDirection.x + displacement.y * enterDirection.y
        if (projection < -reverseDistancePx) {
            clearExclusion()
        }
    }

    /** Clear all state; called on drag start and drag end. */
    fun reset() {
        clearExclusion()
        smoothedDirection = Offset.Zero
    }

    private fun clearExclusion() {
        excludedKey = null
        enterOffset = Offset.Unspecified
        enterDirection = Offset.Zero
    }

    internal companion object {
        /** Fallback in px; [rememberDragAndDropState] overrides it with a density-converted Dp. */
        const val DEFAULT_REVERSE_DISTANCE_PX = 8f

        /** EMA factor in (0, 1]; lower rejects jitter harder at the cost of lag. */
        const val DEFAULT_DIRECTION_SMOOTHING = 0.35f
    }
}
