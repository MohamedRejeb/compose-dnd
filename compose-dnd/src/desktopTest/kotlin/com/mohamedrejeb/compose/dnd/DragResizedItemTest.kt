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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * An item that resizes while it is being dragged (e.g. a container that
 * collapses when picked up) must be hit-tested and drop-animated with the
 * size of the ghost the user sees, not the size snapshotted at drag start.
 * The ghost's size is never animated on drop — only its position — so a
 * size difference between the ghost and the landing item is absorbed by
 * the drop anchor (centered by default).
 */
@OptIn(ExperimentalTestApi::class)
class DragResizedItemTest {

    // Item is 200 tall at rest and collapses to 50 while dragged.
    // The drop target sits at y = 400..500, far below the collapsed item.
    @Composable
    private fun CollapsingItemContent(
        dndState: DragAndDropState<String>,
        dropAlignment: Alignment = Alignment.Center,
        onDrop: () -> Unit = {},
        withTarget: Boolean = true,
    ) {
        val itemHeight = if (dndState.isDragging("item")) 50.dp else 200.dp

        Box(Modifier.fillMaxSize()) {
            DraggableItem(
                state = dndState,
                key = "item",
                data = "data",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .testTag("draggable"),
            ) {
                Box(Modifier.fillMaxWidth().height(itemHeight))
            }

            if (withTarget) {
                Box(
                    modifier = Modifier
                        .offset(y = 400.dp)
                        .fillMaxWidth()
                        .height(100.dp)
                        .dropTarget(
                            key = "target",
                            state = dndState,
                            dropAlignment = dropAlignment,
                            onDrop = { onDrop() },
                        ),
                )
            }
        }
    }

    @Test
    fun hover_usesLiveItemSize_afterItemCollapsesDuringDrag() = runComposeUiTest {
        var dropped = false
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.size(400.dp, 600.dp),
            ) {
                CollapsingItemContent(dndState = dndState, onDrop = { dropped = true })
            }
        }

        waitForIdle()

        // Start the drag near the top of the tall item and move past touch slop
        onNodeWithTag("draggable").performTouchInput {
            down(Offset(200f, 25f))
            repeat(5) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()

        assertTrue(dndState.isDragging("item"), "Drag should have started")

        // Finger at y = 275: the collapsed rect ends around y = 282, nowhere
        // near the target at y = 400. Only the stale 200-tall rect reaches it.
        onNodeWithTag("draggable").performTouchInput {
            repeat(20) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()

        assertNull(
            dndState.hoveredDropTargetKey,
            "Collapsed item far above the target must not hover it",
        )

        // Finger at y = 425: the collapsed rect now overlaps the target
        onNodeWithTag("draggable").performTouchInput {
            repeat(15) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()

        assertEquals(
            "target",
            dndState.hoveredDropTargetKey,
            "Collapsed item over the target must hover it",
        )

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()

        assertTrue(dropped, "Drop should land on the hovered target")
    }

    @Test
    fun dropAnimation_landsAtTargetAlignment_usingLiveItemSize() = runComposeUiTest {
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.size(400.dp, 600.dp),
            ) {
                CollapsingItemContent(
                    dndState = dndState,
                    dropAlignment = Alignment.TopStart,
                )
            }
        }

        waitForIdle()

        // Drag until the collapsed item overlaps the target at y = 400..500
        onNodeWithTag("draggable").performTouchInput {
            down(Offset(200f, 25f))
            repeat(40) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()

        assertEquals("target", dndState.hoveredDropTargetKey)

        // Release with the clock paused and record where the ghost settles
        mainClock.autoAdvance = false
        onNodeWithTag("draggable").performTouchInput { up() }

        var lastGhostY = Float.NaN
        var lastGhostHeight = Float.NaN
        var frames = 0
        while (dndState.draggedItem != null && frames < 1000) {
            lastGhostY = dndState.dragPosition.value.y
            lastGhostHeight = dndState.draggedItemVisibleSize?.height ?: lastGhostHeight
            mainClock.advanceTimeBy(16)
            frames++
        }
        mainClock.autoAdvance = true
        waitForIdle()

        assertTrue(frames < 1000, "Drop animation should have finished")
        assertTrue(
            abs(lastGhostY - 400f) <= 2f,
            "Ghost must land at the target's TopStart (y=400) using the visible " +
                "collapsed size, but ended at y=$lastGhostY",
        )
        assertTrue(
            abs(lastGhostHeight - 50f) <= 2f,
            "Ghost must keep the visible collapsed height (50) but ended at $lastGhostHeight",
        )
    }

    /**
     * Mirrors the "Item to Item (Two Way)" sample: one logical item rendered at
     * 200dp inside zone A or at 100dp inside zone B, moved between the zones by
     * onDragEnter/onDragExit. Hit-testing must follow the ghost the user sees
     * (the drag shadow), not the live item size — the live size changes as a
     * *result* of enter/exit here, so using it feeds back into hover and
     * oscillates. The drop must land the ghost centered on the drop target.
     */
    @Test
    fun zoneResizesItemOnEnterExit_hoverIsStable_andDropCentersGhost() = runComposeUiTest {
        var itemIndex by mutableStateOf(0)
        var enterBCount = 0
        var exitBCount = 0
        lateinit var dndState: DragAndDropState<Int>

        setContent {
            dndState = rememberDragAndDropState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.size(400.dp, 600.dp),
            ) {
                Box(Modifier.fillMaxSize().testTag("scene")) {
                    // Zone A: y in 0..280, hosts the item at 200dp
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .dropTarget(
                                key = "A",
                                state = dndState,
                                onDragEnter = { if (it.data == 1) itemIndex = 0 },
                                onDragExit = { if (it.data == 1) itemIndex = 1 },
                            ),
                    ) {
                        if (itemIndex == 0) {
                            DraggableItem(
                                state = dndState,
                                key = "item",
                                data = 0,
                                dropTargets = listOf("B"),
                                draggableContent = { Box(Modifier.size(200.dp)) },
                                modifier = Modifier.size(200.dp),
                            ) {
                                Box(Modifier.size(200.dp))
                            }
                        }
                    }

                    // Zone B: y in 320..600, hosts the item at 100dp
                    Box(
                        modifier = Modifier
                            .offset(y = 320.dp)
                            .fillMaxWidth()
                            .height(280.dp)
                            .dropTarget(
                                key = "B",
                                state = dndState,
                                onDragEnter = {
                                    if (it.data == 0) {
                                        enterBCount++
                                        itemIndex = 1
                                    }
                                },
                                onDragExit = {
                                    if (it.data == 0) {
                                        exitBCount++
                                        itemIndex = 0
                                    }
                                },
                            ),
                    ) {
                        if (itemIndex == 1) {
                            DraggableItem(
                                state = dndState,
                                key = "item",
                                data = 1,
                                dropTargets = listOf("A"),
                                draggableContent = { Box(Modifier.size(100.dp)) },
                                modifier = Modifier.size(100.dp),
                            ) {
                                Box(Modifier.size(100.dp))
                            }
                        }
                    }
                }
            }
        }

        waitForIdle()

        // Drag the 200dp item from zone A straight down deep into zone B,
        // letting recomposition run between steps so enter/exit-driven item
        // recreation can influence the next hover evaluation
        onNodeWithTag("scene").performTouchInput { down(Offset(100f, 100f)) }
        waitForIdle()
        repeat(36) {
            onNodeWithTag("scene").performTouchInput {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
            waitForIdle()
        }

        assertEquals("B", dndState.hoveredDropTargetKey, "Item should hover zone B")
        assertEquals(1, enterBCount, "Hover must not oscillate when entering resizes the item")
        assertEquals(0, exitBCount, "Hover must not oscillate when entering resizes the item")

        // Release and record where the ghost settles
        mainClock.autoAdvance = false
        onNodeWithTag("scene").performTouchInput { up() }

        var lastGhost = Offset.Unspecified
        var lastGhostSize = Size.Zero
        var frames = 0
        while (dndState.draggedItem != null && frames < 1000) {
            lastGhost = dndState.dragPosition.value
            lastGhostSize = dndState.draggedItemVisibleSize ?: lastGhostSize
            mainClock.advanceTimeBy(16)
            frames++
        }
        mainClock.autoAdvance = true
        waitForIdle()

        assertTrue(frames < 1000, "Drop animation should have finished")
        // The ghost keeps its visible size (200dp) while its center travels
        // to zone B's center at (200, 460) — size is not animated on drop,
        // the size difference is absorbed by the centered drop anchor
        val ghostCenterX = lastGhost.x + lastGhostSize.width / 2f
        val ghostCenterY = lastGhost.y + lastGhostSize.height / 2f
        assertTrue(
            abs(ghostCenterX - 200f) <= 2f && abs(ghostCenterY - 460f) <= 2f,
            "Ghost center must land on zone B's center (200, 460) " +
                "but ended at ($ghostCenterX, $ghostCenterY)",
        )
        assertTrue(
            abs(lastGhostSize.width - 200f) <= 2f && abs(lastGhostSize.height - 200f) <= 2f,
            "Ghost must keep its visible size (200x200), size is not animated " +
                "on drop, but ended at $lastGhostSize",
        )
    }

    @Test
    fun liveSize_fallsBackToDragStartSize_whileLiveEntryIsUnmeasured() = runComposeUiTest {
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.size(400.dp, 600.dp),
            ) {
                CollapsingItemContent(dndState = dndState, withTarget = false)
            }
        }

        waitForIdle()

        onNodeWithTag("draggable").performTouchInput {
            down(Offset(200f, 25f))
            repeat(5) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()

        assertEquals(
            Size(400f, 50f),
            dndState.draggedItemLiveSize,
            "Live size should reflect the collapsed item",
        )

        // Simulate the item being recreated under the same key mid-drag
        // (e.g. moved to a sibling container) before its first measurement
        dndState.draggableItemMap["item"]?.size = Size.Zero

        assertEquals(
            Size(400f, 200f),
            dndState.draggedItemLiveSize,
            "An unmeasured live entry must fall back to the drag-start size",
        )

        onNodeWithTag("draggable").performTouchInput { up() }
        waitForIdle()
    }

    @Test
    fun releaseWithoutTarget_ghostReturnsToLiveItemPosition() = runComposeUiTest {
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.size(400.dp, 600.dp),
            ) {
                CollapsingItemContent(dndState = dndState, withTarget = false)
            }
        }

        waitForIdle()

        onNodeWithTag("draggable").performTouchInput {
            down(Offset(200f, 25f))
            repeat(25) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()

        assertTrue(dndState.isDragging("item"), "Drag should have started")

        mainClock.autoAdvance = false
        onNodeWithTag("draggable").performTouchInput { up() }

        var lastGhostY = Float.NaN
        var frames = 0
        while (dndState.draggedItem != null && frames < 1000) {
            lastGhostY = dndState.dragPosition.value.y
            mainClock.advanceTimeBy(16)
            frames++
        }
        mainClock.autoAdvance = true
        waitForIdle()

        assertTrue(frames < 1000, "Drop animation should have finished")
        assertTrue(
            abs(lastGhostY - 0f) <= 2f,
            "Ghost must return to the item's live position (y=0), " +
                "not offset by the stale size, but ended at y=$lastGhostY",
        )
    }
}
