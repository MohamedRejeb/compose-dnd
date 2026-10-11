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

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives the quick settings list: rows toggle, only the grip reorders. */
@OptIn(ExperimentalTestApi::class)
class DragHandleReorderTest {

    @Test
    fun dragHandle_tapTogglesTheSetting() = runScreen {
        onNodeWithText("Wi-Fi").assertIsOn()
        onNodeWithText("Wi-Fi").performClick()
        waitForIdle()

        onNodeWithText("Wi-Fi").assertIsOff()
        onNodeWithText("3 on").assertExists()
    }

    @Test
    fun dragHandle_gripReordersTheRow() = runScreen {
        val grip = onNodeWithContentDescription("Reorder Wi-Fi")
            .fetchSemanticsNode()
            .boundsInRoot.center
        val to = centerOf("Do not disturb")
        onRoot().performTouchInput {
            down(grip)
            glideTo(grip, grip.copy(y = to.y))
            up()
        }
        waitForIdle()

        assertTrue(centerOf("Bluetooth").y < centerOf("Wi-Fi").y, "Wi-Fi must move below Bluetooth")
        onNodeWithText("Wi-Fi").assertIsOn()
    }

    @Test
    fun dragHandle_draggingTheRowBodyDoesNotReorder() = runScreen {
        val from = centerOf("Wi-Fi")
        val to = centerOf("Do not disturb")
        onRoot().performTouchInput {
            down(from)
            glideTo(from, to)
            up()
        }
        waitForIdle()

        assertTrue(centerOf("Wi-Fi").y < centerOf("Bluetooth").y, "Order must not change")
        onNodeWithText("Wi-Fi").assertIsOn()
    }

    private fun runScreen(block: ComposeUiTest.() -> Unit) =
        runDesktopComposeUiTest(width = 420, height = 880) {
            setContent { AppTheme { DragHandleReorderScreen(onBack = {}) } }
            waitForIdle()
            block()
        }
}
