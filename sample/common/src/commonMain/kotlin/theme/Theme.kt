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
package theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Immutable
data class SampleShapes(
    /** Drop zones, columns, boards. */
    val well: Shape = RoundedCornerShape(20.dp),
    /** Draggable items. */
    val item: Shape = RoundedCornerShape(14.dp),
    val small: Shape = RoundedCornerShape(10.dp),
    val pill: Shape = CircleShape,
)

private val LocalSampleColors = staticCompositionLocalOf { LightSampleColors }
private val LocalSampleShapes = staticCompositionLocalOf { SampleShapes() }
private val LocalCodeStyle = staticCompositionLocalOf { TextStyle.Default }

/** Entry point to the sample's design tokens. */
object SampleTheme {
    val colors: SampleColors
        @Composable @ReadOnlyComposable
        get() = LocalSampleColors.current

    val shapes: SampleShapes
        @Composable @ReadOnlyComposable
        get() = LocalSampleShapes.current

    /** Monospace style for code snippets and API names. */
    val code: TextStyle
        @Composable @ReadOnlyComposable
        get() = LocalCodeStyle.current
}

/** Maps the tokens onto Material so the few Material components we use (Switch, sheets) match. */
private fun SampleColors.toColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accent.copy(alpha = 0.14f),
        onPrimaryContainer = ink,
        secondary = inkMuted,
        onSecondary = surface,
        background = canvas,
        onBackground = ink,
        surface = surface,
        onSurface = ink,
        surfaceVariant = well,
        onSurfaceVariant = inkMuted,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        surfaceContainer = surface,
        surfaceContainerHigh = surface,
        surfaceContainerHighest = well,
        outline = line,
        outlineVariant = line,
        error = danger,
        onError = onAccent,
    )
}

@Composable
fun AppTheme(
    initialDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    var darkTheme by rememberSaveable(initialDarkTheme) { mutableStateOf(initialDarkTheme) }

    val controller = remember(darkTheme) {
        ThemeController(
            darkTheme = darkTheme,
            toggle = { darkTheme = !darkTheme },
        )
    }

    val colors = if (darkTheme) DarkSampleColors else LightSampleColors

    CompositionLocalProvider(
        LocalThemeController provides controller,
        LocalSampleColors provides colors,
        LocalSampleShapes provides SampleShapes(),
        LocalCodeStyle provides sampleCodeStyle(),
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            shapes = Shapes(
                small = RoundedCornerShape(10.dp),
                medium = RoundedCornerShape(14.dp),
                large = RoundedCornerShape(20.dp),
                extraLarge = RoundedCornerShape(28.dp),
            ),
            typography = sampleTypography(),
            content = content,
        )
    }
}
