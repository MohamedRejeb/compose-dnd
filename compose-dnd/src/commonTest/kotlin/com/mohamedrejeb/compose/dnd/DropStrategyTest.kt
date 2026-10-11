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

import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drop.DropTargetState
import kotlin.test.Test
import kotlin.test.assertEquals

class DropStrategyTest {

    private fun target(key: String, topLeft: Offset, size: Size, zIndex: Float = 0f) =
        DropTargetState<String>(
            key = key,
            zIndex = zIndex,
            size = size,
            topLeft = topLeft,
            dropAlignment = Alignment.Center,
            dropOffset = Offset.Zero,
            dropAnimationEnabled = true,
            canDrop = true,
            onDrop = {},
            onDragEnter = {},
            onDragExit = {},
        )

    // A 100x100 item covers the whole small target and a larger part of the big one
    private val itemTopLeft = Offset(150f, 0f)
    private val itemSize = Size(100f, 100f)
    private val big = target("big", topLeft = Offset(0f, 0f), size = Size(210f, 400f))
    private val small = target("small", topLeft = Offset(210f, 0f), size = Size(40f, 40f))

    private fun DropStrategy.pick(vararg targets: DropTargetState<String>): Any? =
        getHoveredDropTarget(itemTopLeft, itemSize, targets.toList())?.key

    @Test
    fun surface_picksTheLargestOverlap() {
        assertEquals("big", DropStrategy.Surface.pick(big, small))
    }

    @Test
    fun surfacePercentage_picksTheTargetCoveredTheMost() {
        assertEquals("small", DropStrategy.SurfacePercentage.pick(big, small))
    }

    @Test
    fun surfacePercentage_sameSizeTargets_picksTheLargestOverlap() {
        val left = target("left", topLeft = Offset(0f, 0f), size = Size(180f, 100f))
        val right = target("right", topLeft = Offset(180f, 0f), size = Size(180f, 100f))

        assertEquals("right", DropStrategy.SurfacePercentage.pick(left, right))
        assertEquals("right", DropStrategy.Surface.pick(left, right))
    }

    @Test
    fun surfacePercentage_higherZIndexWins() {
        val raisedBig = target("big", topLeft = Offset(0f, 0f), size = Size(210f, 400f), zIndex = 1f)

        assertEquals("big", DropStrategy.SurfacePercentage.pick(raisedBig, small))
    }

    @Test
    fun surfacePercentage_ignoresAnEmptyTarget() {
        val empty = target("empty", topLeft = Offset(200f, 50f), size = Size.Zero)

        assertEquals("big", DropStrategy.SurfacePercentage.pick(big, empty))
    }
}
