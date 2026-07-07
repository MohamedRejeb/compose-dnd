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
package com.mohamedrejeb.compose.dnd

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class, ExperimentalDndApi::class)
class ReorderStateHysteresisTest {

    @Test
    fun rememberReorderState_appliesCustomHysteresisDistanceToDndState() = runComposeUiTest {
        var actualPx = -1f
        var expectedPx = -1f

        setContent {
            val density = LocalDensity.current
            val state = rememberReorderState<Int>(reorderHysteresisDistance = 24.dp)
            expectedPx = with(density) { 24.dp.toPx() }
            actualPx = state.dndState.reorderHysteresisDistancePx
        }

        waitForIdle()
        assertEquals(expectedPx, actualPx)
    }

    @Test
    fun rememberReorderState_zeroDistanceDisablesHysteresis() = runComposeUiTest {
        var actualPx = -1f

        setContent {
            val state = rememberReorderState<Int>(reorderHysteresisDistance = 0.dp)
            actualPx = state.dndState.reorderHysteresisDistancePx
        }

        waitForIdle()
        assertEquals(0f, actualPx)
    }
}
