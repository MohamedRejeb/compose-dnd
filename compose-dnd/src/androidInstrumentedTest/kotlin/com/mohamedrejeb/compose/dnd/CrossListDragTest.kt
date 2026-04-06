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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalTestApi::class, ExperimentalDndApi::class)
class CrossListDragTest {

    private data class Item(val id: String)

    /**
     * Two side-by-side LazyColumns sharing one DragAndDropState.
     * Drag an item from the left column to the right column.
     */
    @Test
    fun dragItem_fromLeftColumn_toRightColumn() = runComposeUiTest {
        var leftItems by mutableStateOf(listOf(Item("L1"), Item("L2")))
        var rightItems by mutableStateOf(listOf(Item("R1"), Item("R2")))
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val dndState = rememberDragAndDropState<Item>()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(400.dp).height(400.dp),
            ) {
                Row {
                    // Left column (200dp wide)
                    LazyColumn(
                        modifier = Modifier.width(200.dp).fillMaxHeight(),
                    ) {
                        items(leftItems, key = { it.id }) { item ->
                            val isDragging = dndState.isDragging(item.id)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                    .reorderableItem(
                                        key = item.id,
                                        data = item,
                                        state = dndState,
                                        dropStrategy = DropStrategy.CenterDistance,
                                        onDragEnter = { state ->
                                            val draggedItem = state.data
                                            if (draggedItem.id == item.id) return@reorderableItem
                                            val targetIdx = leftItems.indexOfFirst { it.id == item.id }
                                            if (targetIdx != -1) {
                                                leftItems = leftItems
                                                    .filter { it.id != draggedItem.id }
                                                    .toMutableList()
                                                    .apply { add(targetIdx.coerceAtMost(size), draggedItem) }
                                                rightItems = rightItems.filter { it.id != draggedItem.id }
                                            }
                                        },
                                        draggableContent = {
                                            Box(Modifier.fillMaxWidth().height(100.dp))
                                        },
                                    )
                                    .testTag("item-${item.id}"),
                            )
                        }
                    }

                    // Right column (200dp wide)
                    LazyColumn(
                        modifier = Modifier.width(200.dp).fillMaxHeight(),
                    ) {
                        items(rightItems, key = { it.id }) { item ->
                            val isDragging = dndState.isDragging(item.id)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                    .reorderableItem(
                                        key = item.id,
                                        data = item,
                                        state = dndState,
                                        dropStrategy = DropStrategy.CenterDistance,
                                        onDragEnter = { state ->
                                            val draggedItem = state.data
                                            if (draggedItem.id == item.id) return@reorderableItem
                                            val targetIdx = rightItems.indexOfFirst { it.id == item.id }
                                            if (targetIdx != -1) {
                                                rightItems = rightItems
                                                    .filter { it.id != draggedItem.id }
                                                    .toMutableList()
                                                    .apply { add(targetIdx.coerceAtMost(size), draggedItem) }
                                                leftItems = leftItems.filter { it.id != draggedItem.id }
                                            }
                                        },
                                        draggableContent = {
                                            Box(Modifier.fillMaxWidth().height(100.dp))
                                        },
                                    )
                                    .testTag("item-${item.id}"),
                            )
                        }
                    }
                }
            }
        }

        waitForIdle()

        // Verify initial state
        assertEquals(listOf("L1", "L2"), leftItems.map { it.id })
        assertEquals(listOf("R1", "R2"), rightItems.map { it.id })

        // Drag L1 from left column to right column (200dp to the right, onto R1)
        val horizontalPx = with(density) { 200.dp.toPx() }

        onNodeWithTag("item-L1").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x + horizontalPx, center.y),
            )
            up()
        }

        waitForIdle()

        // L1 should have moved to the right column
        assertTrue(
            "L1 should no longer be in left column",
            leftItems.none { it.id == "L1" },
        )
        assertTrue(
            "L1 should be in right column",
            rightItems.any { it.id == "L1" },
        )
    }

    /**
     * Drag an item within the same column (reorder) and verify
     * the other column is unaffected.
     */
    @Test
    fun dragItem_withinSameColumn_otherColumnUnaffected() = runComposeUiTest {
        var leftItems by mutableStateOf(listOf(Item("L1"), Item("L2"), Item("L3")))
        var rightItems by mutableStateOf(listOf(Item("R1"), Item("R2")))
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val dndState = rememberDragAndDropState<Item>()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(400.dp).height(400.dp),
            ) {
                Row {
                    LazyColumn(
                        modifier = Modifier.width(200.dp).fillMaxHeight(),
                    ) {
                        items(leftItems, key = { it.id }) { item ->
                            val isDragging = dndState.isDragging(item.id)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                    .reorderableItem(
                                        key = item.id,
                                        data = item,
                                        state = dndState,
                                        dropStrategy = DropStrategy.CenterDistance,
                                        onDragEnter = { state ->
                                            val draggedItem = state.data
                                            if (draggedItem.id == item.id) return@reorderableItem
                                            val targetIdx = leftItems.indexOfFirst { it.id == item.id }
                                            if (targetIdx != -1) {
                                                leftItems = leftItems
                                                    .filter { it.id != draggedItem.id }
                                                    .toMutableList()
                                                    .apply { add(targetIdx.coerceAtMost(size), draggedItem) }
                                                rightItems = rightItems.filter { it.id != draggedItem.id }
                                            }
                                        },
                                        draggableContent = {
                                            Box(Modifier.fillMaxWidth().height(100.dp))
                                        },
                                    )
                                    .testTag("item-${item.id}"),
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.width(200.dp).fillMaxHeight(),
                    ) {
                        items(rightItems, key = { it.id }) { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .reorderableItem(
                                        key = item.id,
                                        data = item,
                                        state = dndState,
                                        dropStrategy = DropStrategy.CenterDistance,
                                        onDragEnter = { state ->
                                            val draggedItem = state.data
                                            if (draggedItem.id == item.id) return@reorderableItem
                                            val targetIdx = rightItems.indexOfFirst { it.id == item.id }
                                            if (targetIdx != -1) {
                                                rightItems = rightItems
                                                    .filter { it.id != draggedItem.id }
                                                    .toMutableList()
                                                    .apply { add(targetIdx.coerceAtMost(size), draggedItem) }
                                                leftItems = leftItems.filter { it.id != draggedItem.id }
                                            }
                                        },
                                        draggableContent = {
                                            Box(Modifier.fillMaxWidth().height(100.dp))
                                        },
                                    )
                                    .testTag("item-${item.id}"),
                            )
                        }
                    }
                }
            }
        }

        waitForIdle()

        // Drag L1 down onto L2 (reorder within left column)
        val distancePx = with(density) { 100.dp.toPx() }

        onNodeWithTag("item-L1").performTouchInput {
            immediateDrag(
                start = center,
                end = Offset(center.x, center.y + distancePx),
            )
            up()
        }

        waitForIdle()

        // Left column should be reordered
        assertEquals("L2 should be first in left", "L2", leftItems[0].id)
        assertEquals("L1 should be second in left", "L1", leftItems[1].id)

        // Right column should be unchanged
        assertEquals("Right column should be unchanged", listOf("R1", "R2"), rightItems.map { it.id })
    }
}
