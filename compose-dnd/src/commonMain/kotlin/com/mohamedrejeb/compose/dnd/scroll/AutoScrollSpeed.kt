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
package com.mohamedrejeb.compose.dnd.scroll

import kotlin.math.max
import kotlin.math.min

/** An edge zone never takes more than this share of the viewport, so the middle stays free. */
private const val MaxEdgeZoneFraction = 0.25f

/**
 * Size of the zone along a container edge where a dragged item triggers auto-scroll:
 * [preferredPx] within [minPx]..[maxPx], capped to a quarter of the viewport.
 */
internal fun edgeZoneSize(
    preferredPx: Float,
    minPx: Float,
    maxPx: Float,
    viewportPx: Float,
): Float =
    preferredPx
        .coerceIn(minPx, maxPx)
        .coerceAtMost(viewportPx * MaxEdgeZoneFraction)

/**
 * Tells a deliberate move toward an edge from an item that merely starts there.
 *
 * An item picked up inside an edge zone must not scroll the list under the
 * finger right away: that zone stays unarmed until the pointer moves toward
 * its edge by [intentDistancePx], or the item leaves the zone and comes back.
 */
internal class AutoScrollIntent(
    private val intentDistancePx: Float,
) {
    private var isInitialized = false

    var isStartArmed = true
        private set

    var isEndArmed = true
        private set

    fun reset() {
        isInitialized = false
        isStartArmed = true
        isEndArmed = true
    }

    /**
     * @param movedTowardEnd how far the pointer moved along the scroll axis
     * since the drag started; negative toward the start edge.
     */
    fun update(
        isInStartZone: Boolean,
        isInEndZone: Boolean,
        movedTowardEnd: Float,
    ) {
        if (!isInitialized) {
            isInitialized = true
            isStartArmed = !isInStartZone
            isEndArmed = !isInEndZone
        }

        if (!isInStartZone || -movedTowardEnd >= intentDistancePx) isStartArmed = true
        if (!isInEndZone || movedTowardEnd >= intentDistancePx) isEndArmed = true
    }
}

/**
 * Computes the auto-scroll speed along the scroll axis, in px per second.
 *
 * The edges of the dragged item trigger the scroll, so it starts as soon as the
 * item reaches an edge zone. An item too large to fit between the two zones would
 * sit in both all the time: its edges are followed no further from the pointer
 * than half the free space, so the pointer decides for large items.
 *
 * The speed follows a quadratic ease-in curve (ratio^2): it starts gently and
 * accelerates as the item gets closer to the edge.
 *
 * @param pointer pointer position on the scroll axis, or NaN when unknown.
 * @param canScrollTowardStart false when the content can't scroll further toward the start edge.
 * @param canScrollTowardEnd false when the content can't scroll further toward the end edge.
 *
 * @return a negative value to scroll toward the start edge (up / left), a positive
 * one toward the end edge (down / right), or zero.
 */
internal fun computeEdgeScrollSpeed(
    itemStart: Float,
    itemEnd: Float,
    pointer: Float,
    containerStart: Float,
    containerEnd: Float,
    startZonePx: Float,
    endZonePx: Float,
    maxScrollSpeed: Float,
    intent: AutoScrollIntent,
    movedTowardEnd: Float,
    canScrollTowardStart: Boolean,
    canScrollTowardEnd: Boolean,
): Float {
    val freeSpace = (containerEnd - containerStart - startZonePx - endZonePx).coerceAtLeast(0f)
    val maxLead = freeSpace / 2f
    val leadingStart = if (pointer.isNaN()) itemStart else max(itemStart, pointer - maxLead)
    val leadingEnd = if (pointer.isNaN()) itemEnd else min(itemEnd, pointer + maxLead)

    // How far into each edge zone the item is
    val startOverlap = (containerStart + startZonePx) - leadingStart
    val endOverlap = leadingEnd - (containerEnd - endZonePx)

    intent.update(
        isInStartZone = startOverlap > 0f,
        isInEndZone = endOverlap > 0f,
        movedTowardEnd = movedTowardEnd,
    )

    val startSpeed = if (canScrollTowardStart && intent.isStartArmed) {
        zoneSpeed(startOverlap, startZonePx, maxScrollSpeed)
    } else {
        0f
    }
    val endSpeed = if (canScrollTowardEnd && intent.isEndArmed) {
        zoneSpeed(endOverlap, endZonePx, maxScrollSpeed)
    } else {
        0f
    }

    return if (endSpeed >= startSpeed) endSpeed else -startSpeed
}

private fun zoneSpeed(
    overlap: Float,
    zonePx: Float,
    maxScrollSpeed: Float,
): Float {
    if (overlap <= 0f || zonePx <= 0f) return 0f

    val ratio = (overlap / zonePx).coerceIn(0f, 1f)
    return maxScrollSpeed * ratio * ratio
}
