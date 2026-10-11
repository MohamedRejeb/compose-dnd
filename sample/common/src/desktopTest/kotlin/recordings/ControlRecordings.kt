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
import ui.AutoScrollDemoScreen
import ui.AutoScrollLayout
import ui.ConditionalDropScreen
import ui.DropStrategiesPlaygroundScreen

/** Clips of auto scroll and of the screens that decide where a drop goes. */
class ControlRecordings {

    @Test
    fun autoScroll() = record(
        name = "auto-scroll",
        content = { AutoScrollDemoScreen(onBack = {}, initialLayout = AutoScrollLayout.Masonry) },
    ) {
        // Held at the bottom edge, the grid scrolls; then back up
        press(centerOf("Kyoto") - dp(0, 60))
        moveTo(Offset(size.x * 0.6f, size.y - dp(0, 40).y), 900)
        pause(2200)
        moveTo(Offset(size.x * 0.4f, dp(0, 190).y), 800)
        pause(1500)
        moveBy(dp(0, 200), 500)
        pause(300)
        release()
        pause(600)
        tap(centerOf("Column"))
        pause(500)
        press(centerOf("Review the login flow"))
        moveTo(Offset(size.x * 0.5f, size.y - dp(0, 30).y), 900)
        pause(1800)
        moveBy(dp(0, -160), 500)
        pause(300)
        release()
    }

    @Test
    fun conditionalDrop() = record("conditional-drop", content = { ConditionalDropScreen {} }) {
        val photos = centerOf("Photos") + dp(20, 50)
        val documents = centerOf("Documents") + dp(0, 50)
        // A document is refused by Photos, then taken by Documents
        press(centerOf("invoice.pdf"))
        moveTo(photos, 900)
        pause(900)
        moveTo(documents, 700)
        pause(500)
        release()
        pause(600)
        press(centerOf("beach.jpg"))
        moveTo(photos, 900)
        pause(500)
        release()
        pause(500)
        press(centerOf("skyline.png"))
        moveTo(documents, 900)
        pause(800)
        release()
    }

    @Test
    fun dropStrategies() = record("drop-strategies", content = { DropStrategiesPlaygroundScreen {} }) {
        listOf("Surface", "Percentage", "Center").forEachIndexed { index, strategy ->
            if (index > 0) {
                tap(centerOf(strategy))
                pause(500)
            }
            // The photo straddles Medium and Large, each strategy picks its own winner
            val photo = Offset(size.x / 2, centerOf("Targets of different sizes").y + dp(0, 62).y)
            press(photo)
            moveBy(dp(25, 122), 800)
            pause(1100)
            moveTo(photo, 600)
            release()
            pause(400)
        }
    }
}
