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

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The list scrolls toward the edge the dragged item is held at, whatever its layout direction. */
@OptIn(ExperimentalTestApi::class)
class DragAutoScrollTest {

    private val listState = LazyListState()
    private var items by mutableStateOf(List(40) { "item-$it" })

    /** Puts [dragged] in the slot of [target], like a reorderable list does on drag enter. */
    private fun move(dragged: String, target: String) {
        val to = items.indexOf(target)
        if (dragged == target || to == -1) return
        items = (items - dragged).toMutableList().apply { add(to, dragged) }
    }

    private fun Modifier.testItem(id: String, state: DragAndDropState<String>): Modifier =
        reorderableItem(
            key = id,
            data = id,
            state = state,
            draggableContent = { Box(Modifier.size(ItemSize.dp)) },
        )

    private fun ComposeUiTest.setColumn(reverseLayout: Boolean = false) {
        setContent {
            val state = rememberDragAndDropState<String>()
            DragAndDropContainer(state = state, modifier = Modifier.size(ListSize.dp).testTag(ContainerTag)) {
                LazyColumn(
                    state = listState,
                    reverseLayout = reverseLayout,
                    modifier = Modifier.fillMaxSize().dragAutoScroll(state, listState),
                ) {
                    items(items, key = { it }) { id ->
                        Box(Modifier.fillMaxWidth().height(ItemSize.dp).testItem(id, state))
                    }
                }
            }
        }
        waitForIdle()
    }

    private fun ComposeUiTest.setRow(
        layoutDirection: LayoutDirection = LayoutDirection.Ltr,
        itemWidth: Int = ItemSize,
        reorderOnEnter: Boolean = false,
    ) {
        setContent {
            val state = rememberDragAndDropState<String>()
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                DragAndDropContainer(state = state, modifier = Modifier.size(ListSize.dp).testTag(ContainerTag)) {
                    LazyRow(
                        state = listState,
                        modifier = Modifier.fillMaxSize().dragAutoScroll(state, listState),
                    ) {
                        items(items, key = { it }) { id ->
                            Box(
                                Modifier
                                    .animateItem()
                                    .fillMaxHeight()
                                    .width(itemWidth.dp)
                                    .reorderableItem(
                                        key = id,
                                        data = id,
                                        state = state,
                                        onDragEnter = { dragged -> if (reorderOnEnter) move(dragged.data, id) },
                                        draggableContent = { Box(Modifier.size(itemWidth.dp, ItemSize.dp)) },
                                    ),
                            )
                        }
                    }
                }
            }
        }
        waitForIdle()
    }

    /** Drags from [from] to [to], then holds there for [holdFrames] frames. */
    private fun ComposeUiTest.dragAndHold(from: Offset, to: Offset, holdFrames: Int = 30) {
        mainClock.autoAdvance = false
        onNodeWithTag(ContainerTag).performTouchInput { down(from) }
        repeat(DragSteps) {
            onNodeWithTag(ContainerTag).performTouchInput {
                advanceEventTime(FrameMs)
                moveBy((to - from) / DragSteps.toFloat())
            }
            mainClock.advanceTimeBy(FrameMs)
        }
        repeat(holdFrames) { mainClock.advanceTimeBy(FrameMs) }
    }

    private fun scrolledPx(itemSize: Int = ItemSize): Int =
        listState.firstVisibleItemIndex * itemSize + listState.firstVisibleItemScrollOffset

    @Test
    fun column_heldAtTheBottom_scrollsForward() = runComposeUiTest {
        setColumn()
        dragAndHold(from = Offset(200f, 150f), to = Offset(200f, 380f))
        assertTrue(scrolledPx() > 0, "Should have scrolled, was ${scrolledPx()}")
    }

    @Test
    fun reversedColumn_heldAtTheTop_scrollsTowardTheItemsAbove() = runComposeUiTest {
        setColumn(reverseLayout = true)
        // Reversed: the first item sits at the bottom and the rest of the list is above
        dragAndHold(from = Offset(200f, 250f), to = Offset(200f, 20f))
        assertTrue(scrolledPx() > 0, "Should have scrolled, was ${scrolledPx()}")
    }

    @Test
    fun rtlRow_heldAtTheLeft_scrollsTowardTheItemsOnTheLeft() = runComposeUiTest {
        setRow(LayoutDirection.Rtl)
        // RTL: the first item sits at the right and the rest of the list is on the left
        dragAndHold(from = Offset(250f, 200f), to = Offset(20f, 200f))
        assertTrue(scrolledPx() > 0, "Should have scrolled, was ${scrolledPx()}")
    }

    @Test
    fun rtlScrollableRow_heldAtTheLeft_scrollsTowardTheItemsOnTheLeft() = runComposeUiTest {
        val scrollState = ScrollState(initial = 0)
        setContent {
            val state = rememberDragAndDropState<String>()
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                DragAndDropContainer(state = state, modifier = Modifier.size(ListSize.dp).testTag(ContainerTag)) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .dragAutoScroll(state, scrollState, Orientation.Horizontal)
                            .horizontalScroll(scrollState),
                    ) {
                        items.forEach { id ->
                            Box(Modifier.fillMaxHeight().width(ItemSize.dp).testItem(id, state))
                        }
                    }
                }
            }
        }
        waitForIdle()

        dragAndHold(from = Offset(250f, 200f), to = Offset(20f, 200f))
        assertTrue(scrollState.value > 0, "Should have scrolled, was ${scrollState.value}")
    }

    @Test
    fun ltrRow_heldAtTheRight_scrollsForward() = runComposeUiTest {
        setRow(LayoutDirection.Ltr)
        dragAndHold(from = Offset(150f, 200f), to = Offset(380f, 200f))
        assertTrue(scrolledPx() > 0, "Should have scrolled, was ${scrolledPx()}")
    }

    @Test
    fun itemWiderThanTheFreeSpace_grabbedInTheMiddle_scrollsOnlyOnceThePointerHeadsToTheEdge() = runComposeUiTest {
        // A phone-sized board: 280 wide columns in a 400 wide row
        setRow(itemWidth = WideItem)

        dragAndHold(from = Offset(140f, 200f), to = Offset(180f, 200f))
        assertEquals(0, listState.firstVisibleItemScrollOffset, "A small move must not scroll the board")

        repeat(DragSteps) {
            onNodeWithTag(ContainerTag).performTouchInput {
                advanceEventTime(FrameMs)
                moveBy(Offset(17f, 0f))
            }
            mainClock.advanceTimeBy(FrameMs)
        }
        repeat(30) { mainClock.advanceTimeBy(FrameMs) }
        assertTrue(scrolledPx(WideItem) > 0, "Should scroll once the pointer is at the edge")
    }

    @Test
    fun itemWiderThanTheFreeSpace_grabbedNearTheEdge_scrollsAfterAShortMove() = runComposeUiTest {
        setRow(itemWidth = WideItem)

        // The first item starts inside the start zone and is grabbed by its far end
        dragAndHold(from = Offset(250f, 200f), to = Offset(310f, 200f))
        assertTrue(scrolledPx(WideItem) > 0, "Should have scrolled, was ${scrolledPx(WideItem)}")
    }

    @Test
    fun heldAtTheEdge_keepsScrollingWhileItemsSwapUnderIt() = runComposeUiTest {
        // Board columns: each one the dragged column passes swaps with it
        setRow(itemWidth = WideItem, reorderOnEnter = true)

        dragAndHold(from = Offset(250f, 200f), to = Offset(340f, 200f), holdFrames = 240)

        val passed = items.indexOf("item-0")
        assertTrue(passed >= 4, "The dragged item should have passed several items, passed $passed")
    }

    @Test
    fun itemPickedUpAtTheEdge_doesNotScrollUntilItMoves() = runComposeUiTest {
        setColumn()
        mainClock.autoAdvance = false

        // The last visible row, just past the touch slop
        onNodeWithTag(ContainerTag).performTouchInput { down(Offset(200f, 350f)) }
        onNodeWithTag(ContainerTag).performTouchInput {
            advanceEventTime(FrameMs)
            moveBy(Offset(30f, 0f))
        }
        repeat(30) { mainClock.advanceTimeBy(FrameMs) }

        assertEquals(0, scrolledPx(), "Picking an item up must not scroll the list")
    }

    @Test
    fun column_keepsScrollingWhileThePointerKeepsMovingInTheEdgeZone() = runComposeUiTest {
        setColumn()
        mainClock.autoAdvance = false
        // Into the bottom edge zone, short of full speed
        onNodeWithTag(ContainerTag).performTouchInput { down(Offset(200f, 150f)) }
        repeat(DragSteps) {
            onNodeWithTag(ContainerTag).performTouchInput {
                advanceEventTime(FrameMs)
                moveBy(Offset(0f, 16f))
            }
            mainClock.advanceTimeBy(FrameMs)
        }
        val before = scrolledPx()

        // Creep toward the edge: the pointer moves on every single frame
        repeat(40) {
            onNodeWithTag(ContainerTag).performTouchInput {
                advanceEventTime(FrameMs)
                moveBy(Offset(0f, 0.5f))
            }
            mainClock.advanceTimeBy(FrameMs)
        }
        assertTrue(scrolledPx() > before, "Must scroll while the pointer moves, stayed at $before")
    }

    private companion object {
        const val ContainerTag = "container"
        const val ListSize = 400
        const val ItemSize = 100
        const val WideItem = 280
        const val DragSteps = 10
        const val FrameMs = 16L
    }
}
