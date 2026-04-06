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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class DragToTargetTest {

    @Test
    fun dragItem_toDropTarget_triggersOnDrop() = runComposeUiTest {
        var dropped = false
        var droppedData: Int? = null
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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
                            )
                            .testTag("target"),
                    )
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
        assertTrue("onDrop should have been called", dropped)
        assertEquals("Dropped data should be 42", 42, droppedData)
    }

    @Test
    fun dragItem_released_outsideTarget_doesNotDrop() = runComposeUiTest {
        var dropped = false
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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

                    // Gap with no drop target
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

        // Drag only 50dp — stays within the gap, not reaching the target
        val distancePx = with(density) { 50.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
            up()
        }

        waitForIdle()
        assertFalse("onDrop should NOT be called when not over target", dropped)
    }

    @Test
    fun dragItem_onDragEnter_calledWhenHoveringTarget() = runComposeUiTest {
        var enterCount = 0
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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

        val distancePx = with(density) { 200.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }

        waitForIdle()
        assertTrue("onDragEnter should have been called", enterCount > 0)

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun dragItem_onDragExit_calledWhenLeavingTarget() = runComposeUiTest {
        var exitCount = 0
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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
                            .height(100.dp)
                            .dropTarget(
                                key = "target",
                                state = state,
                                onDragExit = { exitCount++ },
                            ),
                    )

                    // Area below the target
                    Box(Modifier.fillMaxWidth().height(200.dp))
                }
            }
        }

        waitForIdle()

        // Drag through the target and past it
        val distancePx = with(density) { 300.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }

        waitForIdle()
        assertTrue("onDragExit should have been called", exitCount > 0)

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun dragItem_hoveredDropTargetKey_updatesOnHover() = runComposeUiTest {
        var hoveredKey: Any? = null
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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
                                key = "my-target",
                                state = state,
                            ),
                    )
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
        }

        waitForIdle()
        assertEquals("Should be hovering over my-target", "my-target", hoveredKey)

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun dragItem_canDropFalse_doesNotAcceptDrop() = runComposeUiTest {
        var dropped = false
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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
                            ),
                    )
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
        assertFalse("onDrop should NOT be called when canDrop=false", dropped)
    }

    @Test
    fun dragItem_dropTargetRestriction_skipsDisallowedTarget() = runComposeUiTest {
        var droppedOnB = false
        var hoveredKey: Any? = null
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                hoveredKey = state.hoveredDropTargetKey

                Column {
                    // Item restricted to "target-a" only
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = 1,
                        dropTargets = listOf("target-a"),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.fillMaxWidth().height(100.dp))
                    }

                    // Target B — item should NOT interact with it
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .dropTarget(
                                key = "target-b",
                                state = state,
                                onDrop = { droppedOnB = true },
                            ),
                    )
                }
            }
        }

        waitForIdle()

        val distancePx = with(density) { 200.dp.toPx() }

        // Drag onto target-b
        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }

        waitForIdle()
        // Item shouldn't hover target-b because it's not in dropTargets
        assertTrue(
            "Should NOT hover target-b (restricted to target-a only)",
            hoveredKey != "target-b",
        )

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
        assertFalse("Should NOT drop on target-b", droppedOnB)
    }
}
