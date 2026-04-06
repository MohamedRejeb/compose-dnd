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
import androidx.compose.ui.test.TouchInjectionScope

/**
 * Long-press then drag to a target position without releasing.
 * Coordinates are in the node's local coordinate space.
 */
fun TouchInjectionScope.longPressDrag(
    start: Offset,
    end: Offset,
    longPressMs: Long = 600,
    steps: Int = 20,
    stepDelayMs: Long = 16,
) {
    down(start)
    advanceEventTime(longPressMs)
    val dx = (end.x - start.x) / steps
    val dy = (end.y - start.y) / steps
    for (i in 1..steps) {
        advanceEventTime(stepDelayMs)
        moveTo(Offset(start.x + dx * i, start.y + dy * i))
    }
}

/**
 * Immediate drag (no long press) to a target position without releasing.
 */
fun TouchInjectionScope.immediateDrag(
    start: Offset,
    end: Offset,
    steps: Int = 20,
    stepDelayMs: Long = 16,
) {
    down(start)
    val dx = (end.x - start.x) / steps
    val dy = (end.y - start.y) / steps
    for (i in 1..steps) {
        advanceEventTime(stepDelayMs)
        moveTo(Offset(start.x + dx * i, start.y + dy * i))
    }
}
