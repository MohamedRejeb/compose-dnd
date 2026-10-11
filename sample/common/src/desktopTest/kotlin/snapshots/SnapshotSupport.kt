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
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import theme.AppTheme

/**
 * Renders sample screens to PNG, in dark and light, for visual review.
 * Does nothing unless run with `-PsnapshotDir=<dir>`; `-PsnapshotOnly=home,kanban`
 * limits it to some shots (matched on the name, or the part before the first `_`).
 *
 * `./gradlew :sample:common:desktopTest --tests "snapshots.*" -PsnapshotDir=/tmp/shots`
 */
@OptIn(ExperimentalTestApi::class)
object Snapshots {
    private val outDir = System.getProperty("snapshotDir")
    private val only = System.getProperty("snapshotOnly")?.split(",")

    /**
     * A drag held mid-gesture while the screenshot is taken: press on the node
     * showing [fromText] (shifted by [touchOffset] px), optionally wait for a
     * long press, then move by [by] px without releasing.
     */
    data class HeldDrag(
        val fromText: String,
        val by: Offset,
        val longPress: Boolean = false,
        val touchOffset: Offset = Offset.Zero,
    )

    fun shot(
        name: String,
        w: Int = 420,
        h: Int = 880,
        drag: HeldDrag? = null,
        content: @Composable () -> Unit,
    ) {
        if (outDir == null) return
        if (only != null && name.substringBefore("_") !in only && name !in only) return
        shotIn(name, dark = true, w, h, drag, content)
        shotIn(name + "_light", dark = false, w, h, drag, content)
    }

    private fun shotIn(
        name: String,
        dark: Boolean,
        w: Int,
        h: Int,
        drag: HeldDrag?,
        content: @Composable () -> Unit,
    ) {
        runDesktopComposeUiTest(width = w, height = h) {
            setContent { AppTheme(initialDarkTheme = dark) { content() } }
            waitForIdle()
            mainClock.advanceTimeBy(1500)
            if (drag != null) hold(drag)
            val img = onRoot().captureToImage().toAwtImage()
            File(outDir).mkdirs()
            ImageIO.write(img, "png", File(outDir, "$name.png"))
        }
    }

    private fun ComposeUiTest.hold(drag: HeldDrag) {
        val start = onAllNodesWithText(drag.fromText)[0].fetchSemanticsNode().boundsInRoot.center + drag.touchOffset
        onRoot().performTouchInput {
            down(start)
            if (drag.longPress) {
                advanceEventTime(viewConfiguration.longPressTimeoutMillis + 100)
                moveBy(Offset(0f, 1f))
            }
        }
        waitForIdle()
        onRoot().performTouchInput {
            val steps = 12
            repeat(steps) {
                advanceEventTime(16)
                moveBy(drag.by / steps.toFloat())
            }
        }
        mainClock.advanceTimeBy(800)
    }
}
