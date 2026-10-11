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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Reordering across two lists that share one state (a Kanban board). Inserting a card into
 * another list pushes the entered card further along that list, so the drag reaches it again
 * by moving along the list, not by reversing.
 */
@OptIn(ExperimentalTestApi::class)
class CrossListReorderTest {

    private var columns by mutableStateOf(listOf(listOf("X"), listOf("B", "C")))

    /** Puts [dragged] in the slot [target] occupies, in whichever list holds [target]. */
    private fun moveTo(dragged: String, target: String) {
        if (dragged == target) return
        columns = columns.map { column ->
            val slot = column.indexOf(target)
            val without = column - dragged
            if (slot == -1) without else without.toMutableList().apply { add(slot, dragged) }
        }
    }

    private fun ComposeUiTest.setBoard() {
        setContent {
            val state = rememberDragAndDropState<String>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.width(BoardWidth.dp).height(BoardHeight.dp).testTag(BoardTag),
            ) {
                Row {
                    columns.forEach { column ->
                        Column(Modifier.width(ColumnWidth.dp).height(BoardHeight.dp)) {
                            column.forEach { id ->
                                key(id) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(ItemHeight.dp)
                                            .reorderableItem(
                                                key = id,
                                                data = id,
                                                state = state,
                                                dropStrategy = DropStrategy.CenterDistance,
                                                onDragEnter = { dragged -> moveTo(dragged.data, id) },
                                                draggableContent = {
                                                    Box(Modifier.width(ColumnWidth.dp).height(ItemHeight.dp))
                                                },
                                            ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.glide(delta: Offset, steps: Int = 25) {
        onNodeWithTag(BoardTag).performTouchInput {
            repeat(steps) {
                advanceEventTime(16)
                moveBy(delta / steps.toFloat())
            }
        }
        waitForIdle()
    }

    @Test
    fun cardEnteringAnotherList_thenMovingDown_reordersAtEverySlot() = runComposeUiTest {
        setBoard()

        onNodeWithTag(BoardTag).performTouchInput { down(Offset(ColumnWidth / 2f, ItemHeight / 2f)) }
        glide(Offset(ColumnWidth.toFloat(), 0f))
        assertEquals(listOf(emptyList(), listOf("X", "B", "C")), columns, "entering the second list at the top")

        glide(Offset(0f, ItemHeight.toFloat()))
        assertEquals(listOf(emptyList(), listOf("B", "X", "C")), columns, "moving down to the second slot")

        glide(Offset(0f, ItemHeight.toFloat()))
        assertEquals(listOf(emptyList(), listOf("B", "C", "X")), columns, "moving down to the third slot")

        onNodeWithTag(BoardTag).performTouchInput { up() }
    }

    private companion object {
        const val BoardTag = "board"
        const val ColumnWidth = 150
        const val ItemHeight = 100
        const val BoardWidth = ColumnWidth * 2
        const val BoardHeight = ItemHeight * 4
    }
}
