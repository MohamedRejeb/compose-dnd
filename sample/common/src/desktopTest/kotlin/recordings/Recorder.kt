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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import java.io.File
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import theme.AppTheme

/**
 * Records a sample screen to an mp4 while a script drives it, one frame at a time
 * on the test clock. The clips of the README and the docs come from here:
 *
 * ```
 * ./gradlew :sample:common:desktopTest --tests "recordings.*" -PrecordDir=/some/dir
 * ```
 *
 * Needs `ffmpeg` on the path. Without `-PrecordDir` nothing is recorded.
 */
@OptIn(ExperimentalTestApi::class)
object Recorder {
    private val outDir = System.getProperty("recordDir")
    private val only = System.getProperty("recordOnly")?.split(",")

    /** Pixels per dp: clips stay sharp on high density screens. */
    const val Scale = 2

    /**
     * @param width Width of the screen in dp.
     * @param height Height of the screen in dp.
     */
    fun record(
        name: String,
        width: Int = 400,
        height: Int = 760,
        dark: Boolean = true,
        content: @Composable () -> Unit,
        script: Clip.() -> Unit,
    ) {
        if (outDir == null) return
        if (only != null && name !in only) return
        File(outDir).mkdirs()

        val pointer = PointerState()
        val widthPx = width * Scale
        val heightPx = height * Scale
        runDesktopComposeUiTest(width = widthPx, height = heightPx) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(Scale.toFloat())) {
                    AppTheme(initialDarkTheme = dark) {
                        Box(Modifier.fillMaxSize()) {
                            content()
                            PointerOverlay(pointer, dark)
                        }
                    }
                }
            }
            waitForIdle()
            mainClock.advanceTimeBy(1500)
            mainClock.autoAdvance = false

            val encoder = Encoder(File(outDir, "$name.mp4"), widthPx, heightPx)
            try {
                Clip(this, pointer, encoder, Offset(widthPx.toFloat(), heightPx.toFloat())).apply {
                    pause(400)
                    script()
                    pause(900)
                }
            } finally {
                encoder.close()
            }
        }
    }
}

internal class PointerState {
    var position by mutableStateOf(Offset.Zero)
    var pressed by mutableStateOf(false)
}

/** A dot where the finger is, so the viewer sees what drives the screen. */
@Composable
private fun PointerOverlay(pointer: PointerState, dark: Boolean) {
    val alpha by animateFloatAsState(if (pointer.pressed) 1f else 0f)
    val dot = if (dark) Color.White else Color.Black
    val outline = if (dark) Color.Black else Color.White
    Canvas(Modifier.fillMaxSize()) {
        if (alpha == 0f) return@Canvas
        val radius = (20 - 6 * alpha).dp.toPx()
        drawCircle(dot.copy(alpha = 0.28f * alpha), radius, pointer.position)
        drawCircle(outline.copy(alpha = 0.35f * alpha), radius + 1.dp.toPx(), pointer.position, style = Stroke(1.dp.toPx()))
        drawCircle(dot.copy(alpha = 0.9f * alpha), radius, pointer.position, style = Stroke(1.5.dp.toPx()))
    }
}

/** Pipes raw frames to ffmpeg. */
internal class Encoder(
    file: File,
    private val width: Int,
    private val height: Int
) {
    private val process = ProcessBuilder(
        "ffmpeg", "-y", "-loglevel", "error",
        "-f", "rawvideo", "-pix_fmt", "bgra", "-s", "${width}x$height", "-r", "30", "-i", "-",
        "-c:v", "libx264", "-crf", "20", "-preset", "slow", "-pix_fmt", "yuv420p", "-movflags", "+faststart",
        file.absolutePath,
    ).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start()
    private val input: OutputStream = process.outputStream.buffered(1 shl 20)
    private val pixels = IntArray(width * height)
    private val bytes = ByteBuffer.allocate(width * height * 4).order(ByteOrder.LITTLE_ENDIAN)

    fun write(image: androidx.compose.ui.graphics.ImageBitmap) {
        check(image.width == width && image.height == height) { "Unexpected frame ${image.width}x${image.height}" }
        image.readPixels(pixels)
        bytes.clear()
        bytes.asIntBuffer().put(pixels)
        input.write(bytes.array())
    }

    fun close() {
        input.close()
        check(process.waitFor() == 0) { "ffmpeg failed" }
    }
}

/** The script of one clip: a finger and a clock. Positions are in px. */
@OptIn(ExperimentalTestApi::class)
class Clip internal constructor(
    private val test: ComposeUiTest,
    private val pointer: PointerState,
    private val encoder: Encoder,
    /** Size of the screen in px. */
    val size: Offset,
) {
    private var frames = 0

    /** An offset given in dp, as px. */
    fun dp(x: Number, y: Number): Offset = Offset(x.toFloat(), y.toFloat()) * Recorder.Scale.toFloat()

    fun centerOf(text: String, index: Int = 0): Offset =
        test
            .onAllNodesWithText(text)[index]
            .fetchSemanticsNode()
            .boundsInRoot.center

    fun topLeftOf(text: String): Offset =
        test
            .onAllNodesWithText(text)[0]
            .fetchSemanticsNode()
            .boundsInRoot.topLeft

    fun centerOfDescription(description: String): Offset =
        test
            .onAllNodesWithContentDescription(description)[0]
            .fetchSemanticsNode()
            .boundsInRoot.center

    /** Lets [ms] pass with the finger where it is. */
    fun pause(ms: Int) {
        repeat(ms / FrameMs) {
            test.mainClock.advanceTimeBy(FrameMs.toLong())
            frame()
        }
    }

    fun press(at: Offset) {
        pointer.position = at
        pointer.pressed = true
        test.onRoot().performTouchInput { down(at) }
        pause(120)
    }

    /** Presses and holds until the long press fires. */
    fun longPress(at: Offset) {
        press(at)
        pause(LongPressMs)
    }

    /** Glides to [to], easing in and out. */
    fun moveTo(to: Offset, ms: Int = 700) {
        val from = pointer.position
        val steps = (ms / FrameMs).coerceAtLeast(1)
        for (step in 1..steps) {
            val t = step / steps.toFloat()
            val eased = t * t * (3 - 2 * t)
            val position = from + (to - from) * eased
            pointer.position = position
            // Sending the event moves the clock one frame ahead
            test.onRoot().performTouchInput { moveTo(position, delayMillis = FrameMs.toLong()) }
            frame()
        }
    }

    fun moveBy(delta: Offset, ms: Int = 700) = moveTo(pointer.position + delta, ms)

    fun release() {
        test.onRoot().performTouchInput { up() }
        pointer.pressed = false
        pause(100)
    }

    fun tap(at: Offset) {
        press(at)
        release()
    }

    private fun frame() {
        // The clock ticks at 60 fps, the clip keeps every other frame
        if (frames++ % 2 == 0) encoder.write(test.onRoot().captureToImage())
    }

    private companion object {
        const val FrameMs = 16
        const val LongPressMs = 520
    }
}
