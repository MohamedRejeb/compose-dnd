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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class CanDropTest {

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

    private fun TouchInjectionScope.simulateDragHold(
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

    @Test
    fun canDropFalse_preventsDropOnTarget() = runComposeUiTest {
        var dropped = false

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
                                key = "target",
                                state = state,
                                canDrop = false,
                                onDrop = { dropped = true },
                            )
                    )
                }
            }
        }

        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 300f),
            )
        }

        waitForIdle()
        assertFalse(dropped, "onDrop should NOT be called when canDrop is false")
    }

    @Test
    fun canDropFalse_preventsHover() = runComposeUiTest {
        var hoveredKey: Any = ""

        setContent {
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                hoveredKey = state.hoveredDropTargetKey

                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = 1,
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
                                key = "disabled-target",
                                state = state,
                                canDrop = false,
                            )
                    )
                }
            }
        }

        onNodeWithTag("draggable").performTouchInput {
            simulateDragHold(
                start = center,
                end = Offset(center.x, center.y + 200f),
            )
        }

        waitForIdle()
        assertEquals("", hoveredKey, "Should NOT hover over a target with canDrop=false")

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun canDropTrue_allowsDrop() = runComposeUiTest {
        var dropped = false

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
                                key = "target",
                                state = state,
                                canDrop = true,
                                onDrop = { dropped = true },
                            )
                    )
                }
            }
        }

        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 300f),
            )
        }

        waitForIdle()
        assertTrue(dropped, "onDrop should be called when canDrop is true")
    }

    @Test
    fun canDropFalse_skipsToNextValidTarget() = runComposeUiTest {
        var droppedOnFirst = false
        var droppedOnSecond = false

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.fillMaxWidth().height(80.dp))
                    }

                    // First target — disabled
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .dropTarget(
                                key = "disabled",
                                state = state,
                                canDrop = false,
                                onDrop = { droppedOnFirst = true },
                            )
                    )

                    // Second target — enabled
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .dropTarget(
                                key = "enabled",
                                state = state,
                                canDrop = true,
                                onDrop = { droppedOnSecond = true },
                            )
                    )
                }
            }
        }

        // Drag past the disabled target into the enabled one
        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 350f),
            )
        }

        waitForIdle()
        assertFalse(droppedOnFirst, "Should NOT drop on disabled target")
        assertTrue(droppedOnSecond, "Should drop on the enabled target")
    }
}
