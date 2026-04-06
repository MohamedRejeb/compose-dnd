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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.mohamedrejeb.compose.dnd.drag.DragAxis
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class DragAxisTest {

    @Test
    fun verticalAxis_onlyAllowsVerticalDrag_dropsOnVerticalTarget() = runComposeUiTest {
        var droppedVertical = false
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
                                key = "target-below",
                                state = state,
                                onDrop = { droppedVertical = true },
                            ),
                    )
                }
            }
        }

        waitForIdle()

        val distancePx = with(density) { 200.dp.toPx() }

        // Drag diagonally — only vertical component should apply
        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x + distancePx, center.y + distancePx),
            )
            up()
        }

        waitForIdle()
        assertTrue("Should drop on vertical target even with diagonal gesture", droppedVertical)
    }

    @Test
    fun horizontalAxis_verticalDragComponent_isZeroed() = runComposeUiTest {
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
                        dragAxis = DragAxis.Horizontal,
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.width(100.dp).height(100.dp))
                    }

                    // Target below — horizontal drag should not reach it
                    // because vertical component is zeroed
                    Box(Modifier.height(50.dp)) // gap
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .dropTarget(
                                key = "target-below",
                                state = state,
                            ),
                    )
                }
            }
        }

        waitForIdle()

        val distancePx = with(density) { 200.dp.toPx() }

        // Drag purely downward — horizontal axis zeroes Y, so item stays in place
        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }

        waitForIdle()
        // The item didn't actually move vertically, so it can't reach the target
        assertTrue(
            "Should NOT hover target-below with horizontal axis and vertical drag",
            hoveredKey == null || hoveredKey != "target-below",
        )

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun horizontalAxis_dropsOnHorizontalTarget() = runComposeUiTest {
        var droppedRight = false
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<String>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                Row {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = "test",
                        dragAxis = DragAxis.Horizontal,
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .testTag("draggable"),
                    ) {
                        Box(Modifier.width(100.dp).height(100.dp))
                    }

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(100.dp)
                            .dropTarget(
                                key = "target-right",
                                state = state,
                                onDrop = { droppedRight = true },
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
                end = Offset(center.x + distancePx, center.y),
            )
            up()
        }

        waitForIdle()
        assertTrue("Should drop on horizontal target", droppedRight)
    }
}
