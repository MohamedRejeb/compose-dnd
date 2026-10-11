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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import components.lifted
import navigation.Demo
import theme.Hue
import theme.Hues
import theme.SampleTheme

/**
 * A still illustration of an example screen for its home tile, drawn with the
 * same pieces the screen uses: flat items, recessed wells and one lifted item.
 */
@Composable
internal fun BoxScope.ExamplePreview(demo: Demo) {
    when (demo) {
        Demo.Kanban -> KanbanPreview()
        Demo.Photos -> PhotosPreview()
        Demo.StickyNotes -> StickyNotesPreview()
        Demo.FormBuilder -> FormBuilderPreview()
        Demo.PinnedChats -> PinnedChatsPreview()
        else -> Unit
    }
}

private val MiniShape = RoundedCornerShape(6.dp)

/** A tiny flat card: a hue tag and two text lines. */
@Composable
private fun MiniCard(
    hue: Hue,
    modifier: Modifier = Modifier,
    width: Dp = 60.dp,
) {
    val colors = SampleTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .width(width)
            .clip(MiniShape)
            .background(colors.surface)
            .border(1.dp, colors.line, MiniShape)
            .padding(6.dp),
    ) {
        Box(Modifier.size(width = 18.dp, height = 4.dp).clip(CircleShape).background(hue.container))
        TextLine(fraction = 0.9f)
        TextLine(fraction = 0.6f)
    }
}

@Composable
private fun TextLine(fraction: Float, color: Color = SampleTheme.colors.line) {
    Box(
        Modifier
            .fillMaxWidth(fraction)
            .height(4.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun BoxScope.KanbanPreview() {
    val colors = SampleTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.align(Alignment.Center),
    ) {
        listOf(
            listOf(Hues.Sky, Hues.Butter, Hues.Rose),
            listOf(Hues.Mint),
            listOf(Hues.Lilac, Hues.Peach),
        ).forEach { cards ->
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .width(76.dp)
                    .height(104.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.canvas)
                    .padding(8.dp),
            ) {
                cards.forEach { MiniCard(hue = it, width = 60.dp) }
            }
        }
    }
    MiniCard(
        hue = Hues.Rose,
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = (-30).dp, y = 22.dp)
            .lifted(shape = MiniShape, tilt = 6f, animate = false),
    )
}

@Composable
private fun BoxScope.PhotosPreview() {
    val colors = SampleTheme.colors
    val tile = 22.dp
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.align(Alignment.Center),
    ) {
        repeat(3) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { col ->
                    val index = row * 5 + col
                    val selected = index in 6..8
                    Box(
                        Modifier
                            .size(tile)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Hues.at(index).container)
                            .then(
                                if (selected) {
                                    Modifier.border(2.dp, colors.accent, RoundedCornerShape(4.dp))
                                } else {
                                    Modifier
                                },
                            ),
                    )
                }
            }
        }
    }
    Box(
        Modifier
            .align(Alignment.Center)
            .offset(x = 50.dp, y = 30.dp)
            .size(tile + 4.dp)
            .lifted(shape = RoundedCornerShape(5.dp), tilt = -8f, animate = false)
            .clip(RoundedCornerShape(5.dp))
            .background(Hues.at(6).container)
            .border(2.dp, colors.accent, RoundedCornerShape(5.dp)),
    )
}

@Composable
private fun BoxScope.StickyNotesPreview() {
    val dot = SampleTheme.colors.line
    Box(
        Modifier
            .matchParentSize()
            .drawBehind {
                val step = 12.dp.toPx()
                var y = step / 2
                while (y < size.height) {
                    var x = step / 2
                    while (x < size.width) {
                        drawCircle(dot, radius = 1.2.dp.toPx(), center = Offset(x, y))
                        x += step
                    }
                    y += step
                }
            },
    )
    Note(Hues.Butter, Modifier.align(Alignment.Center).offset(x = (-38).dp, y = (-10).dp).rotate(-5f))
    Note(Hues.Mint, Modifier.align(Alignment.Center).offset(x = 8.dp, y = 14.dp).rotate(3f))
    Note(
        Hues.Rose,
        Modifier
            .align(Alignment.Center)
            .offset(x = 46.dp, y = (-14).dp)
            .lifted(shape = RoundedCornerShape(4.dp), tilt = 8f, animate = false),
    )
}

@Composable
private fun Note(hue: Hue, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(hue.container)
            .padding(6.dp),
    ) {
        TextLine(0.8f, hue.onContainer.copy(alpha = 0.35f))
        TextLine(0.5f, hue.onContainer.copy(alpha = 0.35f))
    }
}

@Composable
private fun BoxScope.FormBuilderPreview() {
    val colors = SampleTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.align(Alignment.Center),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf(Hues.Sky, Hues.Mint, Hues.Peach).forEach { hue -> PaletteChip(hue) }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .width(70.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surface)
                .padding(7.dp),
        ) {
            repeat(2) { FieldLine() }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = colors.accent,
                            cornerRadius = androidx.compose.ui.geometry
                                .CornerRadius(3.dp.toPx()),
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 2.dp.toPx())),
                            ),
                        )
                    },
            )
        }
    }
    PaletteChip(
        Hues.Peach,
        Modifier
            .align(Alignment.Center)
            .offset(x = 4.dp, y = 30.dp)
            .lifted(shape = CircleShape, tilt = -4f, animate = false),
    )
}

@Composable
private fun PaletteChip(hue: Hue, modifier: Modifier = Modifier) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .width(40.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .padding(horizontal = 5.dp, vertical = 5.dp),
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(hue.onContainer))
        TextLine(1f)
    }
}

@Composable
private fun FieldLine() {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        TextLine(0.4f)
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(SampleTheme.colors.well),
        )
    }
}

@Composable
private fun BoxScope.PinnedChatsPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .align(Alignment.Center)
            .width(116.dp),
    ) {
        ChatLine(Hues.Sky, pinned = true)
        ChatLine(Hues.Butter, pinned = true)
        ChatLine(Hues.Mint, pinned = false)
    }
    ChatLine(
        Hues.Rose,
        pinned = false,
        modifier = Modifier
            .align(Alignment.Center)
            .offset(x = 12.dp, y = 36.dp)
            .width(116.dp)
            .lifted(shape = MiniShape, tilt = -3f, animate = false),
    )
}

@Composable
private fun ChatLine(hue: Hue, pinned: Boolean, modifier: Modifier = Modifier) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(MiniShape)
            .background(colors.surface)
            .padding(5.dp),
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(hue.container))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
            TextLine(0.7f)
            TextLine(0.45f)
        }
        if (pinned) {
            Icon(
                Icons.Rounded.PushPin,
                contentDescription = null,
                tint = colors.inkMuted,
                modifier = Modifier.size(9.dp),
            )
        }
    }
}
