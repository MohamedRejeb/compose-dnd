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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragScrollPin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalDndApi::class)
class DragScrollPinTest {

    private data class SizedItem(
        val id: String,
        val heightDp: Int,
    )

    /**
     * Simulate a long-press drag by holding the pointer, then moving slowly.
     * Does NOT release — pointer stays held.
     */
    private fun TouchInjectionScope.simulateLongPressDrag(
        start: Offset,
        end: Offset,
        longPressMs: Long = 600,
        steps: Int = 20,
        stepDelayMs: Long = 16,
    ) {
        down(start)
        advanceEventTime(longPressMs)
        val dx = (end.x - start.x) / steps
        val dy = (end.y - start.y) / steps
        for (i in 1..steps) {
            advanceEventTime(stepDelayMs)
            moveTo(Offset(start.x + dx * i, start.y + dy * i))
        }
    }

    /**
     * Helper: sets up a LazyColumn with reorderable different-sized items.
     * Viewport is intentionally small (300dp) so items overflow — this is
     * required to trigger Compose's key-based scroll anchoring.
     */
    private fun runReorderTest(
        useDragScrollPin: Boolean,
        onResult: (reorderCount: Int, items: List<SizedItem>, scrollIndex: Int, scrollOffset: Int) -> Unit,
    ) = runComposeUiTest {
        // Items total: 100+200+150+100+120 = 670dp — overflows 300dp viewport
        val initialItems = listOf(
            SizedItem("A", 100),
            SizedItem("B", 200),
            SizedItem("C", 150),
            SizedItem("D", 100),
            SizedItem("E", 120),
        )
        var items by mutableStateOf(initialItems)
        var reorderCount = 0
        var scrollIndex = 0
        var scrollOffset = 0

        setContent {
            val dndState = rememberDragAndDropState<SizedItem>()
            val listState = rememberLazyListState()

            scrollIndex = listState.firstVisibleItemIndex
            scrollOffset = listState.firstVisibleItemScrollOffset

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(300.dp).height(300.dp),
            ) {
                val baseModifier = Modifier.width(300.dp).height(300.dp)
                val listModifier = if (useDragScrollPin) {
                    baseModifier.dragScrollPin(
                        state = dndState,
                        lazyListState = listState,
                    )
                } else {
                    baseModifier
                }

                LazyColumn(
                    state = listState,
                    modifier = listModifier.testTag("lazyColumn"),
                ) {
                    items(items, key = { it.id }) { item ->
                        val isDragging = dndState.isDragging(item.id)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(item.heightDp.dp)
                                .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                .reorderableItem(
                                    key = item.id,
                                    data = item,
                                    state = dndState,
                                    dropStrategy = DropStrategy.CenterDistance,
                                    dragAfterLongPress = true,
                                    onDragEnter = { state ->
                                        val draggedItem = state.data
                                        if (draggedItem.id == item.id) return@reorderableItem
                                        val targetIndex = items.indexOfFirst { it.id == item.id }
                                        if (targetIndex == -1) return@reorderableItem
                                        reorderCount++
                                        items = items
                                            .filter { it.id != draggedItem.id }
                                            .toMutableList()
                                            .apply { add(targetIndex.coerceAtMost(size), draggedItem) }
                                    },
                                    draggableContent = {
                                        Box(Modifier.fillMaxWidth().height(item.heightDp.dp))
                                    },
                                )
                                .testTag("item-${item.id}"),
                        )
                    }
                }
            }
        }

        waitForIdle()

        // Drag item A (100dp) down onto item B (200dp) and HOLD
        onNodeWithTag("item-A").performTouchInput {
            simulateLongPressDrag(
                start = center,
                end = Offset(center.x, center.y + 200f),
            )
        }

        // Let several frames settle — anchoring may re-apply across frames
        waitForIdle()
        mainClock.advanceTimeBy(500)
        waitForIdle()

        println("[TEST] useDragScrollPin=$useDragScrollPin reorderCount=$reorderCount items=${items.map { it.id }} scroll=($scrollIndex, $scrollOffset)")
        onResult(reorderCount, items, scrollIndex, scrollOffset)

        // Release
        onNodeWithTag("item-A").performTouchInput { up() }
        waitForIdle()
    }

    /**
     * Reproducer: WITHOUT dragScrollPin, dragging a small item (100dp) onto a
     * larger item (200dp) causes a scroll jump due to key-based anchoring.
     */
    @Test
    fun reorder_differentSizedItems_WITHOUT_pin_scrollJumps() {
        runReorderTest(useDragScrollPin = false) { reorderCount, _, scrollIndex, scrollOffset ->
            assertTrue(reorderCount > 0, "Reorder should have triggered (was: $reorderCount)")
            val scrollJumped = scrollIndex != 0 || scrollOffset != 0
            assertTrue(scrollJumped, "Scroll should jump without pin (confirms test reproduces the issue)")
        }
    }

    /**
     * WITH dragScrollPin, the scroll should remain pinned at (0, 0).
     */
    @Test
    fun reorder_differentSizedItems_WITH_pin_scrollStaysPinned() {
        runReorderTest(useDragScrollPin = true) { reorderCount, _, scrollIndex, scrollOffset ->
            assertTrue(reorderCount > 0, "Reorder should have triggered (was: $reorderCount)")
            assertEquals(0, scrollIndex, "Scroll index should remain 0 after reorder")
            assertEquals(0, scrollOffset, "Scroll offset should remain 0 after reorder")
        }
    }

    // -- Multi-column tests (Kanban scenario) --

    private data class KanbanColumn(
        val id: String,
        val items: List<SizedItem>,
    )

    /**
     * Mimics the Kanban layout: multiple LazyColumns side-by-side sharing one
     * DragAndDropState. Drag happens in the first column; verifies that ALL
     * columns' scroll positions remain stable.
     */
    private fun runMultiColumnReorderTest(
        useDragScrollPin: Boolean,
        onResult: (reorderCount: Int, col1Scroll: Pair<Int, Int>, col2Scroll: Pair<Int, Int>) -> Unit,
    ) = runComposeUiTest {
        val initialColumns = listOf(
            KanbanColumn("col1", listOf(
                SizedItem("A", 100),
                SizedItem("B", 200),
                SizedItem("C", 150),
                SizedItem("D", 100),
                SizedItem("E", 120),
            )),
            KanbanColumn("col2", listOf(
                SizedItem("F", 120),
                SizedItem("G", 180),
                SizedItem("H", 100),
                SizedItem("I", 150),
                SizedItem("J", 130),
            )),
        )
        var columns by mutableStateOf(initialColumns)
        var reorderCount = 0
        var col1ScrollIndex = 0
        var col1ScrollOffset = 0
        var col2ScrollIndex = 0
        var col2ScrollOffset = 0

        setContent {
            val dndState = rememberDragAndDropState<SizedItem>()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(600.dp).height(300.dp),
            ) {
                Row {
                    columns.forEach { column ->
                        val colState = rememberLazyListState()

                        // Track each column's scroll
                        when (column.id) {
                            "col1" -> {
                                col1ScrollIndex = colState.firstVisibleItemIndex
                                col1ScrollOffset = colState.firstVisibleItemScrollOffset
                            }
                            "col2" -> {
                                col2ScrollIndex = colState.firstVisibleItemIndex
                                col2ScrollOffset = colState.firstVisibleItemScrollOffset
                            }
                        }

                        val baseModifier = Modifier.width(280.dp).fillMaxHeight()
                        val listModifier = if (useDragScrollPin) {
                            baseModifier.dragScrollPin(
                                state = dndState,
                                lazyListState = colState,
                            )
                        } else {
                            baseModifier
                        }

                        LazyColumn(
                            state = colState,
                            modifier = listModifier.testTag("column-${column.id}"),
                        ) {
                            items(column.items, key = { it.id }) { item ->
                                val isDragging = dndState.isDragging(item.id)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(item.heightDp.dp)
                                        .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                        .reorderableItem(
                                            key = item.id,
                                            data = item,
                                            state = dndState,
                                            dropStrategy = DropStrategy.CenterDistance,
                                            dragAfterLongPress = true,
                                            onDragEnter = { state ->
                                                val draggedItem = state.data
                                                if (draggedItem.id == item.id) return@reorderableItem

                                                reorderCount++
                                                columns = columns.map { col ->
                                                    val targetIndex = col.items.indexOfFirst { it.id == item.id }
                                                    if (targetIndex == -1) {
                                                        col.copy(items = col.items.filter { it.id != draggedItem.id })
                                                    } else {
                                                        col.copy(
                                                            items = col.items
                                                                .filter { it.id != draggedItem.id }
                                                                .toMutableList()
                                                                .apply { add(targetIndex.coerceAtMost(size), draggedItem) }
                                                        )
                                                    }
                                                }
                                            },
                                            draggableContent = {
                                                Box(Modifier.fillMaxWidth().height(item.heightDp.dp))
                                            },
                                        )
                                        .testTag("item-${item.id}"),
                                )
                            }
                        }
                    }
                }
            }
        }

        waitForIdle()

        // Drag item A down onto item B within column 1
        onNodeWithTag("item-A").performTouchInput {
            simulateLongPressDrag(
                start = center,
                end = Offset(center.x, center.y + 200f),
            )
        }

        waitForIdle()
        mainClock.advanceTimeBy(500)
        waitForIdle()

        println("[MULTI-COL] pin=$useDragScrollPin reorders=$reorderCount col1=($col1ScrollIndex,$col1ScrollOffset) col2=($col2ScrollIndex,$col2ScrollOffset)")
        println("[MULTI-COL] col1 items: ${columns[0].items.map { it.id }}")
        onResult(reorderCount, Pair(col1ScrollIndex, col1ScrollOffset), Pair(col2ScrollIndex, col2ScrollOffset))

        onNodeWithTag("item-A").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun multiColumn_WITHOUT_pin_scrollJumps() {
        runMultiColumnReorderTest(useDragScrollPin = false) { reorderCount, col1Scroll, col2Scroll ->
            assertTrue(reorderCount > 0, "Reorder should have triggered")
            val col1Jumped = col1Scroll.first != 0 || col1Scroll.second != 0
            val col2Jumped = col2Scroll.first != 0 || col2Scroll.second != 0
            println("[MULTI-COL] WITHOUT pin: col1Jumped=$col1Jumped col2Jumped=$col2Jumped")
            assertTrue(col1Jumped || col2Jumped, "At least one column should have scrolled without pin")
        }
    }

    @Test
    fun multiColumn_WITH_pin_scrollStaysPinned() {
        runMultiColumnReorderTest(useDragScrollPin = true) { reorderCount, col1Scroll, col2Scroll ->
            assertTrue(reorderCount > 0, "Reorder should have triggered")
            assertEquals(0, col1Scroll.first, "Col1 scroll index should remain 0")
            assertEquals(0, col1Scroll.second, "Col1 scroll offset should remain 0")
            assertEquals(0, col2Scroll.first, "Col2 scroll index should remain 0")
            assertEquals(0, col2Scroll.second, "Col2 scroll offset should remain 0")
        }
    }
}
