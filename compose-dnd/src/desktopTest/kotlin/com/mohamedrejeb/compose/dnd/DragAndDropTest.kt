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
class DragAndDropTest {

    /**
     * Simulate a drag by generating intermediate pointer events.
     * Compose gesture detectors need multiple move events to detect a drag.
     */
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

    /**
     * Simulate a drag without releasing (hold the pointer).
     */
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
        // Don't call up() — pointer stays held
    }

    @Test
    fun dragItemToDropTarget_triggersOnDrop() = runComposeUiTest {
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
        assertTrue(dropped, "onDrop should have been called")
    }

    @Test
    fun dragCancel_doesNotTriggerOnDrop() = runComposeUiTest {
        var dropped = false

        setContent {
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                DraggableItem(
                    state = state,
                    key = "item",
                    data = 1,
                    modifier = Modifier
                        .size(100.dp)
                        .testTag("draggable"),
                ) {
                    Box(Modifier.size(100.dp))
                }
                // No drop target — drag ends without drop
            }
        }

        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x + 50f, center.y + 50f),
            )
        }

        waitForIdle()
        assertFalse(dropped, "onDrop should NOT be called when no target")
    }

    @Test
    fun hoveredDropTargetKey_updatesOnDragOver() = runComposeUiTest {
        var hoveredKey: Any? = null

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
                                key = "target",
                                state = state,
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
        assertEquals("target", hoveredKey, "Should be hovering over the drop target")

        // Release
        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun onDragEnter_calledWhenEnteringTarget() = runComposeUiTest {
        var enterCount = 0

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
                                onDragEnter = { enterCount++ },
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
        assertTrue(enterCount > 0, "onDragEnter should have been called at least once")
    }

    @Test
    fun dropData_isPassedCorrectly() = runComposeUiTest {
        var receivedData: Int? = null

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
                        data = 42,
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
                                onDrop = { receivedData = it.data },
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
        assertEquals(42, receivedData, "Dropped data should match the draggable's data")
    }
}
