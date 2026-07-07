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
import androidx.compose.runtime.mutableIntStateOf
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
import com.mohamedrejeb.compose.dnd.scroll.dragScrollPin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalTestApi::class, ExperimentalDndApi::class)
class ReorderTest {

    private data class SizedItem(
        val id: String,
        val heightDp: Int,
    )

    /**
     * Helper: sets up a LazyColumn with reorderable items and runs the [interaction].
     */
    private fun reorderTest(
        initialItems: List<SizedItem>,
        containerHeightDp: Int = 600,
        useDragScrollPin: Boolean = false,
        dragAfterLongPress: Boolean = true,
        useAnimateItem: Boolean = false,
        initialScrollIndex: Int = 0,
        initialScrollOffset: Int = 0,
        interaction: ReorderTestScope.() -> Unit,
        assertions: (items: List<SizedItem>, reorderCount: Int, scrollIndex: Int, scrollOffset: Int) -> Unit,
    ) = runComposeUiTest {
        var items by mutableStateOf(initialItems)
        var reorderCount by mutableIntStateOf(0)
        var scrollIndex = 0
        var scrollOffset = 0
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val dndState = rememberDragAndDropState<SizedItem>()
            val listState = rememberLazyListState(
                initialFirstVisibleItemIndex = initialScrollIndex,
                initialFirstVisibleItemScrollOffset = initialScrollOffset,
            )

            scrollIndex = listState.firstVisibleItemIndex
            scrollOffset = listState.firstVisibleItemScrollOffset

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(300.dp).height(containerHeightDp.dp),
            ) {
                val listModifier = Modifier
                    .width(300.dp)
                    .height(containerHeightDp.dp)
                    .let {
                        if (useDragScrollPin) {
                            it.dragScrollPin(state = dndState, lazyListState = listState)
                        } else {
                            it
                        }
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
                                    dragAfterLongPress = dragAfterLongPress,
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
                                ).then(if (useAnimateItem) Modifier.animateItem() else Modifier)
                                .testTag("item-${item.id}"),
                        )
                    }
                }
            }
        }

        waitForIdle()

        val scope = ReorderTestScope(density, this)
        scope.interaction()

        waitForIdle()

        assertions(items, reorderCount, scrollIndex, scrollOffset)
    }

    /**
     * Scope providing density-aware helpers for test interactions.
     */
    private class ReorderTestScope(
        val density: Density,
        private val uiTest: androidx.compose.ui.test.ComposeUiTest,
    ) {
        fun dpToPx(dp: Int): Float = with(density) { dp.dp.toPx() }

        /** Drag [tag] vertically by [dyDp] (positive = down), optionally long-pressing first and releasing at the end. */
        fun dragItem(
            tag: String,
            dyDp: Int,
            longPress: Boolean = true,
            release: Boolean = true,
        ) {
            val distancePx = dpToPx(dyDp)
            uiTest.onNodeWithTag(tag).performTouchInput {
                if (longPress) {
                    longPressDrag(
                        start = center,
                        end = Offset(center.x, center.y + distancePx),
                    )
                } else {
                    immediateDrag(
                        start = center,
                        end = Offset(center.x, center.y + distancePx),
                    )
                }
            }
            uiTest.waitForIdle()
            if (release) {
                uiTest.onNodeWithTag(tag).performTouchInput { up() }
                uiTest.waitForIdle()
            }
        }
    }

    // -- Basic reorder tests --

    @Test
    fun reorder_twoSameSizedItems_swapsOrder() = reorderTest(
        initialItems = listOf(SizedItem("A", 100), SizedItem("B", 100)),
        interaction = { dragItem("item-A", dyDp = 100) },
        assertions = { items, reorderCount, _, _ ->
            assertTrue("Reorder should have triggered", reorderCount > 0)
            assertEquals("B should be first", "B", items[0].id)
            assertEquals("A should be second", "A", items[1].id)
        },
    )

    @Test
    fun reorder_differentSizedItems_swapsOrder() = reorderTest(
        initialItems = listOf(SizedItem("A", 100), SizedItem("B", 200)),
        interaction = { dragItem("item-A", dyDp = 150) },
        assertions = { items, reorderCount, _, _ ->
            assertTrue("Reorder should have triggered", reorderCount > 0)
            assertEquals("B should be first", "B", items[0].id)
            assertEquals("A should be second", "A", items[1].id)
        },
    )

    @Test
    fun reorder_threeItems_dragFirstPastSecond() = reorderTest(
        initialItems = listOf(
            SizedItem("A", 100),
            SizedItem("B", 100),
            SizedItem("C", 100),
        ),
        interaction = { dragItem("item-A", dyDp = 100) },
        assertions = { items, reorderCount, _, _ ->
            assertTrue("Reorder should have triggered", reorderCount > 0)
            assertEquals("B should be first", "B", items[0].id)
            assertEquals("A should be second", "A", items[1].id)
            assertEquals("C should be third", "C", items[2].id)
        },
    )

    @Test
    fun reorder_dragSecondToFirst() = reorderTest(
        initialItems = listOf(SizedItem("A", 100), SizedItem("B", 100)),
        interaction = { dragItem("item-B", dyDp = -100) },
        assertions = { items, reorderCount, _, _ ->
            assertTrue("Reorder should have triggered", reorderCount > 0)
            assertEquals("B should be first", "B", items[0].id)
            assertEquals("A should be second", "A", items[1].id)
        },
    )

    @Test
    fun reorder_immediateDrag_swapsOrder() = reorderTest(
        initialItems = listOf(SizedItem("A", 100), SizedItem("B", 100)),
        dragAfterLongPress = false,
        interaction = { dragItem("item-A", dyDp = 100, longPress = false) },
        assertions = { items, reorderCount, _, _ ->
            assertTrue("Reorder should have triggered", reorderCount > 0)
            assertEquals("B should be first", "B", items[0].id)
            assertEquals("A should be second", "A", items[1].id)
        },
    )

    @Test
    fun reorder_smallDrag_doesNotReorder() = reorderTest(
        initialItems = listOf(SizedItem("A", 100), SizedItem("B", 100)),
        interaction = { dragItem("item-A", dyDp = 20) },
        assertions = { items, reorderCount, _, _ ->
            assertEquals("No reorder should happen with small drag", 0, reorderCount)
            assertEquals("A should still be first", "A", items[0].id)
            assertEquals("B should still be second", "B", items[1].id)
        },
    )

    // -- animateItem tests --

    /** Reordering with animateItem enabled must still produce a single clean swap. */
    @Test
    fun animateItem_differentSizedItems_cleanSwap() = reorderTest(
        initialItems = listOf(
            SizedItem("A", 100),
            SizedItem("B", 200),
            SizedItem("C", 150),
        ),
        useAnimateItem = true,
        interaction = { dragItem("item-A", dyDp = 150) },
        assertions = { items, reorderCount, _, _ ->
            assertTrue("Reorder should have triggered", reorderCount > 0)
            assertEquals("B should be first", "B", items[0].id)
            assertEquals("A should be second", "A", items[1].id)
        },
    )

    // -- Scroll pin tests --

    @Test
    fun reorder_differentSizedItems_withPin_scrollDoesNotJump() = reorderTest(
        initialItems = listOf(
            SizedItem("A", 100),
            SizedItem("B", 200),
            SizedItem("C", 150),
            SizedItem("D", 100),
            SizedItem("E", 120),
        ),
        containerHeightDp = 300,
        useDragScrollPin = true,
        interaction = { dragItem("item-A", dyDp = 150) },
        assertions = { items, reorderCount, scrollIndex, scrollOffset ->
            assertTrue("Reorder should have triggered", reorderCount > 0)
            assertEquals("B should be first", "B", items[0].id)
            assertEquals("Scroll index should remain 0", 0, scrollIndex)
            assertEquals("Scroll offset should remain 0", 0, scrollOffset)
        },
    )

    // -- Drag start position tests --

    /**
     * In a scrolled list, picking up an item with a tiny drag must not enter any
     * other target. A false enter means the drag start coordinates were wrong.
     */
    @Test
    fun scrolledList_pickUpThirdItem_noFalseReorder() = runComposeUiTest {
        val initialItems = listOf(
            SizedItem("A", 300),
            SizedItem("B", 100),
            SizedItem("C", 200),
            SizedItem("D", 100),
            SizedItem("E", 100),
        )
        var items by mutableStateOf(initialItems)
        var reorderCount by mutableIntStateOf(0)
        var firstEnteredTarget: String? = null
        var cWasPickedUp = false
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val dndState = rememberDragAndDropState<SizedItem>()
            // Scroll 280dp into item A so only 20dp of A is visible
            val scrollOffsetPx = with(density) { 280.dp.toPx().toInt() }
            val listState = rememberLazyListState(
                initialFirstVisibleItemIndex = 0,
                initialFirstVisibleItemScrollOffset = scrollOffsetPx,
            )

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(300.dp).height(300.dp),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .width(300.dp)
                        .height(300.dp)
                        .testTag("lazyColumn"),
                ) {
                    items(items, key = { it.id }) { item ->
                        val isDragging = dndState.isDragging(item.id)
                        if (isDragging && item.id == "C") {
                            cWasPickedUp = true
                        }

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
                                        if (firstEnteredTarget == null) {
                                            firstEnteredTarget = item.id
                                        }
                                        reorderCount++
                                        val targetIndex =
                                            items.indexOfFirst { it.id == item.id }
                                        if (targetIndex == -1) return@reorderableItem
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
                                )
                                .testTag("item-${item.id}"),
                        )
                    }
                }
            }
        }

        waitForIdle()

        val dragDistancePx = with(density) { 10.dp.toPx() }

        // Pick up C with a tiny drag, it should not cause any reorder
        onNodeWithTag("item-C").performTouchInput {
            longPressDrag(
                start = center,
                end = Offset(center.x, center.y + dragDistancePx),
            )
        }
        waitForIdle()

        println("[TEST] scrolled: items=${items.map{it.id}} reorders=$reorderCount firstEntered=$firstEnteredTarget")

        // Without a real pickup the assertions below pass without testing anything
        assertTrue(
            "C should have been picked up by the long-press drag",
            cWasPickedUp,
        )

        assertEquals(
            "Tiny drag must not enter any other target (firstEntered=$firstEnteredTarget)",
            0,
            reorderCount,
        )
        assertEquals(
            "Order must be unchanged after a tiny drag",
            listOf("A", "B", "C", "D", "E"),
            items.map { it.id },
        )

        onNodeWithTag("item-C").performTouchInput { up() }
        waitForIdle()
    }
}
