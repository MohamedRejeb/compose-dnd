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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import components.DropWell
import components.ItemSurface
import components.SectionLabel
import components.WellState
import theme.Hue
import theme.Hues
import theme.SampleTheme

internal enum class StrategyOption(
    val label: String,
    val apiName: String,
    val strategy: DropStrategy,
    val explanation: String,
) {
    Surface(
        label = "Surface",
        apiName = "DropStrategy.Surface",
        strategy = DropStrategy.Surface,
        explanation = "The target that covers the most of the photo wins. This is the default.",
    ),
    Percentage(
        label = "Percentage",
        apiName = "DropStrategy.SurfacePercentage",
        strategy = DropStrategy.SurfacePercentage,
        explanation = "The target with the largest share of its own area under the photo wins.",
    ),
    Center(
        label = "Center",
        apiName = "DropStrategy.CenterDistance",
        strategy = DropStrategy.CenterDistance,
        explanation = "The target whose center is closest to the photo's center wins, whatever its size.",
    ),
}

/** A drop target placed by fractions of the setup's width, so it scales with the screen. */
@Immutable
internal data class TargetSpec(
    val name: String,
    val startFraction: Float,
    val widthFraction: Float,
    val height: Dp,
    val labelAlignment: Alignment = Alignment.TopStart,
)

/** One comparison: a photo in a tray and a row of targets to drop it on. */
@Immutable
internal data class StrategySetup(
    val title: String,
    val photoKey: String,
    val photoHue: Hue,
    val targetsHeight: Dp,
    val targets: List<TargetSpec>,
)

internal val OverlappingSetup = StrategySetup(
    title = "Overlapping targets",
    photoKey = "beach",
    photoHue = Hues.Sky,
    targetsHeight = 150.dp,
    targets = listOf(
        TargetSpec("Photos", startFraction = 0f, widthFraction = 0.45f, height = 150.dp),
        TargetSpec(
            name = "Archive",
            startFraction = 0.55f,
            widthFraction = 0.45f,
            height = 150.dp,
            labelAlignment = Alignment.TopEnd,
        ),
        // Listed last so it draws on top of the two it overlaps.
        TargetSpec("Shared", startFraction = 0.275f, widthFraction = 0.45f, height = 110.dp),
    ),
)

internal val SizesSetup = StrategySetup(
    title = "Targets of different sizes",
    photoKey = "forest",
    photoHue = Hues.Mint,
    targetsHeight = 160.dp,
    targets = listOf(
        TargetSpec("Small", startFraction = 0f, widthFraction = 0.2f, height = 72.dp),
        TargetSpec("Medium", startFraction = 0.23f, widthFraction = 0.3f, height = 112.dp),
        TargetSpec("Large", startFraction = 0.56f, widthFraction = 0.44f, height = 160.dp),
    ),
)

private val PhotoSize = 60.dp

@Composable
internal fun PhotoToken(
    hue: Hue,
    modifier: Modifier = Modifier,
) {
    ItemSurface(
        color = hue.container,
        contentAlignment = Alignment.Center,
        modifier = modifier.size(PhotoSize),
    ) {
        Icon(
            imageVector = Icons.Rounded.Landscape,
            contentDescription = "Photo",
            tint = hue.onContainer,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
internal fun TargetWell(
    name: String,
    state: WellState,
    modifier: Modifier = Modifier,
    labelAlignment: Alignment = Alignment.TopStart,
) {
    val colors = SampleTheme.colors
    val shape = SampleTheme.shapes.well
    // Overlapping wells need an outline at rest to read as separate shapes.
    val outline = if (state == WellState.Idle) Modifier.border(1.dp, colors.line, shape) else Modifier
    DropWell(
        state = state,
        shape = shape,
        contentAlignment = labelAlignment,
        modifier = modifier.then(outline),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = if (state == WellState.Accept) colors.accent else colors.inkMuted,
            modifier = Modifier.padding(10.dp),
        )
    }
}

/** The setup title with the target that would take the photo right now. */
@Composable
internal fun SetupHeader(
    title: String,
    hovered: String?,
    droppedIn: String?,
) {
    val colors = SampleTheme.colors
    val (status, color) = when {
        hovered != null -> "Over $hovered" to colors.accent
        droppedIn != null -> "In $droppedIn" to colors.inkMuted
        else -> null to colors.inkMuted
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
    ) {
        SectionLabel(text = title)
        Spacer(Modifier.weight(1f))
        if (status != null) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelLarge,
                color = color,
            )
        }
    }
}

/** A recessed tray the photo starts in and can be dropped back into. */
@Composable
internal fun PhotoTray(
    state: WellState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    DropWell(
        state = state,
        shape = SampleTheme.shapes.item,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
        ) {
            content()
        }
    }
}
