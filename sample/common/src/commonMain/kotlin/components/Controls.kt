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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.Hue
import theme.SampleTheme

/**
 * A row of mutually exclusive options on a recessed track. Used for every
 * "pick one mode" control in the sample instead of tabs or filter chips.
 */
@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    Row(
        modifier = modifier
            .clip(SampleTheme.shapes.pill)
            .background(colors.well)
            .padding(3.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val background by animateColorAsState(if (isSelected) colors.surface else Color.Transparent)
            val textColor by animateColorAsState(if (isSelected) colors.ink else colors.inkMuted)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(SampleTheme.shapes.pill)
                    .background(background)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelect(option) },
                    ).padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    text = label(option),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * A pill that turns a demo option on or off. Filled with accent when on.
 */
@Composable
fun OptionToggle(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    val background by animateColorAsState(if (checked) colors.accent else colors.surface)
    val content by animateColorAsState(if (checked) colors.onAccent else colors.ink)
    val border by animateColorAsState(if (checked) colors.accent else colors.line)
    Box(
        modifier = modifier
            .clip(SampleTheme.shapes.pill)
            .background(background)
            .border(1.dp, border, SampleTheme.shapes.pill)
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            ).padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
        )
    }
}

/** A small muted heading that names a group inside a screen. */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = SampleTheme.colors.ink,
        )
        if (trailing != null) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = SampleTheme.colors.inkMuted,
            )
        }
    }
}

/** A person's initial on a hue circle. */
@Composable
fun Avatar(
    name: String,
    hue: Hue,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(SampleTheme.shapes.pill)
            .background(hue.container),
    ) {
        Text(
            text = name.take(1).uppercase(),
            style = if (size >= 36.dp) MaterialTheme.typography.titleMedium else MaterialTheme.typography.labelMedium,
            color = hue.onContainer,
        )
    }
}

/** The round icon button used in top bars. */
@Composable
fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    val colors = SampleTheme.colors
    val background by animateColorAsState(if (active) colors.ink else colors.surface)
    val tint by animateColorAsState(if (active) colors.canvas else colors.ink)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .clip(SampleTheme.shapes.pill)
            .background(background)
            .border(1.dp, if (active) Color.Transparent else colors.line, SampleTheme.shapes.pill)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** A horizontal row of [OptionToggle]s or other small controls, wrapping is left to callers. */
@Composable
fun OptionsRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        content()
    }
}
