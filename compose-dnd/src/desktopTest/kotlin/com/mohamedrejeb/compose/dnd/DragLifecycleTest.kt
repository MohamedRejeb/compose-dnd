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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Drag lifecycle edge cases: overlapping drags, container state changes and
 * registry bookkeeping when an item's key changes.
 */
@OptIn(ExperimentalTestApi::class)
class DragLifecycleTest {

    // Items A (y 0..100) and B (y 100..200), target at y 200..400
    @androidx.compose.runtime.Composable
    private fun TwoItemsAndTarget(
        state: DragAndDropState<String>,
        dropped: MutableList<String>,
        enabled: Boolean = true,
    ) {
        DragAndDropContainer(
            state = state,
            enabled = enabled,
            modifier = Modifier.size(400.dp).testTag("container"),
        ) {
            Column {
                listOf("A", "B").forEach { item ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .draggableItem(
                                key = item,
                                data = item,
                                state = state,
                                draggableContent = {
                                    Box(Modifier.fillMaxWidth().height(100.dp))
                                },
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
                            onDrop = { dropped += it.data },
                        ),
                )
            }
        }
    }

    @Test
    fun dragStartedDuringDropAnimation_finishesPreviousDropAndKeepsNewDrag() = runComposeUiTest {
        val dropped = mutableListOf<String>()
        lateinit var state: DragAndDropState<String>

        setContent {
            state = rememberDragAndDropState()
            TwoItemsAndTarget(state, dropped)
        }
        waitForIdle()

        mainClock.autoAdvance = false

        // Drag A onto the target and release: the drop animation starts
        onNodeWithTag("container").performTouchInput { down(Offset(200f, 50f)) }
        repeat(10) {
            onNodeWithTag("container").performTouchInput {
                advanceEventTime(16)
                moveBy(Offset(0f, 25f))
            }
            mainClock.advanceTimeBy(16)
        }
        assertEquals("target", state.hoveredDropTargetKey)
        onNodeWithTag("container").performTouchInput { up() }
        mainClock.advanceTimeBy(32)
        assertTrue(dropped.isEmpty(), "Drop animation should still be running")

        // Grab B while A's drop animation is still in flight
        onNodeWithTag("container").performTouchInput { down(Offset(200f, 150f)) }
        repeat(4) {
            onNodeWithTag("container").performTouchInput {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
            mainClock.advanceTimeBy(16)
        }

        assertEquals(listOf("A"), dropped, "Previous drop should be committed right away")
        assertEquals("B", state.draggedItem?.key, "New drag should be active")

        // Long after A's animation would have ended, B's drag must survive
        mainClock.advanceTimeBy(2_000)
        assertEquals("B", state.draggedItem?.key, "New drag must not be cleared by the old drop")

        onNodeWithTag("container").performTouchInput {
            advanceEventTime(16)
            moveBy(Offset(0f, 150f))
            advanceEventTime(16)
            up()
        }
        mainClock.autoAdvance = true
        waitForIdle()

        assertEquals(listOf("A", "B"), dropped)
        assertNull(state.draggedItem)
    }

    @Test
    fun disablingContainerMidDrag_cancelsTheDrag() = runComposeUiTest {
        val dropped = mutableListOf<String>()
        var enabled by mutableStateOf(true)
        lateinit var state: DragAndDropState<String>

        setContent {
            state = rememberDragAndDropState()
            TwoItemsAndTarget(state, dropped, enabled = enabled)
        }
        waitForIdle()

        onNodeWithTag("container").performTouchInput {
            down(Offset(200f, 50f))
            repeat(10) {
                advanceEventTime(16)
                moveBy(Offset(0f, 25f))
            }
        }
        waitForIdle()
        assertEquals("A", state.draggedItem?.key)

        enabled = false
        waitForIdle()

        assertNull(state.draggedItem, "Disabling the container must end the drag")
        assertTrue(dropped.isEmpty(), "A canceled drag must not drop")

        onNodeWithTag("container").performTouchInput { up() }
        waitForIdle()
        assertTrue(dropped.isEmpty())
    }

    @Test
    fun changingKey_movesRegistrationsToTheNewKey() = runComposeUiTest {
        var key by mutableStateOf("a")
        lateinit var state: DragAndDropState<String>

        setContent {
            state = rememberDragAndDropState()
            DragAndDropContainer(state = state, modifier = Modifier.size(400.dp)) {
                Box(
                    Modifier
                        .size(100.dp)
                        .draggableItem(
                            key = key,
                            data = key,
                            state = state,
                            draggableContent = { Box(Modifier.size(100.dp)) },
                        ).dropTarget(key = key, state = state),
                )
            }
        }
        waitForIdle()
        assertEquals(setOf<Any>("a"), state.draggableItemMap.keys)
        assertEquals(setOf<Any>("a"), state.dropTargetMap.keys)

        key = "b"
        waitForIdle()

        assertEquals(setOf<Any>("b"), state.draggableItemMap.keys)
        assertEquals(setOf<Any>("b"), state.dropTargetMap.keys)
        assertEquals("b", state.draggableItemMap["b"]?.key)
    }
}
