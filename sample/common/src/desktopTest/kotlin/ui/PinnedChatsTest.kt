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
package ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives the pinned chats gestures end to end. */
@OptIn(ExperimentalTestApi::class)
class PinnedChatsTest {

    @Test
    fun pinnedChats_reordersPinnedChatsAndSavesOnDragEnd() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { PinnedChatsScreen(onBack = {}) } }
        waitForIdle()

        val alex = centerOf("Alex")
        val design = centerOf("Design team")
        onRoot().performTouchInput { longPress(alex) }
        waitForIdle()
        onRoot().performTouchInput {
            glideTo(alex, design)
            up()
        }
        waitForIdle()

        onNodeWithText("Saved once").assertExists()
        onNodeWithContentDescription("Saved order: Alex, Design team, Mom, Release crew").assertExists()
    }

    @Test
    fun pinnedChats_regularChatsStayLocked() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { PinnedChatsScreen(onBack = {}) } }
        waitForIdle()

        val samBefore = centerOf("Sam")

        // Carry a pinned chat over a regular one: no swap happens
        val alex = centerOf("Alex")
        onRoot().performTouchInput { longPress(alex) }
        waitForIdle()
        onRoot().performTouchInput {
            glideTo(alex, samBefore)
            up()
        }
        waitForIdle()

        assertEquals(samBefore.y, centerOf("Sam").y, "A regular chat must keep its slot")
        onNodeWithText("Saved once").assertExists()
        onNodeWithContentDescription("Saved order: Design team, Mom, Release crew, Alex").assertExists()
    }
}
