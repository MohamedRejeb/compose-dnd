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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import theme.AppTheme

/** Drives the kanban board gestures end to end, on a window wide enough to show every column. */
@OptIn(ExperimentalTestApi::class)
class KanbanBoardTest {

    @Test
    fun kanban_cardMovesToAnotherColumn() = runDesktopComposeUiTest(width = WideWidth, height = WindowHeight) {
        setContent { AppTheme { KanbanBoardScreen(onBack = {}) } }
        waitForIdle()

        val todoLeft = topLeftOf("Set up CI pipeline").x
        val card = centerOf("Set up CI pipeline")
        val target = centerOf("Search with filters")
        onRoot().performTouchInput { longPress(card) }
        waitForIdle()
        onRoot().performTouchInput {
            glideTo(card, target)
            up()
        }
        waitForIdle()

        // Titles are left aligned, so cards in one column share the left edge of their title.
        val moved = topLeftOf("Set up CI pipeline")
        val neighbour = topLeftOf("Search with filters")
        assertTrue(abs(moved.x - neighbour.x) < 1f, "The card must now sit in the In progress column")
        assertTrue(moved.y < neighbour.y, "The card must land in the slot it was dropped on")
        assertTrue(
            abs(topLeftOf("Design the login screen").x - todoLeft) < 1f,
            "The To do column keeps its other cards",
        )
    }

    @Test
    fun kanban_columnReordersByItsHeader() = runDesktopComposeUiTest(width = WideWidth, height = WindowHeight) {
        setContent { AppTheme { KanbanBoardScreen(onBack = {}) } }
        waitForIdle()

        val todo = centerOf("To do")
        val inProgress = centerOf("In progress")
        onRoot().performTouchInput {
            down(todo)
            glideTo(todo, inProgress)
            up()
        }
        waitForIdle()

        assertTrue(centerOf("To do").x > centerOf("In progress").x, "To do must move after In progress")
    }

    @Test
    fun kanban_cardHeldInTheEmptyAreaUnderAColumn_goesLastInIt() = runDesktopComposeUiTest(width = WideWidth, height = WindowHeight) {
        setContent { AppTheme { KanbanBoardScreen(onBack = {}) } }
        waitForIdle()

        val card = centerOf("Set up CI pipeline")
        val lastInReview = centerOf("Redesign settings page")
        // Well below the last card of Review, where the column is empty
        val emptyArea = Offset(lastInReview.x, WindowHeight - 100f)
        onRoot().performTouchInput { longPress(card) }
        waitForIdle()
        onRoot().performTouchInput {
            glideTo(card, emptyArea, steps = 40)
            up()
        }
        waitForIdle()

        val neighbour = topLeftOf("Redesign settings page")
        val dropped = topLeftOf("Set up CI pipeline")
        assertTrue(abs(dropped.x - neighbour.x) < 1f && dropped.y > neighbour.y, "The card must end last in Review")
    }

    @Test
    fun kanban_cardHeldUnderItsOwnColumn_goesLastInIt() = runDesktopComposeUiTest(width = WideWidth, height = WindowHeight) {
        setContent { AppTheme { KanbanBoardScreen(onBack = {}) } }
        waitForIdle()

        val card = centerOf("Search with filters")
        val emptyArea = Offset(card.x, WindowHeight - 100f)
        onRoot().performTouchInput { longPress(card) }
        waitForIdle()
        onRoot().performTouchInput {
            glideTo(card, emptyArea, steps = 40)
            up()
        }
        waitForIdle()

        assertTrue(
            topLeftOf("Search with filters").y > topLeftOf("Fix image loader leak").y,
            "The card must end after the last card of its column",
        )
    }

    private companion object {
        const val WideWidth = 1400
        const val WindowHeight = 900
    }
}
