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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drag.dragHandle
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Drags that end in an unusual way: the container goes away, the target stops
 * accepting, the drag is canceled over a target or right as it is released.
 */
@OptIn(ExperimentalTestApi::class)
class DragEdgeCasesTest {

    private val events = mutableListOf<String>()
    private var canDrop by mutableStateOf(true)
    private var itemEnabled by mutableStateOf(true)

    // Item (y 0..100) above a target (y 200..400)
    @Composable
    private fun ItemAndTarget(state: DragAndDropState<String>) {
        DragAndDropContainer(state = state, modifier = Modifier.size(400.dp).testTag(ContainerTag)) {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .draggableItem(
                            key = "item",
                            data = "item",
                            state = state,
                            enabled = itemEnabled,
                            onDragEnd = { events += "end" },
                            draggableContent = { Box(Modifier.fillMaxWidth().height(100.dp)) },
                        ),
                )
                Box(Modifier.fillMaxWidth().height(100.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .dropTarget(
                            key = "target",
                            state = state,
                            canDrop = canDrop,
                            onDrop = { events += "drop" },
                            onDragEnter = { events += "enter" },
                            onDragExit = { events += "exit" },
                        ),
                )
            }
        }
    }

    private fun ComposeUiTest.dragItemOverTarget(startX: Float = 200f) {
        onNodeWithTag(ContainerTag).performTouchInput {
            down(Offset(startX, 50f))
            repeat(10) {
                advanceEventTime(16)
                moveBy(Offset(0f, 25f))
            }
        }
        waitForIdle()
    }

    @Test
    fun containerRemovedMidDrag_endsTheDragAndAllowsTheNextOne() = runComposeUiTest {
        var showContainer by mutableStateOf(true)
        lateinit var state: DragAndDropState<String>

        setContent {
            state = rememberDragAndDropState()
            if (showContainer) ItemAndTarget(state)
        }
        waitForIdle()

        dragItemOverTarget()
        assertEquals("item", state.draggedItem?.key)

        showContainer = false
        waitForIdle()
        assertNull(state.draggedItem, "The drag must not outlive its container")
        assertEquals(listOf("enter", "exit", "end"), events)

        showContainer = true
        waitForIdle()
        onNodeWithTag(ContainerTag).performTouchInput { up() }
        waitForIdle()
        events.clear()

        dragItemOverTarget()
        onNodeWithTag(ContainerTag).performTouchInput { up() }
        waitForIdle()
        assertEquals(listOf("enter", "drop", "end"), events, "The next drag must work")
    }

    @Test
    fun targetThatStopsAcceptingWhileHovered_doesNotGetTheDrop() = runComposeUiTest {
        lateinit var state: DragAndDropState<String>
        setContent {
            state = rememberDragAndDropState()
            ItemAndTarget(state)
        }
        waitForIdle()

        dragItemOverTarget()
        assertEquals("target", state.hoveredDropTargetKey)

        canDrop = false
        waitForIdle()
        onNodeWithTag(ContainerTag).performTouchInput { up() }
        waitForIdle()

        assertTrue("drop" !in events, "A target with canDrop = false must not receive the drop, got $events")
        assertEquals("end", events.last())
    }

    @Test
    fun cancelOverATarget_exitsTheTarget() = runComposeUiTest {
        lateinit var state: DragAndDropState<String>
        setContent {
            state = rememberDragAndDropState()
            ItemAndTarget(state)
        }
        waitForIdle()

        dragItemOverTarget()
        runOnIdle { state.cancelDrag() }
        waitForIdle()

        assertEquals(listOf("enter", "exit", "end"), events)
    }

    @Test
    fun cancelRightAfterRelease_dropsOnceAndEndsOnce() = runComposeUiTest {
        lateinit var state: DragAndDropState<String>
        setContent {
            state = rememberDragAndDropState()
            ItemAndTarget(state)
        }
        waitForIdle()

        dragItemOverTarget()
        // Release and cancel before the drop gets to run
        runOnUiThread {
            onNodeWithTag(ContainerTag).performTouchInput { up() }
            state.cancelDrag()
        }
        waitForIdle()

        assertEquals(listOf("enter", "drop", "end"), events)
        assertNull(state.draggedItem)
    }

    @Test
    fun disabledItem_isNotDraggedByItsHandle() = runComposeUiTest {
        itemEnabled = false
        lateinit var state: DragAndDropState<String>
        setContent {
            state = rememberDragAndDropState()
            DragAndDropContainer(state = state, modifier = Modifier.size(400.dp).testTag(ContainerTag)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .draggableItem(
                            key = "item",
                            data = "item",
                            state = state,
                            enabled = itemEnabled,
                            hasDragHandle = true,
                            draggableContent = { Box(Modifier.fillMaxWidth().height(100.dp)) },
                        ),
                ) {
                    Box(Modifier.size(100.dp).dragHandle(key = "item", state = state))
                }
            }
        }
        waitForIdle()

        dragItemOverTarget(startX = HandleX)
        assertNull(state.draggedItem, "enabled = false must also stop the item's drag handle")

        onNodeWithTag(ContainerTag).performTouchInput { up() }
        itemEnabled = true
        waitForIdle()
        dragItemOverTarget(startX = HandleX)
        assertEquals("item", state.draggedItem?.key, "Enabling the item brings the handle back")
    }

    @Test
    fun handleRemovedFromTheContent_letsTheWholeItemDragAgain() = runComposeUiTest {
        var showHandle by mutableStateOf(true)
        lateinit var state: DragAndDropState<String>
        setContent {
            state = rememberDragAndDropState()
            DragAndDropContainer(state = state, modifier = Modifier.size(400.dp).testTag(ContainerTag)) {
                DraggableItem(
                    key = "item",
                    data = "item",
                    state = state,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                ) {
                    Box(Modifier.fillMaxWidth().height(100.dp)) {
                        if (showHandle) Box(Modifier.size(50.dp).dragHandle())
                    }
                }
            }
        }
        waitForIdle()

        // Outside the 50dp handle: the body doesn't drag while there is a handle
        dragItemOverTarget()
        assertNull(state.draggedItem)
        onNodeWithTag(ContainerTag).performTouchInput { up() }

        showHandle = false
        waitForIdle()
        dragItemOverTarget()
        assertEquals("item", state.draggedItem?.key, "Without a handle the whole item drags")
    }

    private companion object {
        const val ContainerTag = "container"
        const val HandleX = 50f
    }
}
