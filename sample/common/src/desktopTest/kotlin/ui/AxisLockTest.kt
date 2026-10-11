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
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import theme.AppTheme

@OptIn(ExperimentalTestApi::class)
class AxisLockTest {

    @Test
    fun axisLock_speedKnobShowsTheSpeedItIsHeldOver() = runDesktopComposeUiTest(width = 420, height = 880) {
        setContent { AppTheme { AxisLockedDragScreen(onBack = {}) } }
        waitForIdle()

        // "2x" is on the scale under the rail; the knob starts on "1x"
        onAllNodesWithText("2x").assertCountEquals(1)
        val knob = onAllNodesWithText("1x")[0].fetchSemanticsNode().boundsInRoot.center
        val lastStop = knob.copy(x = centerOf("2x").x)
        onRoot().performTouchInput {
            down(knob)
            glideTo(knob, lastStop)
        }
        waitForIdle()

        // Held over the last stop, the knob reads "2x" too
        onAllNodesWithText("2x").assertCountEquals(2)

        onRoot().performTouchInput { up() }
        waitForIdle()
        onAllNodesWithText("2x").assertCountEquals(2)
    }
}
