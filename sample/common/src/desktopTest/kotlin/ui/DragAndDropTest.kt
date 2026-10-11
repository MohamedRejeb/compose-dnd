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
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives the drag and drop screen: files move between the two folders. */
@OptIn(ExperimentalTestApi::class)
class DragAndDropTest {

    @Test
    fun dragAndDrop_fileMovesToOtherFolderAndBack() = runScreen {
        dragFileInto("Invoice.pdf", "Documents")
        assertInDocuments("Invoice.pdf", expected = true)
        onAllNodesWithText("2 files").assertCountEquals(2)

        dragFileInto("Invoice.pdf", "Downloads")
        assertInDocuments("Invoice.pdf", expected = false)
        onNodeWithText("3 files").assertExists()
        onNodeWithText("1 file").assertExists()
    }

    @Test
    fun dragAndDrop_dropOnOwnFolderKeepsTheFileInPlace() = runScreen {
        val before = centerOf("Beach.jpg")
        dragFileInto("Beach.jpg", "Downloads")

        assertEquals(before, centerOf("Beach.jpg"), "A file dropped on its own folder stays put")
        onNodeWithText("3 files").assertExists()
    }

    @Test
    fun dragAndDrop_longPressOptionNeedsAHoldBeforeDragging() = runScreen {
        onNodeWithText("Long press to drag").performClick()
        waitForIdle()

        // A plain drag does nothing now.
        dragFileInto("Invoice.pdf", "Documents")
        assertInDocuments("Invoice.pdf", expected = false)

        // Holding first picks the file up.
        dragFileInto("Invoice.pdf", "Documents", longPress = true)
        assertInDocuments("Invoice.pdf", expected = true)
    }

    @Test
    fun dragAndDrop_movesWithoutDropAnimation() = runScreen {
        onNodeWithText("Drop animation").performClick()
        waitForIdle()

        dragFileInto("Notes.txt", "Documents")
        assertInDocuments("Notes.txt", expected = true)
    }

    private fun runScreen(block: ComposeUiTest.() -> Unit) =
        runDesktopComposeUiTest(width = 420, height = 880) {
            setContent { AppTheme { DragAndDropScreen(onBack = {}) } }
            waitForIdle()
            block()
        }

    private fun ComposeUiTest.dragFileInto(
        file: String,
        folder: String,
        longPress: Boolean = false,
    ) {
        val from = centerOf(file)
        // Aim a little below the folder title, inside its well.
        val to = centerOf(folder) + Offset(0f, FolderBodyOffsetPx)
        onRoot().performTouchInput {
            if (longPress) longPress(from) else down(from)
            glideTo(from, to)
            up()
        }
        waitForIdle()
    }

    private fun ComposeUiTest.assertInDocuments(file: String, expected: Boolean) {
        val inDocuments = centerOf(file).y > centerOf("Documents").y
        assertTrue(inDocuments == expected, "$file in Documents: expected $expected")
    }

    private companion object {
        const val FolderBodyOffsetPx = 120f
    }
}
