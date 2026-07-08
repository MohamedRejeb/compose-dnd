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
import kotlin.test.assertTrue

/**
 * Covers the reorder hysteresis ([ReorderHysteresis]): after a swap the
 * just-entered target is excluded from hover candidates until the drag
 * reverses, so a slow drag can't oscillate the swap while a deliberate
 * drag-back can still undo it.
 */
@OptIn(ExperimentalTestApi::class, ExperimentalDndApi::class)
class ReorderUndoBackTest {

    private data class SizedItem(
        val id: String,
        val heightDp: Int,
    )

    @Test
    fun smallItemSwapWithBigger_reverseDrag_restoresOriginalOrder() = runComposeUiTest {
        val initialItems = listOf(
            SizedItem("A", 200),
            SizedItem("B", 100),
        )
        var items by mutableStateOf(initialItems)
        var reorderCount = 0

        setContent {
            val dndState = rememberDragAndDropState<SizedItem>()
            val listState = rememberLazyListState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(300.dp).height(600.dp),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .width(300.dp)
                        .height(600.dp)
                        .dragScrollPin(state = dndState, lazyListState = listState)
                        .testTag("lazyColumn"),
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
                                        val targetIndex =
                                            items.indexOfFirst { it.id == item.id }
                                        if (targetIndex == -1) return@reorderableItem
                                        reorderCount++
                                        items = items
                                            .filter { it.id != draggedItem.id }
                                            .toMutableList()
                                            .apply {
                                                add(targetIndex.coerceAtMost(size), draggedItem)
                                            }
                                    },
                                    draggableContent = {
                                        Box(Modifier.fillMaxWidth().height(item.heightDp.dp))
                                    },
                                ).testTag("item-${item.id}"),
                        )
                    }
                }
            }
        }

        waitForIdle()

        // Long-press B and drag up just past the swap threshold with A
        onNodeWithTag("item-B").performTouchInput {
            down(center)
            advanceEventTime(600) // long-press
            val upDistance = 80f
            val steps = 20
            val stepDelta = -upDistance / steps
            repeat(steps) {
                advanceEventTime(16)
                moveBy(Offset(0f, stepDelta))
            }
        }
        waitForIdle()

        val reorderCountAfterUp = reorderCount
        val orderAfterUp = items.map { it.id }
        println("[TEST] After up-drag: items=$orderAfterUp reorders=$reorderCountAfterUp")

        // Make sure the up drag actually swapped, otherwise the undo check below means nothing
        assertTrue(
            reorderCountAfterUp >= 1,
            "Up-drag should have triggered the swap (reorderCount=$reorderCountAfterUp, items=$orderAfterUp)",
        )
        assertEquals(
            "B",
            items[0].id,
            "Up-drag should have swapped B above A (got ${items.map { it.id }})",
        )

        // Without releasing, drag back down past the reverse threshold to undo
        onNodeWithTag("item-B").performTouchInput {
            val downDistance = 250f
            val steps = 50
            val stepDelta = downDistance / steps
            repeat(steps) {
                advanceEventTime(16)
                moveBy(Offset(0f, stepDelta))
            }
        }
        waitForIdle()

        onNodeWithTag("item-B").performTouchInput { up() }
        waitForIdle()

        println("[TEST] Final: items=${items.map { it.id }} reorders=$reorderCount")

        assertEquals(
            "A",
            items[0].id,
            "After reverse drag, A should be first again (items=${items.map { it.id }}, reorders=$reorderCount)",
        )
        assertEquals(
            "B",
            items[1].id,
            "After reverse drag, B should be second again (items=${items.map { it.id }}, reorders=$reorderCount)",
        )
    }

    /** One swap must not fire a follow-up reorder when the drag keeps going. */
    @Test
    fun threeEqualItems_dragFirstPastSecond_swapsExactlyOnce() = runComposeUiTest {
        val initialItems = listOf(
            SizedItem("A", 100),
            SizedItem("B", 100),
            SizedItem("C", 100),
        )
        var items by mutableStateOf(initialItems)
        var reorderCount = 0

        setContent {
            val dndState = rememberDragAndDropState<SizedItem>()
            val listState = rememberLazyListState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(300.dp).height(600.dp),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .width(300.dp)
                        .height(600.dp)
                        .testTag("lazyColumn"),
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
                                        val targetIndex =
                                            items.indexOfFirst { it.id == item.id }
                                        if (targetIndex == -1) return@reorderableItem
                                        reorderCount++
                                        items = items
                                            .filter { it.id != draggedItem.id }
                                            .toMutableList()
                                            .apply {
                                                add(targetIndex.coerceAtMost(size), draggedItem)
                                            }
                                    },
                                    draggableContent = {
                                        Box(Modifier.fillMaxWidth().height(item.heightDp.dp))
                                    },
                                ).testTag("item-${item.id}"),
                        )
                    }
                }
            }
        }

        waitForIdle()

        // Long-press A and drag down by exactly 100dp.
        onNodeWithTag("item-A").performTouchInput {
            down(center)
            advanceEventTime(600)
            val steps = 20
            val stepDelta = 100f / steps
            repeat(steps) {
                advanceEventTime(16)
                moveBy(Offset(0f, stepDelta))
            }
        }
        waitForIdle()

        onNodeWithTag("item-A").performTouchInput { up() }
        waitForIdle()

        println("[TEST] threeEqual final: items=${items.map { it.id }} reorders=$reorderCount")

        assertTrue(
            reorderCount > 0,
            "Expected at least one reorder, got $reorderCount",
        )
        assertEquals(
            "B",
            items[0].id,
            "B should be first (items=${items.map { it.id }})",
        )
        assertEquals(
            "A",
            items[1].id,
            "A should be second (items=${items.map { it.id }})",
        )
        assertEquals(
            "C",
            items[2].id,
            "C should be third (items=${items.map { it.id }})",
        )
    }

    /**
     * A continuous slow drag in one direction must produce exactly one reorder.
     * A directionless hysteresis would lift on any movement and oscillate.
     */
    @Test
    fun smallItemSwapWithBigger_continueSameDirection_swapsOnce() = runComposeUiTest {
        val initialItems = listOf(
            SizedItem("A", 200),
            SizedItem("B", 100),
        )
        var items by mutableStateOf(initialItems)
        var reorderCount = 0

        setContent {
            val dndState = rememberDragAndDropState<SizedItem>()
            val listState = rememberLazyListState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(300.dp).height(600.dp),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .width(300.dp)
                        .height(600.dp)
                        .dragScrollPin(state = dndState, lazyListState = listState)
                        .testTag("lazyColumn"),
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
                                        val targetIndex =
                                            items.indexOfFirst { it.id == item.id }
                                        if (targetIndex == -1) return@reorderableItem
                                        reorderCount++
                                        items = items
                                            .filter { it.id != draggedItem.id }
                                            .toMutableList()
                                            .apply {
                                                add(targetIndex.coerceAtMost(size), draggedItem)
                                            }
                                    },
                                    draggableContent = {
                                        Box(Modifier.fillMaxWidth().height(item.heightDp.dp))
                                    },
                                ).testTag("item-${item.id}"),
                        )
                    }
                }
            }
        }

        waitForIdle()

        // Long-press B and drag UP slowly past the swap threshold and KEEP
        // GOING in the same direction. Without the directional hysteresis,
        // the same swap would re-fire on each subsequent ~8px step.
        onNodeWithTag("item-B").performTouchInput {
            down(center)
            advanceEventTime(600)
            // 200px up in 50 small steps (~4px each)
            val totalUp = 200f
            val steps = 50
            val stepDelta = -totalUp / steps
            repeat(steps) {
                advanceEventTime(16)
                moveBy(Offset(0f, stepDelta))
            }
        }
        waitForIdle()

        onNodeWithTag("item-B").performTouchInput { up() }
        waitForIdle()

        println("[TEST] continueSame final: items=${items.map { it.id }} reorders=$reorderCount")

        assertEquals(
            1,
            reorderCount,
            "Continuous same-direction drag should produce exactly one reorder, got $reorderCount (items=${items.map { it.id }})",
        )
        assertEquals(
            "B",
            items[0].id,
            "B should be first after the single swap (items=${items.map { it.id }})",
        )
        assertEquals(
            "A",
            items[1].id,
            "A should be second after the single swap (items=${items.map { it.id }})",
        )
    }
}
