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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking

/**
 * Reordering in a staggered grid while it auto scrolls under the held item.
 *
 * A swap rearranges both columns, and the grid keeps one scroll offset per column:
 * anything that resets them moves other items under the held one, which swaps again.
 */
@OptIn(ExperimentalTestApi::class)
class StaggeredGridReorderScrollTest {

    private var items by mutableStateOf(List(ItemCount) { it })
    private lateinit var gridState: LazyStaggeredGridState

    @Composable
    private fun Grid() {
        CompositionLocalProvider(LocalDensity provides Density(1f)) {
            val state = rememberDragAndDropState<Int>()
            gridState = rememberLazyStaggeredGridState()
            DragAndDropContainer(state = state, modifier = Modifier.fillMaxSize().testTag(ContainerTag)) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    state = gridState,
                    verticalItemSpacing = 8.dp,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .dragAutoScroll(state = state, lazyStaggeredGridState = gridState),
                ) {
                    items(items, key = { it }) { item ->
                        Box(
                            Modifier
                                .animateItem()
                                .reorderableItem(
                                    key = item,
                                    data = item,
                                    state = state,
                                    onDragEnter = { dragged -> items = items.withItemMovedTo(item, dragged.data) },
                                    draggableContent = { Box(Modifier.fillMaxWidth().height(heightOf(item).dp)) },
                                ).fillMaxWidth()
                                .height(heightOf(item).dp),
                        )
                    }
                }
            }
        }
    }

    @Test
    fun itemHeldAtTheBottomEdge_scrollsToTheEndWithoutJumpingBack() =
        runDesktopComposeUiTest(width = 400, height = 600) {
            setContent { Grid() }
            mainClock.autoAdvance = false
            mainClock.advanceTimeBy(200)

            // Item 1 is the first one of the right column; held over both columns
            dragAndHold(from = Offset(300f, 100f), to = Offset(240f, 570f))

            val jumps = holdAndCollectJumps { before, after -> after < before - EndSettlePx }

            assertEquals(emptyList(), jumps, "The grid scrolled back while the item was held at the bottom edge")
            assertFalse(gridState.canScrollForward, "The grid should have reached its end")
            assertEquals(items.last(), 1, "The held item should have followed the scroll to the end")
        }

    @Test
    fun itemHeldAtTheTopEdge_scrollsToTheStartWithoutJumpingForward() =
        runDesktopComposeUiTest(width = 400, height = 600) {
            setContent { Grid() }
            runOnIdle { runBlocking { gridState.scrollToItem(ItemCount - 1) } }
            waitForIdle()
            assertFalse(gridState.canScrollForward)
            mainClock.autoAdvance = false
            mainClock.advanceTimeBy(200)

            val held = gridState.layoutInfo.visibleItemsInfo.last()
            val grab = Offset(held.offset.x + 100f, held.offset.y + 50f)
            dragAndHold(from = grab, to = Offset(160f, 60f))

            val jumps = holdAndCollectJumps { before, after -> after > before + EndSettlePx }

            assertEquals(emptyList(), jumps, "The grid scrolled forward while the item was held at the top edge")
            assertFalse(gridState.canScrollBackward, "The grid should have reached its start")
            assertTrue(items.indexOf(held.key) < 2, "The held item should have followed the scroll to the start, order $items")
        }

    private fun ComposeUiTest.dragAndHold(from: Offset, to: Offset) {
        onNodeWithTag(ContainerTag).performTouchInput {
            down(from)
            repeat(Steps) { step ->
                moveTo(from + (to - from) * ((step + 1f) / Steps), delayMillis = FrameMillis)
            }
        }
    }

    /** Holds the item for a while and returns the frames where the scroll went the wrong way. */
    private fun ComposeUiTest.holdAndCollectJumps(isJump: (before: Int, after: Int) -> Boolean): List<String> {
        val jumps = mutableListOf<String>()
        repeat(HoldFrames) { frame ->
            val before = scrollMark()
            mainClock.advanceTimeBy(FrameMillis)
            val after = scrollMark()
            if (isJump(before, after)) jumps += "frame $frame: $before -> $after"
        }
        return jumps
    }

    /** Grows as the grid scrolls forward: the first visible item and how far it is scrolled. */
    private fun scrollMark(): Int =
        gridState.firstVisibleItemIndex * IndexWeight + gridState.firstVisibleItemScrollOffset

    private fun List<Int>.withItemMovedTo(anchor: Int, item: Int): List<Int> {
        val index = indexOf(anchor)
        if (index == -1) return this
        val without = this - item
        return without.take(index) + item + without.drop(index)
    }

    private companion object {
        const val ContainerTag = "container"
        const val ItemCount = 24
        const val Steps = 50
        const val HoldFrames = 360
        const val FrameMillis = 16L
        const val IndexWeight = 1000

        // A swap next to the end of the grid can shorten a column by a few pixels
        const val EndSettlePx = 8

        val Heights = listOf(150, 210, 120, 180, 240, 140, 200)
        fun heightOf(item: Int) = Heights[item % Heights.size]
    }
}
