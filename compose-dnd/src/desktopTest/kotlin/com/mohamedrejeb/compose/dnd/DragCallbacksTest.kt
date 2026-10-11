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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Item lifecycle callbacks (onDragStart / onDragEnd), programmatic cancel and
 * the hover helpers on [DragAndDropState].
 */
@OptIn(ExperimentalTestApi::class)
class DragCallbacksTest {

    private fun TouchInjectionScope.dragBy(start: Offset, delta: Offset, release: Boolean = true) {
        down(start)
        val steps = 10
        repeat(steps) {
            advanceEventTime(16)
            moveBy(delta / steps.toFloat())
        }
        if (release) up()
    }

    @androidx.compose.runtime.Composable
    private fun ItemAndTarget(
        state: DragAndDropState<String>,
        events: MutableList<String>,
        useComposable: Boolean = false,
    ) {
        DragAndDropContainer(state = state, modifier = Modifier.size(400.dp).testTag("container")) {
            Column {
                if (useComposable) {
                    DraggableItem(
                        key = "item",
                        data = "item",
                        state = state,
                        onDragStart = { events += "start:${it.key}" },
                        onDragEnd = { events += "end:${it.key}" },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                    ) {
                        Box(Modifier.fillMaxSize())
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .draggableItem(
                                key = "item",
                                data = "item",
                                state = state,
                                onDragStart = { events += "start:${it.key}" },
                                onDragEnd = { events += "end:${it.key}" },
                                draggableContent = { Box(Modifier.size(400.dp, 100.dp)) },
                            ),
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .dropTarget(
                            key = "target",
                            state = state,
                            onDragEnter = { events += "enter" },
                            onDrop = { events += "drop:${it.data}" },
                        ),
                )
            }
        }
    }

    @Test
    fun dropOnTarget_firesStartThenDropThenEnd() = runComposeUiTest {
        val events = mutableListOf<String>()
        setContent { ItemAndTarget(rememberDragAndDropState(), events) }

        onNodeWithTag("container").performTouchInput { dragBy(Offset(200f, 50f), Offset(0f, 200f)) }
        waitForIdle()

        assertEquals(listOf("start:item", "enter", "drop:item", "end:item"), events)
    }

    @Test
    fun releaseOutsideTargets_firesEndWithoutDrop() = runComposeUiTest {
        val events = mutableListOf<String>()
        setContent { ItemAndTarget(rememberDragAndDropState(), events, useComposable = true) }

        // Up and out of the container: no target is hovered on release
        onNodeWithTag("container").performTouchInput { dragBy(Offset(200f, 50f), Offset(0f, -60f)) }
        waitForIdle()

        assertEquals(listOf("start:item", "end:item"), events)
    }

    @Test
    fun cancelDrag_endsDragWithoutDropping() = runComposeUiTest {
        val events = mutableListOf<String>()
        lateinit var state: DragAndDropState<String>
        setContent {
            state = rememberDragAndDropState()
            ItemAndTarget(state, events)
        }

        onNodeWithTag("container").performTouchInput {
            dragBy(Offset(200f, 50f), Offset(0f, 200f), release = false)
        }
        waitForIdle()
        assertTrue(state.isDragging("item"))
        assertTrue(state.isDropTargetHovered("target"))

        runOnUiThread { state.cancelDrag() }
        waitForIdle()

        assertNull(state.draggedItem)
        assertFalse(state.isDropTargetHovered("target"))

        // Releasing the finger afterwards must not drop anything
        onNodeWithTag("container").performTouchInput { up() }
        waitForIdle()
        assertEquals(listOf("start:item", "enter", "end:item"), events)
    }

    @Test
    fun reorderableItem_canDropFalse_isNeverSwapped() = runComposeUiTest {
        var items by mutableStateOf(listOf("pinned", "a", "b"))
        lateinit var state: DragAndDropState<String>

        setContent {
            state = rememberDragAndDropState()
            DragAndDropContainer(state = state, modifier = Modifier.size(400.dp).testTag("container")) {
                Column {
                    items.forEach { item ->
                        key(item) {
                            val pinned = item == "pinned"
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .reorderableItem(
                                        key = item,
                                        data = item,
                                        state = state,
                                        enabled = !pinned,
                                        canDrop = !pinned,
                                        onDragEnter = { dragged ->
                                            items = items.toMutableList().apply {
                                                val index = indexOf(item)
                                                remove(dragged.data)
                                                add(index, dragged.data)
                                            }
                                        },
                                        draggableContent = { Box(Modifier.size(400.dp, 100.dp)) },
                                    ),
                            )
                        }
                    }
                }
            }
        }

        // Drag "b" (slot 3) all the way up over the pinned item (slot 1)
        onNodeWithTag("container").performTouchInput { dragBy(Offset(200f, 250f), Offset(0f, -250f)) }
        waitForIdle()

        assertEquals("pinned", items.first(), "A canDrop = false item must keep its slot")
        assertEquals(listOf("pinned", "b", "a"), items)
    }

    @Test
    fun reorderableItemComposable_scopedDragHandle_onlyHandleStartsDrag() = runComposeUiTest {
        lateinit var state: DragAndDropState<String>

        setContent {
            state = rememberDragAndDropState()
            DragAndDropContainer(state = state, modifier = Modifier.size(400.dp)) {
                ReorderableItem(
                    key = "item",
                    data = "item",
                    state = state,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                ) {
                    Column {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("handle")
                                .dragHandle(),
                        )
                        Box(Modifier.fillMaxWidth().height(50.dp).testTag("body"))
                    }
                }
            }
        }
        waitForIdle()

        onNodeWithTag("body").performTouchInput { dragBy(center, Offset(0f, 100f), release = false) }
        waitForIdle()
        assertNull(state.draggedItem, "Dragging the body must not start a drag")
        onNodeWithTag("body").performTouchInput { up() }
        waitForIdle()

        onNodeWithTag("handle").performTouchInput { dragBy(center, Offset(0f, 100f), release = false) }
        waitForIdle()
        assertTrue(state.isDragging("item"), "Dragging the handle must start a drag")
        // The drag shadow renders a second copy of the handle while dragging
        onAllNodesWithTag("handle").onFirst().performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun cancelDrag_calledTwice_endsTheDragOnce() = runComposeUiTest {
        val events = mutableListOf<String>()
        lateinit var state: DragAndDropState<String>
        setContent {
            state = rememberDragAndDropState()
            ItemAndTarget(state, events)
        }

        onNodeWithTag("container").performTouchInput {
            dragBy(Offset(200f, 50f), Offset(0f, 200f), release = false)
        }
        waitForIdle()

        runOnUiThread {
            state.cancelDrag()
            state.cancelDrag()
        }
        waitForIdle()
        onNodeWithTag("container").performTouchInput { up() }
        waitForIdle()

        assertNull(state.draggedItem)
        assertEquals(listOf("start:item", "enter", "end:item"), events)
    }
}
