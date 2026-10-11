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
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives both modes of the "Move between lists" screen end to end. */
@OptIn(ExperimentalTestApi::class)
class BetweenListsTest {

    @Test
    fun append_dropOnARowStillAddsToTheEnd() = runDesktopComposeUiTest(width = WIDTH, height = HEIGHT) {
        openScreen()
        val mayaBefore = centerOf("Maya Chen")

        drag(from = mayaBefore, to = centerOf("Tom Weber"))

        val maya = centerOf("Maya Chen")
        val ravi = centerOf("Ravi Patel")
        assertSameColumn("Ravi Patel", "Maya Chen")
        assertTrue(maya.y > ravi.y, "Append puts the person after the last member, got $maya vs $ravi")
        assertEquals(mayaBefore.y, centerOf("Leo Park").y, 1f, "Design closes the gap Maya left")
    }

    @Test
    fun append_dropOnOwnTeamKeepsTheOrder() = runDesktopComposeUiTest(width = WIDTH, height = HEIGHT) {
        openScreen()
        val mayaBefore = centerOf("Maya Chen")
        val omarBefore = centerOf("Omar Aziz")

        drag(from = mayaBefore, to = omarBefore)

        assertEquals(mayaBefore, centerOf("Maya Chen"), "A person only drops on the other team")
        assertEquals(omarBefore, centerOf("Omar Aziz"))
    }

    @Test
    fun insert_hoverARowPlacesThePersonThere() = runDesktopComposeUiTest(width = WIDTH, height = HEIGHT) {
        openScreen()
        onNodeWithText("Insert anywhere").performClick()
        waitForIdle()

        drag(from = centerOf("Maya Chen"), to = centerOf("Tom Weber"))

        val maya = centerOf("Maya Chen")
        val nia = centerOf("Nia Okafor")
        assertSameColumn("Nia Okafor", "Maya Chen")
        assertTrue(maya.y < nia.y, "Insert places the person among the rows, got $maya vs $nia")
        assertTrue(maya.y < centerOf("Ravi Patel").y, "The person is not appended")
    }

    @Test
    fun insert_reordersInsideATeam() = runDesktopComposeUiTest(width = WIDTH, height = HEIGHT) {
        openScreen()
        onNodeWithText("Insert anywhere").performClick()
        waitForIdle()
        val leoBefore = centerOf("Leo Park")

        drag(from = centerOf("Maya Chen"), to = centerOf("Ines Duarte"))

        val maya = centerOf("Maya Chen")
        assertSameColumn("Leo Park", "Maya Chen")
        assertTrue(maya.y > centerOf("Leo Park").y, "Maya moved down past Leo")
        assertTrue(centerOf("Leo Park").y < leoBefore.y, "Leo moved up into Maya's old spot")
    }

    private fun ComposeUiTest.openScreen() {
        setContent { AppTheme { BetweenListsScreen(onBack = {}) } }
        waitForIdle()
    }

    /** Presses on [from], glides to [to] and releases. The screen drags without a long press. */
    private fun ComposeUiTest.drag(from: Offset, to: Offset) {
        onRoot().performTouchInput {
            down(from)
            glideTo(from, to, steps = 20)
            up()
        }
        waitForIdle()
    }

    /** Names start at the same x in a column, whatever their length. */
    private fun ComposeUiTest.assertSameColumn(expected: String, actual: String) {
        val expectedX = topLeftOf(expected).x
        val actualX = topLeftOf(actual).x
        assertTrue(abs(expectedX - actualX) < 1f, "Expected $actual in the column of $expected: $actualX vs $expectedX")
    }

    private companion object {
        const val WIDTH = 420
        const val HEIGHT = 880
    }
}
