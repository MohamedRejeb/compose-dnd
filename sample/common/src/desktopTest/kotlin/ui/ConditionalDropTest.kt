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

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives the conditional drop gestures end to end. */
@OptIn(ExperimentalTestApi::class)
class ConditionalDropTest {

    @Test
    fun conditionalDrop_acceptedFileLandsInTheFolder() = runDesktopComposeUiTest(width = 420, height = 880) {
        setContent { AppTheme { ConditionalDropScreen(onBack = {}) } }
        waitForIdle()

        val beach = centerOf("beach.jpg")
        val photos = centerOf("Photos")
        onRoot().performTouchInput {
            down(beach)
            glideTo(beach, photos)
            up()
        }
        waitForIdle()

        onNodeWithText("6 left").assertExists()
        onNodeWithText("1 of 3").assertExists()
        assertTrue(centerOf("beach.jpg").y > photos.y, "beach.jpg should now be listed inside Photos")
    }

    @Test
    fun conditionalDrop_rejectedFileShowsTheReasonAndStaysPut() = runDesktopComposeUiTest(width = 420, height = 880) {
        setContent { AppTheme { ConditionalDropScreen(onBack = {}) } }
        waitForIdle()

        val invoice = centerOf("invoice.pdf")
        val photos = centerOf("Photos")
        onRoot().performTouchInput {
            down(invoice)
            glideTo(invoice, photos)
        }
        waitForIdle()
        onNodeWithText("Only images go here").assertExists()

        onRoot().performTouchInput { up() }
        waitForIdle()

        onNodeWithText("7 left").assertExists()
        assertTrue(onAllNodesWithText("1 of 3").fetchSemanticsNodes().isEmpty(), "Photos must stay empty")
        assertTrue(onAllNodesWithText("Only images go here").fetchSemanticsNodes().isEmpty(), "Reason clears on release")
        assertEquals(invoice, centerOf("invoice.pdf"), "invoice.pdf should return to its slot")
    }

    @Test
    fun conditionalDrop_fullFolderRejectsMore() = runDesktopComposeUiTest(width = 420, height = 880) {
        val images = SampleFiles.filter { it.kind == FileKind.Image }
        val filed = images.take(FolderCapacity).associateWith { Folder.Photos }
        setContent { AppTheme { ConditionalDrop(initialPlacement = filed, onBack = {}) } }
        waitForIdle()

        val extra = images.last().name
        val start = centerOf(extra)
        val photos = centerOf("Photos")
        onRoot().performTouchInput {
            down(start)
            glideTo(start, photos)
        }
        waitForIdle()
        onNodeWithText("Photos is full").assertExists()

        onRoot().performTouchInput { up() }
        waitForIdle()

        onNodeWithText("$FolderCapacity of $FolderCapacity").assertExists()
        assertEquals(start, centerOf(extra), "$extra should return to its slot")
    }
}
