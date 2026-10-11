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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.Avatar
import components.ItemSurface
import components.SectionLabel
import theme.Hue
import theme.SampleTheme

internal val AxisTokenHeight = 40.dp
private val RailLineWidth = 2.dp

/** A section title with the `dragAxis` value it demonstrates on the right. */
@Composable
internal fun AxisSectionHeader(
    title: String,
    api: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 12.dp),
    ) {
        SectionLabel(text = title, modifier = Modifier.weight(1f))
        Text(
            text = api,
            style = SampleTheme.code,
            color = SampleTheme.colors.inkMuted,
        )
    }
}

/** A draggable pill in a content hue: a speed, a task. */
@Composable
internal fun AxisHueToken(
    label: String,
    hue: Hue,
    modifier: Modifier = Modifier,
) {
    ItemSurface(
        color = hue.container,
        shape = SampleTheme.shapes.pill,
        bordered = false,
        contentAlignment = Alignment.Center,
        modifier = modifier.height(AxisTokenHeight),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = hue.onContainer,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

/** A person in the seating grid: their avatar and first name. */
@Composable
internal fun AxisPersonToken(
    name: String,
    hue: Hue,
    modifier: Modifier = Modifier,
) {
    ItemSurface(
        shape = SampleTheme.shapes.pill,
        contentAlignment = Alignment.Center,
        modifier = modifier.height(AxisTokenHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 6.dp, end = 14.dp),
        ) {
            Avatar(name = name, hue = hue, size = 28.dp)
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = SampleTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The muted name of an empty slot. */
@Composable
internal fun AxisSlotLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = SampleTheme.colors.inkMuted,
        maxLines = 1,
        modifier = modifier,
    )
}

/** A small dot that marks a stop on a rail. */
@Composable
internal fun AxisRailStop(modifier: Modifier = Modifier) {
    val color = SampleTheme.colors.line
    Spacer(
        modifier = modifier
            .size(8.dp)
            .drawBehind { drawCircle(color) },
    )
}

/** Draws the rail's guide line through the middle, horizontal or vertical. */
@Composable
internal fun Modifier.railLine(horizontal: Boolean): Modifier {
    val color = SampleTheme.colors.line
    return drawBehind {
        val stroke = RailLineWidth.toPx()
        if (horizontal) {
            drawLine(color, Offset(0f, center.y), Offset(size.width, center.y), stroke)
        } else {
            drawLine(color, Offset(center.x, 0f), Offset(center.x, size.height), stroke)
        }
    }
}

/** Keeps a fixed-width label column in the vertical ladder. */
internal val AxisLadderLabelWidth = 88.dp

@Composable
internal fun AxisLadderLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = SampleTheme.colors.inkMuted,
        modifier = Modifier
            .width(AxisLadderLabelWidth)
            .padding(start = 16.dp),
    )
}

internal const val AxisLockCode = """
// The speed knob only slides sideways
Modifier.draggableItem(
    key = "speed",
    data = index,
    state = state,
    dragAxis = DragAxis.Horizontal,
) { AxisHueToken(speed, Hues.Sky) }

// Each stop on the rail takes the drop
Modifier.dropTarget(
    key = stops[i],
    state = state,
    onDrop = { selected = i },
)
"""
