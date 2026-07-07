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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
 * Repro for https://github.com/MohamedRejeb/compose-dnd/issues/46
 * Releasing a drag outside every drop target must animate the ghost to the
 * item's current slot, not to the slot it had at drag start.
 */
@OptIn(ExperimentalTestApi::class)
class DropEndAnimationTest {

    @Test
    fun releaseOutsideTargets_animatesToCurrentSlot() = runComposeUiTest {
        var items by mutableStateOf(listOf("A", "B", "C"))
        var reorderCount = 0
        var exitCount = 0
        lateinit var dndState: DragAndDropState<String>

        setContent {
            dndState = rememberDragAndDropState()

            Box(modifier = Modifier.width(300.dp).height(600.dp)) {
                DragAndDropContainer(
                    state = dndState,
                    modifier = Modifier
                        .width(300.dp)
                        .height(300.dp)
                        .align(Alignment.TopStart),
                ) {
                    Column {
                        items.forEach { item ->
                            key(item) {
                                val isDragging = dndState.isDragging(item)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                        .reorderableItem(
                                            key = item,
                                            data = item,
                                            state = dndState,
                                            onDragEnter = { state ->
                                                if (state.data == item) return@reorderableItem
                                                reorderCount++
                                                items = items.toMutableList().apply {
                                                    val index = indexOf(item)
                                                    if (index != -1) {
                                                        remove(state.data)
                                                        add(index, state.data)
                                                    }
                                                }
                                            },
                                            onDragExit = { exitCount++ },
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
            }
        }

        waitForIdle()

        // Drag A down past B and C, then keep going below the container so
        // no target is hovered anymore
        onNodeWithTag("item-A").performTouchInput {
            down(center)
            repeat(40) {
                advanceEventTime(16)
                moveBy(Offset(0f, 10f))
            }
        }
        waitForIdle()

        assertEquals(listOf("B", "C", "A"), items, "A should have moved to the end")
        assertTrue(exitCount >= 1, "onDragExit should fire when leaving all targets")

        // A's live slot is now the third row, y = 200
        val liveSlotY = 200f

        // Release with the clock paused and drive the drop animation manually,
        // recording where the ghost ends up right before the state clears
        mainClock.autoAdvance = false
        onNodeWithTag("item-A").performTouchInput { up() }

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
        assertTrue(
            abs(lastGhostY - liveSlotY) <= 2f,
            "Ghost should animate to A's current slot (y=$liveSlotY) but ended at y=$lastGhostY",
        )
    }
}
