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
package snapshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import snapshots.Snapshots.HeldDrag
import snapshots.Snapshots.shot
import theme.AppTheme
import ui.PhotoGalleryScreen
import ui.StickyNotesBoardScreen

/** Photo gallery and sticky notes, at rest and mid-drag. */
@OptIn(ExperimentalTestApi::class)
class AppsBSnapshots {

    @Test
    fun screens() {
        shot("photos") { PhotoGalleryScreen {} }
        gestureShot("photos_select", gesture = { holdDrag(photo(5), photo(11), longPress = true) }) {
            PhotoGalleryScreen {}
        }
        gestureShot(
            name = "photos_drag",
            gesture = {
                listOf(5, 6, 9).forEach { onNodeWithContentDescription("Photo $it").performClick() }
                waitForIdle()
                holdDrag(photo(6), textCenter("Travel") + Offset(0f, -36f), longPress = true)
            },
        ) { PhotoGalleryScreen {} }

        shot("notes") { StickyNotesBoardScreen {} }
        shot("notes_drag", drag = HeldDrag("Record demo", Offset(150f, 40f))) { StickyNotesBoardScreen {} }
        gestureShot(
            name = "notes_trash",
            gesture = {
                val trash = onNodeWithContentDescription("Trash").fetchSemanticsNode().boundsInRoot.center
                holdDrag(textCenter("Plan beta") + Offset(20f, 40f), trash)
            },
        ) { StickyNotesBoardScreen {} }
    }

    private fun ComposeUiTest.photo(number: Int): Offset =
        onNodeWithContentDescription("Photo $number").fetchSemanticsNode().boundsInRoot.center

    private fun ComposeUiTest.textCenter(text: String): Offset =
        onNodeWithText(text).fetchSemanticsNode().boundsInRoot.center

    /** Presses at [from], moves to [to] and keeps the pointer down. */
    private fun ComposeUiTest.holdDrag(from: Offset, to: Offset, longPress: Boolean = false) {
        onRoot().performTouchInput {
            down(from)
            if (longPress) advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
            val steps = 16
            repeat(steps) {
                advanceEventTime(16)
                moveBy((to - from) / steps.toFloat())
            }
        }
    }

    /** Like [shot], for gestures that [HeldDrag] cannot express. */
    private fun gestureShot(
        name: String,
        gesture: ComposeUiTest.() -> Unit,
        content: @Composable () -> Unit,
    ) {
        val outDir = System.getProperty("snapshotDir") ?: return
        val only = System.getProperty("snapshotOnly")?.split(",")
        if (only != null && name.substringBefore("_") !in only && name !in only) return
        listOf(true to name, false to "${name}_light").forEach { (dark, fileName) ->
            runDesktopComposeUiTest(width = 420, height = 880) {
                setContent { AppTheme(initialDarkTheme = dark) { content() } }
                waitForIdle()
                mainClock.advanceTimeBy(1500)
                gesture()
                mainClock.advanceTimeBy(800)
                val image = onRoot().captureToImage().toAwtImage()
                File(outDir).mkdirs()
                ImageIO.write(image, "png", File(outDir, "$fileName.png"))
            }
        }
    }
}
