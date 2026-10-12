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
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalTestApi::class)
class DragStateTest {

    @Test
    fun isActiveDrag_trueWhileDragging_falseAfterRelease() = runComposeUiTest {
        var isActive = false
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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
        assertFalse("Should not be dragging initially", isActive)

        val distancePx = with(density) { 50.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }
        waitForIdle()
        assertTrue("Should be dragging while pointer is held", isActive)

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
        assertFalse("Should not be dragging after release", isActive)
    }

    @Test
    fun isDragging_trueForDraggedItem_falseForOthers() = runComposeUiTest {
        var isDraggingA = false
        var isDraggingB = false
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<String>()
            isDraggingA = state.isDragging("A")
            isDraggingB = state.isDragging("B")

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                Column {
                    DraggableItem(
                        state = state,
                        key = "A",
                        data = "item-a",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("item-A"),
                    ) {
                        Box(Modifier.fillMaxWidth().height(100.dp))
                    }

                    DraggableItem(
                        state = state,
                        key = "B",
                        data = "item-b",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("item-B"),
                    ) {
                        Box(Modifier.fillMaxWidth().height(100.dp))
                    }
                }
            }
        }

        waitForIdle()
        assertFalse("A should not be dragging initially", isDraggingA)
        assertFalse("B should not be dragging initially", isDraggingB)

        val distancePx = with(density) { 30.dp.toPx() }

        // Drag item A
        onNodeWithTag("item-A").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }
        waitForIdle()

        assertTrue("A should be dragging", isDraggingA)
        assertFalse("B should NOT be dragging", isDraggingB)

        onNodeWithTag("item-A").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun enabled_false_preventsDrag() = runComposeUiTest {
        var isActive = false
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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

        val distancePx = with(density) { 50.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }
        waitForIdle()

        assertFalse("Drag should not start when enabled=false", isActive)

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun draggedItemData_matchesDraggedItem() = runComposeUiTest {
        var draggedData: String? = null
        var draggedKey: Any? = null
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<String>()
            draggedData = state.draggedItem?.data
            draggedKey = state.draggedItem?.key

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                DraggableItem(
                    state = state,
                    key = "my-key",
                    data = "my-data",
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
        assertNull("No dragged data initially", draggedData)
        assertNull("No dragged key initially", draggedKey)

        val distancePx = with(density) { 50.dp.toPx() }

        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }
        waitForIdle()

        assertEquals("Dragged data should match", "my-data", draggedData)
        assertEquals("Dragged key should match", "my-key", draggedKey)

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun hoveredDropTargetKey_clearsAfterDragEnd() = runComposeUiTest {
        var hoveredKey: Any? = "initial"
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
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
        assertNull("No hovered key initially", hoveredKey)

        val distancePx = with(density) { 200.dp.toPx() }

        // Drag onto target
        onNodeWithTag("draggable").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
        }
        waitForIdle()
        assertEquals("Should hover target", "target", hoveredKey)

        // Release
        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
        assertNull("Hovered key should clear after drop", hoveredKey)
    }
}
