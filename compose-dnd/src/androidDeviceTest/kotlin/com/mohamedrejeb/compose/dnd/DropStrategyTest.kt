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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class DropStrategyTest {

    /**
     * CenterDistance: when the dragged item's center is closer to target A's center,
     * it should hover A even if it overlaps more with target B.
     *
     * Layout:
     *   [draggable 100x100] at (0,0)
     *   [target-small 100x50] at (0,100)  ← center at y=125
     *   [target-large 100x200] at (0,150) ← center at y=250
     *
     * Drag down 80dp → item center at y=130, closer to target-small center (125)
     * than target-large center (250).
     */
    @Test
    fun centerDistance_selectsClosestCenter() = runComposeUiTest {
        var hoveredKey: Any? = null
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<String>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                hoveredKey = state.hoveredDropTargetKey

                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = "test",
                        dropStrategy = DropStrategy.CenterDistance,
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.size(100.dp))
                    }

                    // Small target right below
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(50.dp)
                            .dropTarget(key = "target-small", state = state),
                    )

                    // Large target further down
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(200.dp)
                            .dropTarget(key = "target-large", state = state),
                    )
                }
            }
        }

        waitForIdle()

        val distancePx = with(density) { 80.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }

        waitForIdle()
        assertEquals(
            "CenterDistance should pick target-small (closer center)",
            "target-small",
            hoveredKey,
        )

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    /**
     * SurfacePercentage: when the dragged item overlaps more (as a %) with target A
     * than target B, it should hover A.
     *
     * Layout:
     *   [draggable 100x100] at (0,0)
     *   [target-A 100x100] at (0,100)
     *   [target-B 100x100] at (0,200)
     *
     * Drag down 120dp → item occupies y=120..220
     *   Overlap with A (y=100..200): 80px → 80% of item
     *   Overlap with B (y=200..300): 20px → 20% of item
     * → Should pick target-A
     */
    @Test
    fun surfacePercentage_selectsLargestOverlapPercentage() = runComposeUiTest {
        var hoveredKey: Any? = null
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<String>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                hoveredKey = state.hoveredDropTargetKey

                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = "test",
                        dropStrategy = DropStrategy.SurfacePercentage,
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.size(100.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .dropTarget(key = "target-A", state = state),
                    )

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .dropTarget(key = "target-B", state = state),
                    )
                }
            }
        }

        waitForIdle()

        // Drag 120dp down — mostly overlapping target-A
        val distancePx = with(density) { 120.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }

        waitForIdle()
        assertEquals(
            "SurfacePercentage should pick target-A (larger overlap %)",
            "target-A",
            hoveredKey,
        )

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    /**
     * SurfacePercentage: when dragged past the midpoint between two targets,
     * the hover should switch to the second target.
     */
    @Test
    fun surfacePercentage_switchesWhenPastMidpoint() = runComposeUiTest {
        var hoveredKey: Any? = null
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<String>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                hoveredKey = state.hoveredDropTargetKey

                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = "test",
                        dropStrategy = DropStrategy.SurfacePercentage,
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.size(100.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .dropTarget(key = "target-A", state = state),
                    )

                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .dropTarget(key = "target-B", state = state),
                    )
                }
            }
        }

        waitForIdle()

        // Drag 180dp — mostly overlapping target-B now
        val distancePx = with(density) { 180.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }

        waitForIdle()
        assertEquals(
            "SurfacePercentage should switch to target-B past midpoint",
            "target-B",
            hoveredKey,
        )

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    /**
     * zIndex: when two targets overlap, the one with higher zIndex wins.
     */
    @Test
    fun zIndex_higherPriorityTargetWins() = runComposeUiTest {
        var droppedKey: String? = null
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<String>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = "test",
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.size(100.dp))
                    }

                    // Two overlapping targets at the same position
                    Box(modifier = Modifier.width(100.dp).height(200.dp)) {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .dropTarget(
                                    key = "low-z",
                                    state = state,
                                    zIndex = 0f,
                                    onDrop = { droppedKey = "low-z" },
                                ),
                        )
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .dropTarget(
                                    key = "high-z",
                                    state = state,
                                    zIndex = 1f,
                                    onDrop = { droppedKey = "high-z" },
                                ),
                        )
                    }
                }
            }
        }

        waitForIdle()

        val distancePx = with(density) { 200.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
            up()
        }

        waitForIdle()
        assertEquals("Higher zIndex target should win", "high-z", droppedKey)
    }
}
