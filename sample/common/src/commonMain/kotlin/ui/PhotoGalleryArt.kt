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

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp

/** How far each layer leans from the hue's container towards its onContainer. */
private const val SkyTopTone = 0.30f
private const val SkyHorizonTone = 0.08f
private const val FarTone = 0.42f
private const val NearTone = 0.62f

private const val SunRadius = 0.11f
private const val SeaHorizon = 0.60f
private const val GlintAlpha = 0.75f

/** Glints on the water under the sun: (y, half width) as fractions of the photo. */
private val SeaGlints = listOf(0.66f to 0.12f, 0.72f to 0.08f, 0.78f to 0.045f)

/** The two land layers of a scene, far (lighter) and near (darker). */
private class SceneLayers(
    val far: Path,
    val near: Path
)

/**
 * Paints a photo as a quiet one-hue landscape: a sky that deepens upwards,
 * a pale sun, and two layers of land or sea. Every tone is a mix of the
 * photo's hue container and onContainer, so it fits either theme.
 */
internal fun Modifier.photoArt(photo: GalleryPhoto): Modifier =
    drawWithCache {
        val hue = photo.hue
        fun tone(amount: Float): Color = lerp(hue.container, hue.onContainer, amount)

        val sky = Brush.verticalGradient(listOf(tone(SkyTopTone), tone(SkyHorizonTone)))
        val layers = sceneLayers(photo.scene, size)
        val sun = Offset(photo.sun.x * size.width, photo.sun.y * size.height)
        val farColor = tone(FarTone)
        val nearColor = tone(NearTone)

        onDrawBehind {
            drawRect(sky)
            drawCircle(color = hue.container, radius = size.minDimension * SunRadius, center = sun)
            scale(scaleX = if (photo.mirrored) -1f else 1f, scaleY = 1f) {
                drawPath(layers.far, farColor)
                drawPath(layers.near, nearColor)
            }
            if (photo.scene == PhotoScene.Sea) drawGlints(sun.x, hue.container)
        }
    }

private fun DrawScope.drawGlints(sunX: Float, color: Color) {
    val stroke = size.height * 0.02f
    SeaGlints.forEach { (y, halfWidth) ->
        drawLine(
            color = color.copy(alpha = GlintAlpha),
            start = Offset(sunX - halfWidth * size.width, y * size.height),
            end = Offset(sunX + halfWidth * size.width, y * size.height),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}

private fun sceneLayers(scene: PhotoScene, size: Size): SceneLayers =
    when (scene) {
        PhotoScene.Hills -> hills(size)
        PhotoScene.Peaks -> peaks(size)
        PhotoScene.Sea -> sea(size)
    }

/** Starts a closed shape along the bottom edge, [outline] draws its top. */
private inline fun ground(size: Size, outline: Path.(w: Float, h: Float) -> Unit): Path =
    Path().apply {
        outline(size.width, size.height)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }

private fun hills(size: Size) =
    SceneLayers(
        far = ground(size) { w, h ->
            moveTo(0f, h * 0.64f)
            cubicTo(w * 0.22f, h * 0.50f, w * 0.46f, h * 0.54f, w * 0.64f, h * 0.60f)
            cubicTo(w * 0.80f, h * 0.65f, w * 0.92f, h * 0.56f, w, h * 0.54f)
        },
        near = ground(size) { w, h ->
            moveTo(0f, h * 0.82f)
            cubicTo(w * 0.28f, h * 0.68f, w * 0.56f, h * 0.72f, w, h * 0.86f)
        },
    )

private fun peaks(size: Size) =
    SceneLayers(
        far = ground(size) { w, h ->
            moveTo(0f, h * 0.68f)
            lineTo(w * 0.26f, h * 0.42f)
            lineTo(w * 0.44f, h * 0.58f)
            lineTo(w * 0.66f, h * 0.36f)
            lineTo(w, h * 0.64f)
        },
        near = ground(size) { w, h ->
            moveTo(0f, h * 0.88f)
            lineTo(w * 0.34f, h * 0.72f)
            lineTo(w * 0.60f, h * 0.84f)
            lineTo(w * 0.84f, h * 0.74f)
            lineTo(w, h * 0.80f)
        },
    )

private fun sea(size: Size) =
    SceneLayers(
        far = ground(size) { w, h ->
            moveTo(0f, h * SeaHorizon)
            lineTo(w, h * SeaHorizon)
        },
        near = ground(size) { w, h ->
            moveTo(0f, h * 0.86f)
            cubicTo(w * 0.30f, h * 0.80f, w * 0.62f, h * 0.90f, w, h * 0.84f)
        },
    )
