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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test

/** Opens demos through the real navigation graph, the way a user does. */
@OptIn(ExperimentalTestApi::class)
class AppNavigationTest {

    @Test
    fun tappingAnExampleOpensItAndBackReturnsHome() = runDesktopComposeUiTest(width = 420, height = 1900) {
        setContent { App() }
        waitForIdle()

        onNodeWithText("Photo gallery").performClick()
        waitForIdle()
        onNodeWithText("Library").assertExists()

        onNodeWithContentDescription("Back").performClick()
        waitForIdle()
        onNodeWithText("Compose DnD").assertExists()
    }

    @Test
    fun tappingAFeatureOpensIt() = runDesktopComposeUiTest(width = 420, height = 1900) {
        setContent { App() }
        waitForIdle()

        onNodeWithText("Drag handle").performClick()
        waitForIdle()
        onNodeWithText("Quick settings").assertExists()
    }
}
