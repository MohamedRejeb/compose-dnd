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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The slot a dragged item takes must follow where it is held, also when the items
 * have different heights and animate to their new place: no swap is undone or
 * repeated on the way, and the same position always gives the same order.
 */
@OptIn(ExperimentalTestApi::class)
class ReorderAccuracyTest {

    private data class Card(
        val id: String,
        val height: Int
    )

    private var lists by mutableStateOf(emptyList<List<Card>>())

    /** Every time the dragged card took the slot of another one: "dragged>target". */
    private val moves = mutableListOf<String>()

    /** Puts [dragged] in the slot of [target], in whichever list that is. */
    private fun move(dragged: Card, target: Card) {
        if (dragged.id == target.id) return
        moves += "${dragged.id}>${target.id}"
        lists = lists.map { cards ->
            val rest = cards.filter { it.id != dragged.id }
            val index = cards.indexOfFirst { it.id == target.id }
            if (index == -1) rest else rest.take(index.coerceAtMost(rest.size)) + dragged + rest.drop(index)
        }
    }

    @Composable
    private fun Board(strategy: DropStrategy) {
        val state = rememberDragAndDropState<Card>()
        DragAndDropContainer(state = state, modifier = Modifier.size(BoardWidth.dp, BoardHeight.dp).testTag(BoardTag)) {
            Row {
                lists.forEach { cards ->
                    LazyColumn(Modifier.width(ColumnWidth.dp).fillMaxHeight()) {
                        items(cards, key = { it.id }) { card ->
                            Box(
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .height(card.height.dp)
                                    .reorderableItem(
                                        key = card.id,
                                        data = card,
                                        state = state,
                                        dropStrategy = strategy,
                                        dragAfterLongPress = true,
                                        onDragEnter = { dragged -> move(dragged.data, card) },
                                        draggableContent = { Box(Modifier.size(ColumnWidth.dp, card.height.dp)) },
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }

    /** Long-presses at [from] and glides to [to], one small step per frame so every animation runs. */
    private fun ComposeUiTest.drag(from: Offset, to: Offset, stepPx: Float = 3f) {
        mainClock.autoAdvance = false
        onNodeWithTag(BoardTag).performTouchInput { down(from) }
        mainClock.advanceTimeBy(600)
        val steps = ((to - from).getDistance() / stepPx).toInt()
        repeat(steps) {
            onNodeWithTag(BoardTag).performTouchInput { moveBy((to - from) / steps.toFloat()) }
        }
        // Held still while the last items settle
        repeat(60) { mainClock.advanceTimeBy(FrameMs) }
    }

    private fun ComposeUiTest.release() {
        onNodeWithTag(BoardTag).performTouchInput { up() }
        mainClock.autoAdvance = true
        waitForIdle()
    }

    private fun order(list: Int): String = lists[list].joinToString("") { it.id }

    private fun runOnBoard(strategy: DropStrategy, block: ComposeUiTest.() -> Unit) = runComposeUiTest {
        setContent { Board(strategy) }
        waitForIdle()
        block()
    }

    private val strategies = listOf(DropStrategy.Surface, DropStrategy.SurfacePercentage, DropStrategy.CenterDistance)

    @Test
    fun mixedHeights_draggedDownTheList_takesEachSlotOnce() = strategies.forEach { strategy ->
        moves.clear()
        lists = listOf(listOf(Card("A", 80), Card("B", 240), Card("C", 60), Card("D", 160), Card("E", 80)))
        runOnBoard(strategy) {
            // A ends with its center over D's
            drag(from = Offset(100f, 40f), to = Offset(100f, 470f))
            release()
        }
        assertEquals(listOf("A>B", "A>C", "A>D"), moves, "$strategy")
        assertEquals("BCDAE", order(0), "$strategy")
    }

    @Test
    fun mixedHeights_draggedUpTheList_takesEachSlotOnce() = strategies.forEach { strategy ->
        moves.clear()
        lists = listOf(listOf(Card("A", 80), Card("B", 240), Card("C", 60), Card("D", 160), Card("E", 80)))
        runOnBoard(strategy) {
            // E ends with its center over B's
            drag(from = Offset(100f, 580f), to = Offset(100f, 200f))
            release()
        }
        assertEquals(listOf("E>D", "E>C", "E>B"), moves, "$strategy")
        assertEquals("AEBCD", order(0), "$strategy")
    }

    @Test
    fun tallItemOverShortOnes_heldAtTheSwapPoint_doesNotSwapBack() = strategies.forEach { strategy ->
        moves.clear()
        lists = listOf(listOf(Card("A", 240), Card("B", 60), Card("C", 60), Card("D", 60)))
        runOnBoard(strategy) {
            // Just far enough to take B's slot, then held there
            drag(from = Offset(100f, 120f), to = Offset(100f, 160f), stepPx = 1f)
            release()
        }
        assertEquals(listOf("A>B"), moves, "$strategy")
        assertEquals("BACD", order(0), "$strategy")
    }

    @Test
    fun cardCarriedOntoACardOfAnotherList_takesThatSlot() = strategies.forEach { strategy ->
        moves.clear()
        lists = listOf(
            listOf(Card("A", 130), Card("B", 110)),
            listOf(Card("X", 130), Card("Y", 110), Card("Z", 150)),
        )
        runOnBoard(strategy) {
            // A is grabbed by its middle and carried until it covers Y
            drag(from = Offset(100f, 65f), to = Offset(300f, 185f))
            release()
        }
        assertEquals("B", order(0), "$strategy")
        assertEquals("XAYZ", order(1), "$strategy, moves $moves")
        // On its way A may pass B and X, but it never goes back and forth
        assertEquals(moves.distinct().size, moves.toSet().size)
        assertTrue(moves.size <= 3, "A bounced between slots with $strategy: $moves")
    }

    @Test
    fun cardCarriedBelowTheLastCardOfAnotherList_endsLast() = strategies.forEach { strategy ->
        moves.clear()
        lists = listOf(
            listOf(Card("A", 150), Card("B", 110)),
            listOf(Card("X", 130), Card("Y", 110)),
        )
        runOnBoard(strategy) {
            drag(from = Offset(100f, 75f), to = Offset(300f, 320f))
            release()
        }
        assertEquals("XYA", order(1), "$strategy, moves $moves")
    }

    @Test
    fun cardCarriedSidewaysBelowTheMiddleOfTheLastCard_endsLast() = strategies.forEach { strategy ->
        moves.clear()
        lists = listOf(
            listOf(Card("P", 200), Card("A", 150)),
            listOf(Card("X", 130), Card("Y", 110)),
        )
        runOnBoard(strategy) {
            // A comes in level with Y, most of it below Y's middle
            drag(from = Offset(100f, 275f), to = Offset(300f, 275f))
            release()
        }
        assertEquals("P", order(0), "$strategy")
        assertEquals("XYA", order(1), "$strategy, moves $moves")
    }

    @Test
    fun cardMostlyOutOfItsListSideways_doesNotSwapInIt() = strategies.forEach { strategy ->
        moves.clear()
        lists = listOf(listOf(Card("A", 130), Card("B", 110), Card("C", 110)))
        runOnBoard(strategy) {
            // A is pulled more than half its width out of the list, then carried down past B
            mainClock.autoAdvance = false
            onNodeWithTag(BoardTag).performTouchInput { down(Offset(100f, 65f)) }
            mainClock.advanceTimeBy(600)
            repeat(40) { onNodeWithTag(BoardTag).performTouchInput { moveBy(Offset(3f, 0f)) } }
            repeat(50) { onNodeWithTag(BoardTag).performTouchInput { moveBy(Offset(0f, 3f)) } }
            repeat(30) { mainClock.advanceTimeBy(FrameMs) }
            release()
        }
        assertEquals(emptyList(), moves, "$strategy")
        assertEquals("ABC", order(0), "$strategy")
    }

    @Test
    fun cardSlightlyOffItsListSideways_stillSwapsInIt() = strategies.forEach { strategy ->
        moves.clear()
        lists = listOf(listOf(Card("A", 130), Card("B", 110), Card("C", 110)))
        runOnBoard(strategy) {
            // A third of its width out: A is still over the list
            drag(from = Offset(100f, 65f), to = Offset(165f, 215f))
            release()
        }
        assertEquals(listOf("A>B"), moves, "$strategy")
        assertEquals("BAC", order(0), "$strategy")
    }

    private companion object {
        const val BoardTag = "board"
        const val ColumnWidth = 200
        const val BoardWidth = 400
        const val BoardHeight = 700
        const val FrameMs = 16L
    }
}
