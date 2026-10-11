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
import androidx.compose.ui.test.onNodeWithText

/** Gesture helpers shared by the sample's screen tests. */
@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.centerOf(text: String): Offset =
    onNodeWithText(text).fetchSemanticsNode().boundsInRoot.center

@OptIn(ExperimentalTestApi::class)
internal fun ComposeUiTest.topLeftOf(text: String): Offset =
    onNodeWithText(text).fetchSemanticsNode().boundsInRoot.topLeft

internal fun TouchInjectionScope.longPress(at: Offset) {
    down(at)
    advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
    moveBy(Offset(0f, 1f))
}

internal fun TouchInjectionScope.glideTo(from: Offset, to: Offset, steps: Int = 15) {
    val delta = (to - from) / steps.toFloat()
    repeat(steps) {
        advanceEventTime(16)
        moveBy(delta)
    }
}
