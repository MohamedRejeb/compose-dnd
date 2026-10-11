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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Once a drag has started, the pointer belongs to the drag: a scrollable or
 * clickable parent/child of the dragged item must not react to it.
 */
@OptIn(ExperimentalTestApi::class)
class LongPressDragInScrollableTest {

    @Test
    fun longPressDragInLazyColumn_doesNotScrollTheList() = runComposeUiTest {
        lateinit var state: DragAndDropState<Int>
        lateinit var listState: LazyListState
        var clicks = 0

        setContent {
            state = rememberDragAndDropState(dragAfterLongPress = true)
            listState = rememberLazyListState()
            DragAndDropContainer(state = state, modifier = Modifier.size(400.dp)) {
                LazyColumn(state = listState, modifier = Modifier.testTag("list")) {
                    items((0 until 30).toList(), key = { it }) { item ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable { clicks++ }
                                .draggableItem(
                                    key = item,
                                    data = item,
                                    state = state,
                                    draggableContent = { Box(Modifier.size(400.dp, 50.dp)) },
                                ),
                        )
                    }
                }
            }
        }
        waitForIdle()

        onNodeWithTag("list").performTouchInput {
            down(Offset(200f, 125f))
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            moveBy(Offset(0f, 1f))
        }
        waitForIdle()
        assertTrue(state.isDragging(2), "Long press should start the drag")

        onNodeWithTag("list").performTouchInput {
            repeat(10) {
                advanceEventTime(16)
                moveBy(Offset(0f, -15f))
            }
        }
        waitForIdle()

        assertEquals(0, listState.firstVisibleItemIndex, "The list must not scroll with the drag")
        assertEquals(0, listState.firstVisibleItemScrollOffset, "The list must not scroll with the drag")

        onNodeWithTag("list").performTouchInput { up() }
        waitForIdle()
        assertEquals(0, clicks, "Releasing a drag must not click the item")
    }

    /** A real finger never holds perfectly still: it drifts a little during the long press. */
    @Test
    fun fingerDriftingDuringTheLongPress_stillDragsInsteadOfScrolling() = runComposeUiTest {
        lateinit var state: DragAndDropState<Int>
        lateinit var listState: LazyListState

        setContent {
            state = rememberDragAndDropState(dragAfterLongPress = true)
            listState = rememberLazyListState()
            DragAndDropContainer(state = state, modifier = Modifier.size(400.dp)) {
                LazyColumn(state = listState, modifier = Modifier.testTag("list")) {
                    items((0 until 30).toList(), key = { it }) { item ->
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .draggableItem(
                                    key = item,
                                    data = item,
                                    state = state,
                                    draggableContent = { Box(Modifier.size(400.dp, 50.dp)) },
                                ),
                        )
                    }
                }
            }
        }
        waitForIdle()

        onNodeWithTag("list").performTouchInput {
            down(Offset(200f, 125f))
            // drift upwards, staying under the touch slop
            repeat(3) {
                advanceEventTime(50)
                moveBy(Offset(0f, -viewConfiguration.touchSlop * 0.2f))
            }
            // the long press fires, then the first move takes the whole gesture past the slop
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            moveBy(Offset(0f, -viewConfiguration.touchSlop * 0.6f))
        }
        waitForIdle()
        assertTrue(state.isDragging(2), "Long press should start the drag")

        onNodeWithTag("list").performTouchInput {
            repeat(10) {
                advanceEventTime(16)
                moveBy(Offset(0f, -15f))
            }
        }
        waitForIdle()

        assertTrue(state.isDragging(2), "The drag must survive the first moves after the long press")
        assertEquals(0, listState.firstVisibleItemIndex, "The list must not scroll with the drag")
        assertEquals(0, listState.firstVisibleItemScrollOffset, "The list must not scroll with the drag")

        onNodeWithTag("list").performTouchInput { up() }
    }

    @Test
    fun longPressReleasedInPlace_doesNotClickTheItem() = runComposeUiTest {
        lateinit var state: DragAndDropState<Int>
        var clicks = 0
        var dragEnds = 0

        setContent {
            state = rememberDragAndDropState(dragAfterLongPress = true)
            DragAndDropContainer(state = state, modifier = Modifier.size(400.dp).testTag("container")) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clickable { clicks++ }
                        .draggableItem(
                            key = 0,
                            data = 0,
                            state = state,
                            onDragEnd = { dragEnds++ },
                            draggableContent = { Box(Modifier.size(400.dp, 50.dp)) },
                        ),
                )
            }
        }
        waitForIdle()

        onNodeWithTag("container").performTouchInput {
            down(Offset(200f, 25f))
            advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            up()
        }
        waitForIdle()

        assertEquals(1, dragEnds, "The long press should have started a drag")
        assertEquals(0, clicks, "Releasing a drag must not click the item")
    }
}
