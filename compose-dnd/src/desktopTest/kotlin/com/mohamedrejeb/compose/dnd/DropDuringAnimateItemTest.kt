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
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Releasing a drag while the animateItem placement animation is still
 * running must land the drop animation on the item's settled position,
 * not on the mid-animation position captured when the swap happened.
 */
@OptIn(ExperimentalTestApi::class)
class DropDuringAnimateItemTest {

    @Test
    fun releaseWhileAnimateItemRunning_ghostLandsOnSettledSlot() = runComposeUiTest {
        var items by mutableStateOf(listOf("A", "B", "C"))
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()

            DragAndDropContainer(
                state = dndState,
                modifier = Modifier.width(300.dp).height(600.dp),
            ) {
                LazyColumn {
                    items(items, key = { it }) { item ->
                        val isDragging = dndState.isDragging(item)

                        Box(
                            modifier = Modifier
                                .animateItem()
                                .fillMaxWidth()
                                .height(100.dp)
                                .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                .reorderableItem(
                                    key = item,
                                    data = item,
                                    state = dndState,
                                    onDragEnter = { state ->
                                        if (state.data == item) return@reorderableItem
                                        items = items.toMutableList().apply {
                                            val index = indexOf(item)
                                            if (index != -1) {
                                                remove(state.data)
                                                add(index, state.data)
                                            }
                                        }
                                    },
                                    draggableContent = {
                                        Box(Modifier.fillMaxWidth().height(100.dp))
                                    },
                                )
                                .testTag("item-$item"),
                        )
                    }
                }
            }
        }

        waitForIdle()

        // Drive the drag frame by frame so the release happens right after
        // the swap, while the animateItem placement animation is running
        mainClock.autoAdvance = false
        onNodeWithTag("item-A").performTouchInput { down(center) }
        mainClock.advanceTimeBy(16)
        repeat(12) {
            onNodeWithTag("item-A").performTouchInput {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
            mainClock.advanceTimeBy(16)
        }

        assertEquals(listOf("B", "A", "C"), items, "Swap should have fired during the drag")

        onNodeWithTag("item-A").performTouchInput { up() }

        // Drive the drop animation and record where the ghost ends up
        var lastGhostY = Float.NaN
        var frames = 0
        while (dndState.draggedItem != null && frames < 1000) {
            lastGhostY = dndState.dragPosition.value.y + dndState.dragPositionAnimatable.value.y
            mainClock.advanceTimeBy(16)
            frames++
        }
        mainClock.autoAdvance = true
        waitForIdle()

        assertTrue(frames < 1000, "Drop animation should have finished")

        // A settles in the second slot at y = 100
        assertTrue(
            abs(lastGhostY - 100f) <= 3f,
            "Ghost should land on A's settled slot (y=100) but ended at y=$lastGhostY",
        )
    }
}
