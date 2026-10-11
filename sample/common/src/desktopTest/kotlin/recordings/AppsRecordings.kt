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

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import recordings.Recorder.record
import ui.FormBuilderScreen
import ui.KanbanBoardScreen
import ui.PhotoGalleryScreen
import ui.PinnedChatsScreen
import ui.StickyNotesBoardScreen

/** Clips of the example apps. */
class AppsRecordings {

    @Test
    fun kanban() = record("kanban", width = 1120, height = 660, content = { KanbanBoardScreen {} }) { moveCardsAndAColumn() }

    @Test
    fun kanbanLight() = record(
        name = "kanban-light",
        width = 1120,
        height = 660,
        dark = false,
        content = { KanbanBoardScreen {} },
    ) { moveCardsAndAColumn() }

    private fun Clip.moveCardsAndAColumn() {
        longPress(centerOf("Set up CI pipeline"))
        moveTo(centerOf("Update onboarding flow"), 1100)
        pause(500)
        release()
        pause(600)
        longPress(centerOf("Fix image loader leak"))
        moveTo(centerOf("Redesign settings page") + dp(0, 90), 1100)
        pause(500)
        release()
        pause(600)
        // Columns move by their header
        press(centerOf("Done"))
        moveTo(centerOf("In progress") + dp(20, 10), 1200)
        pause(500)
        release()
    }

    @Test
    fun kanbanPhone() = record("kanban-phone", content = { KanbanBoardScreen {} }) {
        // Held at the edge, the board scrolls to the next columns
        longPress(centerOf("Design the login screen"))
        moveTo(Offset(size.x - dp(24, 0).x, size.y * 0.42f), 900)
        pause(1300)
        moveBy(dp(-150, 0), 500)
        pause(500)
        release()
        pause(700)
        // A column dragged by its header scrolls the board back
        val header = centerOf("Done")
        press(header)
        moveTo(Offset(dp(50, 0).x, header.y + dp(0, 10).y), 900)
        pause(1500)
        release()
    }

    @Test
    fun photoGallery() = record("multi-select", content = { PhotoGalleryScreen {} }) {
        // Long press, then sweep across photos to select them
        longPress(centerOfDescription("Photo 1"))
        moveTo(centerOfDescription("Photo 4"), 700)
        moveTo(centerOfDescription("Photo 7"), 700)
        pause(300)
        release()
        pause(600)
        // The whole selection drags as one
        longPress(centerOfDescription("Photo 6"))
        moveTo(centerOf("Travel") - dp(0, 20), 1100)
        pause(500)
        release()
    }

    @Test
    fun stickyNotes() = record("free-form", content = { StickyNotesBoardScreen {} }) {
        press(topLeftOf("Record demo") + dp(30, 40))
        moveBy(dp(110, 170), 1000)
        pause(300)
        release()
        pause(600)
        press(topLeftOf("Ship 1.0 alpha") + dp(30, 40))
        moveBy(dp(20, 260), 900)
        pause(300)
        release()
        pause(600)
        press(topLeftOf("Write docs") + dp(30, 40))
        moveTo(centerOfDescription("Trash"), 1100)
        pause(500)
        release()
    }

    @Test
    fun formBuilder() = record("form-builder", content = { FormBuilderScreen {} }) {
        press(centerOf("Email"))
        moveTo(centerOf("Drag a field here"), 900)
        pause(300)
        release()
        pause(500)
        // Dropped on a field, the new one lands above it
        press(centerOf("Short text"))
        moveTo(centerOf("Work email"), 900)
        pause(600)
        release()
        pause(500)
        press(centerOf("Checkbox"))
        moveTo(centerOf("Drop here to add at the end"), 900)
        pause(300)
        release()
        pause(600)
        // Fields reorder by their handle
        press(centerOf("Full name") + dp(-51, 25))
        moveBy(dp(0, 130), 1000)
        pause(400)
        release()
    }

    @Test
    fun pinnedChats() = record("pinned-chats", content = { PinnedChatsScreen {} }) {
        longPress(centerOf("Alex"))
        moveTo(centerOf("Design team"), 1100)
        pause(400)
        release()
        pause(700)
        // Chats that are not pinned keep their place
        longPress(centerOf("Mom"))
        moveTo(centerOf("Jordan"), 1100)
        pause(600)
        release()
    }
}
