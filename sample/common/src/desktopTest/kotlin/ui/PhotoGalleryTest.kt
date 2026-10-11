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
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives the photo gallery gestures end to end. */
@OptIn(ExperimentalTestApi::class)
class PhotoGalleryTest {

    @Test
    fun photoGallery_dragSelectsARangeThenMovesItToAnAlbum() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { PhotoGalleryScreen(onBack = {}) } }
        waitForIdle()

        // Long-press photo 1 and drag across to photo 3
        val first = photoCenter(1)
        val third = photoCenter(3)
        onRoot().performTouchInput { longPress(first) }
        waitForIdle()
        onRoot().performTouchInput {
            glideTo(first, third)
            up()
        }
        waitForIdle()

        onNodeWithText("3 selected").assertExists()

        // Long-press a selected photo and carry the selection onto "Travel"
        val second = photoCenter(2)
        val travel = centerOf("Travel")
        onRoot().performTouchInput { longPress(second) }
        waitForIdle()
        onRoot().performTouchInput { glideTo(second, travel) }
        waitForIdle()
        onRoot().performTouchInput { up() }
        waitForIdle()

        onNodeWithText("3 selected").assertDoesNotExist()
        onNodeWithText("3 photos").assertExists()
        assertTrue(photoGone(1), "Photo 1 should have moved")
        assertTrue(photoGone(2), "Photo 2 should have moved")
        assertTrue(photoGone(3), "Photo 3 should have moved")
    }

    @Test
    fun photoGallery_tapTogglesAndClearEmptiesTheSelection() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { PhotoGalleryScreen(onBack = {}) } }
        waitForIdle()

        onNodeWithContentDescription("Photo 4").performClick()
        onNodeWithContentDescription("Photo 6").performClick()
        waitForIdle()
        onNodeWithText("2 selected").assertExists()

        onNodeWithContentDescription("Photo 4").performClick()
        waitForIdle()
        onNodeWithText("1 selected").assertExists()

        onNodeWithText("Clear").performClick()
        waitForIdle()
        onNodeWithText("1 selected").assertDoesNotExist()
        onNodeWithText("Library").assertExists()
    }

    private fun ComposeUiTest.photoCenter(number: Int): Offset =
        onNodeWithContentDescription("Photo $number").fetchSemanticsNode().boundsInRoot.center

    private fun ComposeUiTest.photoGone(number: Int): Boolean =
        onAllNodesWithContentDescription("Photo $number").fetchSemanticsNodes().isEmpty()
}
