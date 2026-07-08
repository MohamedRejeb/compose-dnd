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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.dragHandle
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Dragging the same item a second time from its handle after a reorder must
 * start from the item's current position, not jump by the first drag's offset.
 */
@OptIn(ExperimentalTestApi::class)
class DragHandleRepeatedDragTest {

    @Test
    fun secondDragFromHandle_afterReorder_startsAtCurrentPosition() = runComposeUiTest {
        var items by mutableStateOf(listOf("A", "B", "C"))
        var reorderCount = 0
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(300.dp).height(600.dp),
            ) {
                LazyColumn {
                    items(items, key = { it }) { item ->
                        val isDragging = dndState.isDragging(item)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                .reorderableItem(
                                    key = item,
                                    data = item,
                                    state = dndState,
                                    hasDragHandle = true,
                                    onDragEnter = { state ->
                                        if (state.data == item) return@reorderableItem
                                        reorderCount++
                                        items = items.toMutableList().apply {
                                            val index = indexOf(item)
                                            if (index != -1) {
                                                remove(state.data)
                                                add(index, state.data)
                                            }
                                        }
                                    },
                                    draggableContent = {
                                        Box(Modifier.fillMaxWidth().height(100.dp))
                                    },
                                ).animateItem()
                                .testTag("item-$item"),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(100.dp)
                                    .testTag("handle-$item")
                                    .dragHandle(key = item, state = dndState),
                            )
                            Box(Modifier.weight(1f).height(100.dp))
                        }
                    }
                }
            }
        }

        waitForIdle()

        // First drag: move A down one slot from its handle
        onNodeWithTag("handle-A").performTouchInput {
            down(center)
            repeat(12) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
            up()
        }
        waitForIdle()

        assertEquals(listOf("B", "A", "C"), items, "First drag should swap A below B")
        val reorderCountAfterFirstDrag = reorderCount

        // Second drag from the same handle, small move that stays inside A's slot
        onNodeWithTag("handle-A").performTouchInput {
            down(center)
            repeat(4) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()

        // A's slot is now y = 100. The ghost must stay within that slot,
        // moved by at most the 40px of pointer travel minus touch slop. The
        // stale handle bug shifted it by another 100px to y = 220.
        val ghostY = dndState.dragPosition.value.y

        onNodeWithTag("handle-A").performTouchInput { up() }
        waitForIdle()

        assertTrue(
            ghostY in 95f..145f,
            "Second drag ghost should stay within A's slot (y=100..140) but was y=$ghostY",
        )
        assertEquals(
            reorderCountAfterFirstDrag,
            reorderCount,
            "Small second drag must not trigger a reorder (items=$items)",
        )
        assertEquals(listOf("B", "A", "C"), items, "Order should be unchanged after small second drag")
    }
}
