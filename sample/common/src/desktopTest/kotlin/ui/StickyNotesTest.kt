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

/** Drives the sticky notes board gestures end to end. */
@OptIn(ExperimentalTestApi::class)
class StickyNotesTest {

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
}
