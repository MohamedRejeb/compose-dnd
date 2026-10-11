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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reordering while the list auto-scrolls under the dragged item. */
@OptIn(ExperimentalTestApi::class)
class AutoScrollReorderTest {

    private var items by mutableStateOf(List(30) { "item-$it" })

    private fun move(dragged: String, target: String) {
        val to = items.indexOf(target)
        if (dragged == target || to == -1) return
        items = (items - dragged).toMutableList().apply { add(to, dragged) }
    }

    @Test
    fun itemHeldAtTheEdge_swapsWithTheItemsScrollingUnderIt() = runComposeUiTest {
        val listState = LazyListState()
        var density = Density(1f)

        setContent {
            density = LocalDensity.current
            val state = rememberDragAndDropState<String>()

            DragAndDropContainer(state = state, modifier = Modifier.size(300.dp).testTag("container")) {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxSize().dragAutoScroll(state, listState),
                ) {
                    items(items, key = { it }) { id ->
                        Box(
                            Modifier
                                .animateItem()
                                .fillMaxHeight()
                                .width(200.dp)
                                .reorderableItem(
                                    key = id,
                                    data = id,
                                    state = state,
                                    onDragEnter = { dragged -> move(dragged.data, id) },
                                    draggableContent = { Box(Modifier.size(200.dp, 300.dp)) },
                                ),
                        )
                    }
                }
            }
        }
        waitForIdle()

        fun px(dp: Int) = with(density) { dp.dp.toPx() }

        // Grab the first item by its far end and hold it at the end edge of the row
        mainClock.autoAdvance = false
        onNodeWithTag("container").performTouchInput { down(Offset(px(180), px(150))) }
        repeat(10) {
            onNodeWithTag("container").performTouchInput {
                advanceEventTime(16)
                moveBy(Offset(px(7), 0f))
            }
            mainClock.advanceTimeBy(16)
        }
        repeat(240) { mainClock.advanceTimeBy(16) }

        assertTrue("The list should have scrolled", listState.firstVisibleItemIndex > 0)
        val passed = items.indexOf("item-0")
        assertTrue("The dragged item should move along with the scroll, passed $passed", passed >= 3)
    }
}
