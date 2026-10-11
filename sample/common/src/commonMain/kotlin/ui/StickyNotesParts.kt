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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import components.lifted
import theme.Hue
import theme.Hues
import theme.SampleTheme

/** Notes snap to this grid; a note is five steps wide. */
internal val NoteGridStep = 24.dp
internal val NoteSize = NoteGridStep * 5

private const val NoteTilt = -3f

/** A note over the trash shrinks so the danger ring shows around it. */
private const val OverTrashScale = 0.5f
private const val TrashReadyScale = 1.15f
private const val TrashHoverScale = 1.45f
private val TrashSize = 56.dp

@Immutable
internal data class BoardNote(
    val id: Int,
    val title: String,
    val detail: String,
    val hue: Hue,
    /** Top-left on the board, in px. */
    val position: Offset,
)

private data class NoteText(
    val title: String,
    val detail: String
)

private val NoteTexts = listOf(
    NoteText("Ship 1.0 alpha", "Tag it and post the changelog"),
    NoteText("Write docs", "Drop targets and auto scroll"),
    NoteText("Record demo", "Twenty seconds, phone frame"),
    NoteText("Reply to issues", "Three open since Monday"),
    NoteText("Plan beta", "Collect feedback first"),
    NoteText("Design review", "Friday at 10"),
)

/** Where new notes appear, in grid steps, so the board starts out scattered. */
private val NoteSlots = listOf(1 to 1, 8 to 2, 2 to 7, 9 to 9, 1 to 14, 7 to 16)

internal fun newNote(id: Int, density: Density): BoardNote {
    val text = NoteTexts[id % NoteTexts.size]
    val (column, row) = NoteSlots[id % NoteSlots.size]
    return with(density) {
        BoardNote(
            id = id,
            title = text.title,
            detail = text.detail,
            hue = Hues.at(id),
            position = Offset(
                x = (NoteGridStep * column).toPx(),
                y = (NoteGridStep * row).toPx(),
            ),
        )
    }
}

internal fun initialNotes(density: Density): List<BoardNote> = List(5) { newNote(it, density) }

/** A note at rest: flat paper in its hue, no border, no shadow. */
@Composable
internal fun StickyNote(
    note: BoardNote,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .size(NoteSize)
            .clip(SampleTheme.shapes.small)
            .background(note.hue.container)
            .padding(12.dp),
    ) {
        Text(
            text = note.title,
            style = MaterialTheme.typography.titleSmall,
            color = note.hue.onContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = note.detail,
            style = MaterialTheme.typography.bodySmall,
            color = note.hue.onContainer.copy(alpha = 0.72f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The note being dragged: lifted with a tilt, and shrinking over the trash. */
@Composable
internal fun LiftedStickyNote(
    note: BoardNote,
    overTrash: Boolean,
) {
    val scale by animateFloatAsState(if (overTrash) OverTrashScale else 1f)
    StickyNote(
        note = note,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }.lifted(shape = SampleTheme.shapes.small, tilt = NoteTilt),
    )
}

/** The dashed accent outline of where the dragged note will land. */
@Composable
internal fun LandingPreview(modifier: Modifier = Modifier) {
    val accent = SampleTheme.colors.accent
    val shape = SampleTheme.shapes.small
    Box(
        modifier = modifier
            .size(NoteSize)
            .clip(shape)
            .background(accent.copy(alpha = 0.10f))
            .drawWithContent {
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = accent,
                    style = Stroke(
                        width = 3.dp.toPx(), // half is clipped by the shape
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 6.dp.toPx())),
                    ),
                )
            },
    )
}

/**
 * A round trash can. It stays quiet at rest, shows a danger outline while a
 * note is in flight, and fills with danger when a note is over it.
 */
@Composable
internal fun TrashTarget(
    isDragging: Boolean,
    isHovered: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    val shape = SampleTheme.shapes.pill
    val background by animateColorAsState(if (isHovered) colors.danger else colors.surface)
    val border by animateColorAsState(if (isDragging) colors.danger else colors.line)
    val tint by animateColorAsState(
        when {
            isHovered -> colors.onAccent
            isDragging -> colors.danger
            else -> colors.inkMuted
        },
    )
    val scale by animateFloatAsState(
        when {
            isHovered -> TrashHoverScale
            isDragging -> TrashReadyScale
            else -> 1f
        },
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }.size(TrashSize)
            .clip(shape)
            .background(background)
            .border(1.5.dp, border, shape),
    ) {
        Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = "Trash",
            tint = tint,
        )
    }
}

/** A dot at every grid point the notes snap to. */
@Composable
internal fun BoxScope.BoardDots(step: Dp) {
    val dot = SampleTheme.colors.line
    Box(
        Modifier
            .matchParentSize()
            .drawBehind {
                val stepPx = step.toPx()
                val radius = 1.5.dp.toPx()
                var y = stepPx
                while (y < size.height) {
                    var x = stepPx
                    while (x < size.width) {
                        drawCircle(dot, radius = radius, center = Offset(x, y))
                        x += stepPx
                    }
                    y += stepPx
                }
            },
    )
}
