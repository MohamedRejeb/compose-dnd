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
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives the list reorder gestures. */
@OptIn(ExperimentalTestApi::class)
class ReorderListTest {

    @Test
    fun reorderList_longPressMovesARow() = runDesktopComposeUiTest(width = 420, height = 880) {
        setContent { AppTheme { ReorderListScreen(onBack = {}) } }
        waitForIdle()

        val from = centerOf("Kotlin")
        val to = centerOf("Rust")
        onRoot().performTouchInput {
            longPress(from)
            glideTo(from, to)
            up()
        }
        waitForIdle()

        assertTrue(centerOf("Swift").y < centerOf("Rust").y)
        assertTrue(centerOf("Rust").y < centerOf("Kotlin").y, "Must land after Rust")
    }

    @Test
    fun reorderList_plainDragDoesNotReorder() = runDesktopComposeUiTest(width = 420, height = 880) {
        setContent { AppTheme { ReorderListScreen(onBack = {}) } }
        waitForIdle()

        val from = centerOf("Kotlin")
        val to = centerOf("Rust")
        onRoot().performTouchInput {
            down(from)
            glideTo(from, to)
            up()
        }
        waitForIdle()

        assertTrue(centerOf("Kotlin").y < centerOf("Swift").y, "Order changed")
    }
}
