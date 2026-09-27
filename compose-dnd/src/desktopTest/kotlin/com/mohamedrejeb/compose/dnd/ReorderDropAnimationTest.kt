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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggedItemState
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import kotlin.math.abs
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * When a list reorders in onDragEnter, the sibling slot that triggered the
 * reorder moves away from the release point while the dragged item takes its
 * place. A drag that ends without further pointer movement (fast, jerky
 * motion) still has that sibling as the hovered target, so the drop animation
 * must not chase the sibling's live bounds — it must land the ghost where the
 * dragged item actually settles.
 */
@OptIn(ExperimentalTestApi::class)
class ReorderDropAnimationTest {

    private class ListHarness {
        var items by mutableStateOf(listOf("item1", "item2", "item3"))

        fun moveOnto(target: String, dragged: DraggedItemState<String>) {
            items = items.toMutableList().apply {
                val index = indexOf(target)
                if (index != -1) {
                    remove(dragged.data)
                    add(index, dragged.data)
                }
            }
        }
    }

    // Three 400x100 items in a plain Column: slots at y = 0, 100, 200.
    @androidx.compose.runtime.Composable
    private fun ReorderColumn(
        harness: ListHarness,
        dndState: DragAndDropState<String>,
        reorderOnEnter: Boolean,
    ) {
        Column(Modifier.fillMaxSize().testTag("list")) {
            harness.items.forEach { item ->
                key(item) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .reorderableItem(
                                key = item,
                                data = item,
                                state = dndState,
                                onDragEnter = { dragged ->
                                    if (reorderOnEnter) harness.moveOnto(item, dragged)
                                },
                                onDrop = { dragged ->
                                    if (!reorderOnEnter) harness.moveOnto(item, dragged)
                                },
                                draggableContent = {
                                    Box(Modifier.fillMaxWidth().height(100.dp))
                                },
                            ),
                    )
                }
            }
        }
    }

    private fun androidx.compose.ui.test.ComposeUiTest.dragItem1ToItem3Slot(
        dndState: () -> DragAndDropState<String>,
    ) {
        // Start the drag on item1 and move past touch slop, staying in slot 1
        onNodeWithTag("list").performTouchInput {
            down(Offset(200f, 50f))
            repeat(3) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()
        assertTrue(dndState().isDragging("item1"), "Drag should have started")

        // One fast jump into item3's slot — no further movement before release
        onNodeWithTag("list").performTouchInput {
            advanceEventTime(16)
            moveBy(Offset(0f, 170f))
        }
        waitForIdle()
    }

    private fun androidx.compose.ui.test.ComposeUiTest.releaseAndTrackGhostY(
        dndState: () -> DragAndDropState<String>,
    ): Float {
        mainClock.autoAdvance = false
        onNodeWithTag("list").performTouchInput { up() }

        var lastGhostY = Float.NaN
        var frames = 0
        while (dndState().draggedItem != null && frames < 1000) {
            lastGhostY = dndState().dragPosition.value.y + dndState().dragPositionAnimatable.value.y
            mainClock.advanceTimeBy(16)
            frames++
        }
        mainClock.autoAdvance = true
        waitForIdle()

        assertTrue(frames < 1000, "Drop animation should have finished")
        return lastGhostY
    }

    @Test
    fun fastReorderDrop_ghostLandsOnDraggedItemsFinalSlot() = runComposeUiTest {
        val harness = ListHarness()
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()
            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.size(400.dp, 600.dp),
            ) {
                ReorderColumn(harness, dndState, reorderOnEnter = true)
            }
        }
        waitForIdle()

        dragItem1ToItem3Slot { dndState }

        // Entering item3's slot already reordered the list; the release comes
        // before any further pointer move re-evaluates the hovered target
        assertEquals(listOf("item2", "item3", "item1"), harness.items)
        assertEquals("item3", dndState.hoveredDropTargetKey)

        val lastGhostY = releaseAndTrackGhostY { dndState }

        assertTrue(
            abs(lastGhostY - 200f) <= 2f,
            "Ghost must land on the dragged item's final slot (y=200), not the " +
                "swapped neighbor's slot, but ended at y=$lastGhostY",
        )
    }

    @Test
    fun reorderOnDrop_ghostStillLandsOnHoveredSlot() = runComposeUiTest {
        val harness = ListHarness()
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()
            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.size(400.dp, 600.dp),
            ) {
                ReorderColumn(harness, dndState, reorderOnEnter = false)
            }
        }
        waitForIdle()

        dragItem1ToItem3Slot { dndState }

        // Nothing moved during the drag: the hovered slot is where the item lands
        assertEquals(listOf("item1", "item2", "item3"), harness.items)
        assertEquals("item3", dndState.hoveredDropTargetKey)

        val lastGhostY = releaseAndTrackGhostY { dndState }

        assertTrue(
            abs(lastGhostY - 200f) <= 2f,
            "Ghost must land on the hovered slot (y=200) when the list only " +
                "reorders on drop, but ended at y=$lastGhostY",
        )
        assertEquals(listOf("item2", "item3", "item1"), harness.items)
    }

    /**
     * The real-world repro: LazyColumn with animateItem, fast fling released
     * before the reorder's recomposition has even run. The sibling that
     * triggered the reorder glides away from the drop slot while the dragged
     * item glides in — the ghost must stay on the drop slot for the whole
     * animation (right from the first frame) and land on the item's settled
     * position, never chasing the sibling toward its new slot.
     */
    @Test
    fun fastReorderDropInLazyColumn_ghostStaysOnDropSlot() = runComposeUiTest {
        val harness = ListHarness()
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(400.dp).height(600.dp),
            ) {
                LazyColumn(Modifier.testTag("list")) {
                    items(harness.items, key = { it }) { item ->
                        Box(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .height(100.dp)
                                .reorderableItem(
                                    key = item,
                                    data = item,
                                    state = dndState,
                                    onDragEnter = { dragged ->
                                        if (dragged.data != item) harness.moveOnto(item, dragged)
                                    },
                                    draggableContent = {
                                        Box(Modifier.fillMaxWidth().height(100.dp))
                                    },
                                ),
                        )
                    }
                }
            }
        }
        waitForIdle()

        // Start the drag on item1 and move past touch slop, staying in slot 1
        onNodeWithTag("list").performTouchInput {
            down(Offset(200f, 50f))
            repeat(3) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()
        assertTrue(dndState.isDragging("item1"), "Drag should have started")

        // Fast fling into item3's slot and release with the clock paused, so
        // not a single recomposition frame runs between the swap and the drop
        mainClock.autoAdvance = false
        onNodeWithTag("list").performTouchInput {
            advanceEventTime(16)
            moveBy(Offset(0f, 170f))
        }
        assertEquals(listOf("item2", "item3", "item1"), harness.items)
        assertEquals("item3", dndState.hoveredDropTargetKey)

        onNodeWithTag("list").performTouchInput { up() }
        val releaseGhostY = dndState.dragPosition.value.y

        var lastGhostY = Float.NaN
        var minGhostY = Float.MAX_VALUE
        var frames = 0
        while (dndState.draggedItem != null && frames < 1000) {
            val ghostY = dndState.dragPosition.value.y + dndState.dragPositionAnimatable.value.y
            lastGhostY = ghostY
            minGhostY = min(minGhostY, ghostY)
            mainClock.advanceTimeBy(16)
            frames++
        }
        mainClock.autoAdvance = true
        waitForIdle()

        assertTrue(frames < 1000, "Drop animation should have finished")
        assertTrue(
            abs(lastGhostY - 200f) <= 2f,
            "Ghost must land on the dragged item's settled slot (y=200) " +
                "but ended at y=$lastGhostY",
        )
        assertTrue(
            minGhostY >= releaseGhostY - 20f,
            "Ghost must stay on the drop slot for the whole animation, but it " +
                "dipped to y=$minGhostY (release was y=$releaseGhostY) — it " +
                "chased the swapped sibling toward the wrong slot",
        )
    }

    /**
     * Issue #73 follow-up: scrolling the list while the ghost is settling
     * after a hover reorder must not stall the drop until the scroll stops.
     * The settle check has to ignore motion shared by every item (scroll)
     * and only wait for the reorder's own placement animation.
     */
    @Test
    fun scrollDuringReorderSettle_dropFinishesWhileScrolling() = runComposeUiTest {
        val harness = ListHarness()
        harness.items = (1..12).map { "item$it" }
        lateinit var dndState: DragAndDropState<String>
        lateinit var listState: LazyListState

        setContent {
            dndState = rememberDragAndDropState()
            listState = rememberLazyListState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(400.dp).height(600.dp),
            ) {
                LazyColumn(state = listState, modifier = Modifier.testTag("list")) {
                    items(harness.items, key = { it }) { item ->
                        Box(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .height(100.dp)
                                .reorderableItem(
                                    key = item,
                                    data = item,
                                    state = dndState,
                                    onDragEnter = { dragged ->
                                        if (dragged.data != item) harness.moveOnto(item, dragged)
                                    },
                                    draggableContent = {
                                        Box(Modifier.fillMaxWidth().height(100.dp))
                                    },
                                ),
                        )
                    }
                }
            }
        }
        waitForIdle()

        onNodeWithTag("list").performTouchInput {
            down(Offset(200f, 50f))
            repeat(3) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()
        assertTrue(dndState.isDragging("item1"), "Drag should have started")

        mainClock.autoAdvance = false
        onNodeWithTag("list").performTouchInput {
            advanceEventTime(16)
            moveBy(Offset(0f, 170f))
        }
        assertEquals("item3", dndState.hoveredDropTargetKey)
        onNodeWithTag("list").performTouchInput { up() }

        // Scroll 1px every frame for longer than the settle timeout
        val scrollFrames = 200
        var frames = 0
        var lastGhostY = Float.NaN
        while (dndState.draggedItem != null && frames < scrollFrames) {
            lastGhostY = dndState.dragPosition.value.y + dndState.dragPositionAnimatable.value.y
            runOnUiThread { listState.dispatchRawDelta(1f) }
            mainClock.advanceTimeBy(16)
            frames++
        }
        val itemY = dndState.draggableItemMap["item1"]
            ?.layoutCoordinates
            ?.takeIf { it.isAttached }
            ?.positionInRoot()
            ?.y
        mainClock.autoAdvance = true
        waitForIdle()

        assertTrue(
            frames < 110,
            "Drop should finish while the list is still scrolling, but took $frames frames",
        )
        assertTrue(itemY != null, "Dragged item should still be laid out")
        assertTrue(
            abs(lastGhostY - itemY) <= 3f,
            "Ghost should land on the scrolled item (y=$itemY) but ended at y=$lastGhostY",
        )
    }
}
