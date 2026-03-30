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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class DragHandleTest {

    private fun TouchInjectionScope.simulateDrag(
        start: Offset,
        end: Offset,
        steps: Int = 20,
        stepDelayMs: Long = 16,
    ) {
        down(start)
        val dx = (end.x - start.x) / steps
        val dy = (end.y - start.y) / steps
        for (i in 1..steps) {
            advanceEventTime(stepDelayMs)
            moveTo(Offset(start.x + dx * i, start.y + dy * i))
        }
        up()
    }

    @Test
    fun dragHandle_triggersDropWhenDraggingFromHandle() = runComposeUiTest {
        var dropped = false

        setContent {
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    ) {
                        Row(Modifier.fillMaxWidth().height(100.dp)) {
                            // The drag handle region
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(100.dp)
                                    .testTag("handle")
                                    .dragHandle()
                            )
                            // The content region
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .testTag("content")
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .dropTarget(
                                key = "target",
                                state = state,
                                onDrop = { dropped = true },
                            )
                    )
                }
            }
        }

        // Drag from the handle
        onNodeWithTag("handle").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 300f),
            )
        }

        waitForIdle()
        assertTrue(dropped, "onDrop should be called when dragging from the handle")
    }

    @Test
    fun dragHandle_doesNotDragFromContent() = runComposeUiTest {
        var dropped = false

        setContent {
            val state = rememberDragAndDropState<Int>()

            DragAndDropContainer(
                state = state,
                modifier = Modifier.size(400.dp),
            ) {
                Column {
                    DraggableItem(
                        state = state,
                        key = "item",
                        data = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                    ) {
                        Row(Modifier.fillMaxWidth().height(100.dp)) {
                            // The drag handle region
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(100.dp)
                                    .testTag("handle")
                                    .dragHandle()
                            )
                            // The content region
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(100.dp)
                                    .testTag("content")
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .dropTarget(
                                key = "target",
                                state = state,
                                onDrop = { dropped = true },
                            )
                    )
                }
            }
        }

        // Wait for recomposition to remove the outer gesture (hasDragHandle triggers recompose)
        waitForIdle()

        // Drag from the content area (not the handle)
        onNodeWithTag("content").performTouchInput {
            simulateDrag(
                start = center,
                end = Offset(center.x, center.y + 300f),
            )
        }

        waitForIdle()
        assertFalse(dropped, "onDrop should NOT be called when dragging from content (not handle)")
    }
}
