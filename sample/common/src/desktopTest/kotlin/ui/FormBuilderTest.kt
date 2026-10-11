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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives the form builder gestures end to end. */
@OptIn(ExperimentalTestApi::class)
class FormBuilderTest {

    @Test
    fun formBuilder_paletteFieldDropsIntoFormAndStaysInPalette() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { FormBuilderScreen(onBack = {}) } }
        waitForIdle()

        onNodeWithText("Work email").assertDoesNotExist()
        val chip = centerOf("Email")
        val emptyForm = centerOf("Drag a field here")
        onRoot().performTouchInput {
            down(chip)
            glideTo(chip, emptyForm)
            up()
        }
        waitForIdle()

        onNodeWithText("Work email").assertExists()
        onNodeWithText("1 field").assertExists()
        onNodeWithText("Email").assertExists()
    }

    @Test
    fun formBuilder_paletteFieldDroppedOnAFieldLandsAboveIt() = runDesktopComposeUiTest(width = 480, height = 900) {
        setContent { AppTheme { FormBuilderScreen(onBack = {}, initialFields = listOf(FieldType.Email)) } }
        waitForIdle()

        val chip = centerOf("Short text")
        val field = centerOf("Work email")
        onRoot().performTouchInput {
            down(chip)
            glideTo(chip, field)
            up()
        }
        waitForIdle()

        onNodeWithText("2 fields").assertExists()
        assertTrue(centerOf("Full name").y < centerOf("Work email").y, "The new field must land above the one it was dropped on")
        onNodeWithText("Short text").assertExists()
    }
}
