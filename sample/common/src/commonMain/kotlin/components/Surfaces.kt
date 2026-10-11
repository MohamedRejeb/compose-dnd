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
package components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import theme.SampleTheme

/**
 * A flat item that sits on the canvas: a surface fill with a 1dp line border
 * and no shadow. Items only get a shadow when they are picked up, see [lifted].
 */
@Composable
fun ItemSurface(
    modifier: Modifier = Modifier,
    color: Color = SampleTheme.colors.surface,
    shape: Shape = SampleTheme.shapes.item,
    bordered: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val lineColor = SampleTheme.colors.line
    Box(
        contentAlignment = contentAlignment,
        modifier = modifier
            .clip(shape)
            .background(color)
            .then(if (bordered) Modifier.border(1.dp, lineColor, shape) else Modifier),
        content = content,
    )
}

/**
 * The picked-up look shared by every draggable in the sample: the item grows a
 * little, optionally tilts, and casts a deep shadow. Apply it to the root of a
 * `draggableContent` lambda.
 *
 * @param tilt rotation in degrees once lifted. Use it for physical objects
 * (notes, cards, photos); leave it at 0 for full-width rows.
 * @param animate whether to spring into the lifted look, or start there
 * (for static illustrations).
 */
@Composable
fun Modifier.lifted(
    shape: Shape = SampleTheme.shapes.item,
    tilt: Float = 0f,
    scale: Float = 1.04f,
    animate: Boolean = true,
): Modifier {
    val progress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium))
    }
    val isDark = SampleTheme.colors.isDark
    val shadowColor = if (isDark) Color.Black else Color(0xFF1B2440)
    return graphicsLayer {
        val p = progress.value
        val s = 1f + (scale - 1f) * p
        scaleX = s
        scaleY = s
        rotationZ = tilt * p
        shadowElevation = 22.dp.toPx() * p
        this.shape = shape
        clip = false
        ambientShadowColor = shadowColor
        spotShadowColor = shadowColor
    }.drawWithContent {
        drawContent()
        // Shadows barely show on a dark canvas, so a lifted item also brightens a little.
        if (isDark) {
            drawOutline(
                outline = shape.createOutline(size, layoutDirection, this),
                color = Color.White.copy(alpha = LiftedDarkHighlight * progress.value),
            )
        }
    }
}

private const val LiftedDarkHighlight = 0.06f

/**
 * Replaces an item with the hole it leaves behind while it is being dragged:
 * a recessed fill with a dashed outline in the item's [shape].
 */
@Composable
fun Modifier.dragPlaceholder(
    active: Boolean,
    shape: Shape = SampleTheme.shapes.item,
): Modifier {
    val fill = SampleTheme.colors.well
    val outline = SampleTheme.colors.line
    return drawWithContent {
        if (!active) {
            drawContent()
            return@drawWithContent
        }
        val shapeOutline = shape.createOutline(size, layoutDirection, this)
        drawOutline(shapeOutline, fill)
        val stroke = 1.5.dp.toPx()
        drawOutline(
            outline = shapeOutline,
            color = outline,
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())),
            ),
        )
    }
}

/** How a drop target reads right now. */
enum class WellState {
    /** Nothing is being dragged. */
    Idle,

    /** Something is being dragged and this target would take it. */
    Ready,

    /** The dragged item is over this target and will land here. */
    Accept,

    /** The dragged item is over this target but it is not allowed here. */
    Reject,
}

/**
 * Picks the [WellState] for a drop target.
 *
 * @param dragging whether any item is being dragged.
 * @param hovered whether the dragged item is over this target.
 * @param canDrop whether this target accepts the dragged item.
 */
fun wellState(
    dragging: Boolean,
    hovered: Boolean,
    canDrop: Boolean = true,
): WellState = when {
    hovered && canDrop -> WellState.Accept
    hovered -> WellState.Reject
    dragging && canDrop -> WellState.Ready
    else -> WellState.Idle
}

/**
 * A recessed area that items are dropped into. It stays quiet at rest, shows a
 * dashed accent outline while a compatible item is in flight, and fills with
 * accent (or danger) when the item is over it.
 */
@Composable
fun DropWell(
    modifier: Modifier = Modifier,
    state: WellState = WellState.Idle,
    shape: Shape = SampleTheme.shapes.well,
    color: Color = SampleTheme.colors.well,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit,
) {
    val colors = SampleTheme.colors
    val tint = when (state) {
        WellState.Accept -> colors.accent
        WellState.Reject -> colors.danger
        else -> Color.Transparent
    }
    val stroke = when (state) {
        WellState.Idle -> Color.Transparent
        WellState.Ready -> colors.accent.copy(alpha = 0.55f)
        WellState.Accept -> colors.accent
        WellState.Reject -> colors.danger
    }
    val animatedTint by animateColorAsState(tint.copy(alpha = if (tint == Color.Transparent) 0f else 0.10f))
    val animatedStroke by animateColorAsState(stroke)
    val dashed = state == WellState.Ready

    Box(
        contentAlignment = contentAlignment,
        modifier = modifier
            .clip(shape)
            .background(color)
            .background(animatedTint)
            .drawWithContent {
                drawContent()
                val width = 3.5.dp.toPx() // half is clipped by the shape
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = animatedStroke,
                    style = Stroke(
                        width = width,
                        pathEffect = if (dashed) {
                            PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 6.dp.toPx()))
                        } else {
                            null
                        },
                    ),
                )
            },
        content = content,
    )
}
