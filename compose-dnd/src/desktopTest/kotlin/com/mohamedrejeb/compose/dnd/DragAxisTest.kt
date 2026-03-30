/*
 * Copyright 2025, Mohamed Ben Rejeb and the Compose Dnd project contributors
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DragAxis
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DragAxisTest {

    private fun TouchInjectionScope.simulateDrag(
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
        up()
    }

    @Test
    fun horizontalAxis_dropsOnHorizontalTarget() = runComposeUiTest {
        var droppedOnRight = false

        setContent {
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                Row {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = 1,
                        dragAxis = DragAxis.Horizontal,
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.width(100.dp).fillMaxHeight())
                    }

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .fillMaxHeight()
                            .dropTarget(
                                key = "right-target",
                                state = state,
                                onDrop = { droppedOnRight = true },
                            )
                    )
                }
            }
        }

        // Drag diagonally — but axis lock means only horizontal movement counts
        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x + 250f, center.y + 200f),
            )
        }

        waitForIdle()
        assertTrue(droppedOnRight, "Should drop on the right target (horizontal movement)")
    }

    @Test
    fun horizontalAxis_doesNotDropOnVerticalTarget() = runComposeUiTest {
        var droppedOnBottom = false

        setContent {
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(600.dp),
            ) {
                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = 1,
                        dragAxis = DragAxis.Horizontal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.fillMaxWidth().height(100.dp))
                    }

                    // Spacer to separate item from target vertically
                    Box(Modifier.fillMaxWidth().height(100.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .dropTarget(
                                key = "bottom-target",
                                state = state,
                                onDrop = { droppedOnBottom = true },
                            )
                    )
                }
            }
        }

        // Drag straight down — horizontal lock zeroes vertical, so no intersection with bottom target
        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 400f),
            )
        }

        waitForIdle()
        assertFalse(droppedOnBottom, "Should NOT drop on bottom target with horizontal axis lock")
    }

    @Test
    fun verticalAxis_dropsOnVerticalTarget() = runComposeUiTest {
        var droppedOnBottom = false

        setContent {
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = 1,
                        dragAxis = DragAxis.Vertical,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.fillMaxWidth().height(100.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .dropTarget(
                                key = "bottom-target",
                                state = state,
                                onDrop = { droppedOnBottom = true },
                            )
                    )
                }
            }
        }

        // Drag diagonally — vertical lock means only vertical movement counts
        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x + 200f, center.y + 300f),
            )
        }

        waitForIdle()
        assertTrue(droppedOnBottom, "Should drop on bottom target (vertical movement)")
    }

    @Test
    fun verticalAxis_doesNotDropOnHorizontalTarget() = runComposeUiTest {
        var droppedOnRight = false

        setContent {
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(600.dp),
            ) {
                Row {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = 1,
                        dragAxis = DragAxis.Vertical,
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.width(100.dp).fillMaxHeight())
                    }

                    // Spacer to separate item from target horizontally
                    Box(Modifier.width(100.dp).fillMaxHeight())

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .fillMaxHeight()
                            .dropTarget(
                                key = "right-target",
                                state = state,
                                onDrop = { droppedOnRight = true },
                            )
                    )
                }
            }
        }

        // Drag straight right — vertical lock zeroes horizontal, so no intersection
        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x + 400f, center.y),
            )
        }

        waitForIdle()
        assertFalse(droppedOnRight, "Should NOT drop on right target with vertical axis lock")
    }
}
