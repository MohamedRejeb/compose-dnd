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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplanemodeActive
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.FlashlightOn
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiTethering
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import components.GripDots
import components.ItemSurface
import theme.Hue
import theme.Hues
import theme.SampleTheme

private val SettingRowHeight = 64.dp
private val HandleWidth = 44.dp
private val SettingIconSize = 40.dp

@Immutable
internal data class QuickSetting(
    val label: String,
    val icon: ImageVector,
    val hue: Hue,
    val on: Boolean,
)

internal val InitialSettings = listOf(
    QuickSetting("Wi-Fi", Icons.Rounded.Wifi, Hues.Sky, on = true),
    QuickSetting("Bluetooth", Icons.Rounded.Bluetooth, Hues.Lilac, on = true),
    QuickSetting("Do not disturb", Icons.Rounded.DoNotDisturbOn, Hues.Rose, on = false),
    QuickSetting("Flashlight", Icons.Rounded.FlashlightOn, Hues.Butter, on = false),
    QuickSetting("Location", Icons.Rounded.LocationOn, Hues.Mint, on = true),
    QuickSetting("Dark mode", Icons.Rounded.DarkMode, Hues.Lilac, on = true),
    QuickSetting("Airplane mode", Icons.Rounded.AirplanemodeActive, Hues.Peach, on = false),
    QuickSetting("Hotspot", Icons.Rounded.WifiTethering, Hues.Sky, on = false),
)

/** Returns a new list where [moved] takes the slot of [target]. */
internal fun List<QuickSetting>.move(
    moved: QuickSetting,
    target: QuickSetting,
): List<QuickSetting> {
    val from = indexOfFirst { it.label == moved.label }
    val to = indexOfFirst { it.label == target.label }
    if (from == -1 || to == -1 || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

/** Returns a new list where the setting called [label] is flipped. */
internal fun List<QuickSetting>.toggle(label: String): List<QuickSetting> =
    map { if (it.label == label) it.copy(on = !it.on) else it }

/**
 * One quick setting. The whole row toggles the setting; only the grip on
 * the left, which receives [handleModifier], starts a drag.
 */
@Composable
internal fun SettingRow(
    setting: QuickSetting,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    ItemSurface(modifier = modifier.fillMaxWidth().height(SettingRowHeight)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = handleModifier
                    .semantics { contentDescription = "Reorder ${setting.label}" }
                    .width(HandleWidth)
                    .fillMaxHeight(),
            ) {
                GripDots(color = colors.inkMuted.copy(alpha = 0.6f))
            }
            SettingToggle(
                setting = setting,
                onToggle = onToggle,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun SettingToggle(
    setting: QuickSetting,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .toggleable(value = setting.on, role = Role.Switch, onValueChange = { onToggle() })
            .padding(end = 16.dp),
    ) {
        SettingIcon(setting)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = setting.label,
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
                maxLines = 1,
            )
            Text(
                text = if (setting.on) "On" else "Off",
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
            )
        }
        // The row handles the toggle, the switch only shows it.
        Switch(checked = setting.on, onCheckedChange = null)
    }
}

@Composable
private fun SettingIcon(setting: QuickSetting) {
    val colors = SampleTheme.colors
    val background by animateColorAsState(if (setting.on) setting.hue.container else colors.well)
    val tint by animateColorAsState(if (setting.on) setting.hue.onContainer else colors.inkMuted)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(SettingIconSize)
            .clip(SampleTheme.shapes.small)
            .background(background),
    ) {
        Icon(
            imageVector = setting.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}
