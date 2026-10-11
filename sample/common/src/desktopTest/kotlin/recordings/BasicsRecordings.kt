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
package recordings

import kotlin.test.Test
import recordings.Recorder.record
import ui.AxisLockedDragScreen
import ui.BetweenListsScreen
import ui.DragAndDropScreen
import ui.DragHandleReorderScreen
import ui.ReorderListScreen

/** Clips of the screens that show one API each. */
class BasicsRecordings {

    @Test
    fun dragAndDrop() = record("drag-and-drop", content = { DragAndDropScreen {} }) {
        press(centerOf("Invoice.pdf"))
        moveBy(dp(10, 250), 900)
        pause(300)
        release()
        pause(700)
        press(centerOf("Beach.jpg"))
        moveBy(dp(10, 250), 800)
        pause(300)
        release()
        pause(700)
        press(centerOf("Budget.xlsx"))
        moveBy(dp(-6, -170), 900)
        pause(300)
        release()
    }

    @Test
    fun reorder() = record("reorder", content = { ReorderListScreen {} }) {
        longPress(centerOf("Kotlin"))
        moveBy(dp(0, 300), 1300)
        pause(400)
        release()
        pause(600)
        longPress(centerOf("Go"))
        moveBy(dp(0, -370), 1300)
        pause(400)
        release()
    }

    @Test
    fun dragHandle() = record("drag-handle", content = { DragHandleReorderScreen {} }) {
        // The row body doesn't drag
        press(centerOf("Wi-Fi"))
        moveBy(dp(0, 130), 700)
        pause(300)
        release()
        pause(500)
        // Its grip does
        press(centerOfDescription("Reorder Wi-Fi"))
        moveBy(dp(0, 190), 1100)
        pause(400)
        release()
        pause(600)
        press(centerOfDescription("Reorder Do not disturb"))
        moveBy(dp(0, -130), 1000)
        pause(400)
        release()
    }

    @Test
    fun betweenLists() = record("between-lists", content = { BetweenListsScreen {} }) {
        press(centerOf("Maya Chen"))
        moveTo(centerOf("Tom Weber"), 900)
        pause(400)
        release()
        pause(700)
        tap(centerOf("Insert anywhere"))
        pause(500)
        press(centerOf("Sara Kim"))
        moveTo(centerOf("Ines Duarte"), 1000)
        pause(500)
        release()
        pause(600)
        press(centerOf("Leo Park"))
        moveTo(centerOf("Ravi Patel"), 1000)
        pause(500)
        release()
    }

    @Test
    fun axisLock() = record("axis-lock", content = { AxisLockedDragScreen {} }) {
        // The finger wanders, the item keeps to its axis
        press(centerOf("1x"))
        moveBy(dp(110, 60), 700)
        moveBy(dp(-230, -90), 900)
        moveBy(dp(180, 70), 800)
        pause(200)
        release()
        pause(500)
        press(centerOf("Login crash"))
        moveBy(dp(-90, 45), 700)
        moveBy(dp(120, 40), 700)
        pause(200)
        release()
        pause(500)
        press(centerOf("Maya"))
        moveBy(dp(90, 90), 600)
        moveTo(centerOf("Desk 5"), 600)
        pause(200)
        release()
    }
}
