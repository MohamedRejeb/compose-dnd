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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DragToTargetDesktopTest {

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
    }

    @Test
    fun dragItem_toDropTarget_triggersOnDrop() = runComposeUiTest {
        var dropped = false
        var droppedData: Int? = null

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
                                onDrop = {
                                    dropped = true
                                    droppedData = it.data
                                },
                            ),
                    )
                }
            }
        }

        waitForIdle()

        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 300f),
            )
            up()
        }

        waitForIdle()
        assertTrue(dropped, "onDrop should have been called")
        assertEquals(42, droppedData, "Dropped data should be 42")
    }

    @Test
    fun dragItem_released_outsideTarget_doesNotDrop() = runComposeUiTest {
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

                    Box(Modifier.fillMaxWidth().height(200.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .dropTarget(
                                key = "target",
                                state = state,
                                onDrop = { dropped = true },
                            ),
                    )
                }
            }
        }

        waitForIdle()

        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 50f),
            )
            up()
        }

        waitForIdle()
        assertFalse(dropped, "onDrop should NOT be called when not over target")
    }

    @Test
    fun dragItem_onDragEnter_calledWhenHoveringTarget() = runComposeUiTest {
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
                            ),
                    )
                }
            }
        }

        waitForIdle()

        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 300f),
            )
        }

        waitForIdle()
        assertTrue(enterCount > 0, "onDragEnter should have been called")

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun enabled_false_preventsDrag() = runComposeUiTest {
        var isActive = false

        setContent {
            val state = rememberDragAndDropState<String>()
            isActive = state.isActiveDrag

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                DraggableItem(
                    state = state,
                    key = "item",
                    data = "test",
                    enabled = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("draggable"),
                ) {
                    Box(Modifier.fillMaxWidth().height(100.dp))
                }
            }
        }

        waitForIdle()

        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 50f),
            )
        }
        waitForIdle()

        assertFalse(isActive, "Drag should not start when enabled=false")

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun hoveredDropTargetKey_clearsAfterDragEnd() = runComposeUiTest {
        var hoveredKey: Any? = "initial"

        setContent {
            val state = rememberDragAndDropState<String>()
            hoveredKey = state.hoveredDropTargetKey

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
                            .dropTarget(key = "target", state = state),
                    )
                }
            }
        }

        waitForIdle()
        assertNull(hoveredKey, "No hovered key initially")

        onNodeWithTag("draggable").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 300f),
            )
        }
        waitForIdle()
        assertEquals("target", hoveredKey, "Should hover target")

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
        assertNull(hoveredKey, "Hovered key should clear after drop")
    }
}
