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

/**
 * Drives the gestures of the new demo screens end to end, so the sample keeps
 * showing what it claims to show.
 */
@OptIn(ExperimentalTestApi::class)
class NewDemosTest {

    private fun ComposeUiTest.centerOf(text: String): Offset =
        onNodeWithText(text).fetchSemanticsNode().boundsInRoot.center

    private fun ComposeUiTest.topLeftOf(text: String): Offset =
        onNodeWithText(text).fetchSemanticsNode().boundsInRoot.topLeft

    private fun TouchInjectionScope.longPress(at: Offset) {
        down(at)
        advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
        moveBy(Offset(0f, 1f))
    }

    private fun TouchInjectionScope.glideTo(from: Offset, to: Offset, steps: Int = 15) {
        val delta = (to - from) / steps.toFloat()
        repeat(steps) {
            advanceEventTime(16)
            moveBy(delta)
        }
    }

    // -- Photo Gallery --

    @Test
    fun photoGallery_dragSelectsARangeThenMovesItToAnAlbum() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { PhotoGalleryScreen(onBack = {}) } }
        waitForIdle()

        // Long-press photo 1 and drag across to photo 3
        val first = centerOf("1")
        val third = centerOf("3")
        onRoot().performTouchInput { longPress(first) }
        waitForIdle()
        onRoot().performTouchInput {
            glideTo(first, third)
            up()
        }
        waitForIdle()

        onNodeWithText("3 selected").assertExists()

        // Long-press a selected photo and carry the selection onto "Travel"
        val second = centerOf("2")
        val travel = centerOf("Travel")
        onRoot().performTouchInput { longPress(second) }
        waitForIdle()
        onRoot().performTouchInput { glideTo(second, travel) }
        waitForIdle()
        onRoot().performTouchInput { up() }
        waitForIdle()

        onNodeWithText("Photo Gallery").assertExists()
        assertTrue(onAllNodesWithText("1").fetchSemanticsNodes().isEmpty(), "Photo 1 should have moved")
        assertTrue(onAllNodesWithText("2").fetchSemanticsNodes().isEmpty(), "Photo 2 should have moved")
    }

    // -- Sticky Notes Board --

    @Test
    fun stickyNotes_dropSnapsTheNoteToTheGrid() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { StickyNotesBoardScreen(onBack = {}) } }
        waitForIdle()

        val before = topLeftOf("Ship 1.0 alpha")
        val start = before + Offset(10f, 10f)
        onRoot().performTouchInput {
            down(start)
            glideTo(start, start + Offset(100f, 50f))
            up()
        }
        waitForIdle()

        // Density 1 in tests: a 24px grid, the note moved by (100, 50) snaps to (96, 48)
        val moved = topLeftOf("Ship 1.0 alpha") - before
        assertTrue(abs(moved.x - 96f) <= 1f && abs(moved.y - 48f) <= 1f, "Note should snap to the grid, moved by $moved")
    }

    @Test
    fun stickyNotes_dropOnTrashDeletesTheNote() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { StickyNotesBoardScreen(onBack = {}) } }
        waitForIdle()

        val start = topLeftOf("Write docs") + Offset(10f, 10f)
        val trash = onNodeWithContentDescription("Trash").fetchSemanticsNode().boundsInRoot.center
        onRoot().performTouchInput {
            down(start)
            glideTo(start, trash)
            up()
        }
        waitForIdle()

        assertTrue(onAllNodesWithText("Write docs").fetchSemanticsNodes().isEmpty(), "Note should be deleted")
    }

    // -- Pinned Chats --

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

        onNodeWithText("Saved order (1): Alex · Design team · Mom · Release crew").assertExists()
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
        onNodeWithText("Saved order (1): Design team · Mom · Release crew · Alex").assertExists()
    }
}
