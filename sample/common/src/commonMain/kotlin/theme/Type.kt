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

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import sample.resources.Res
import sample.resources.instrument_sans_bold
import sample.resources.instrument_sans_medium
import sample.resources.instrument_sans_regular
import sample.resources.instrument_sans_semibold
import sample.resources.jetbrains_mono_medium
import sample.resources.jetbrains_mono_regular

@Composable
private fun instrumentSans() = FontFamily(
    Font(Res.font.instrument_sans_regular, FontWeight.Normal),
    Font(Res.font.instrument_sans_medium, FontWeight.Medium),
    Font(Res.font.instrument_sans_semibold, FontWeight.SemiBold),
    Font(Res.font.instrument_sans_bold, FontWeight.Bold),
)

@Composable
internal fun jetBrainsMono() = FontFamily(
    Font(Res.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(Res.font.jetbrains_mono_medium, FontWeight.Medium),
)

@Composable
internal fun sampleTypography(): Typography {
    val sans = instrumentSans()

    fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
        fontFamily = sans,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = tracking.sp,
    )

    return Typography(
        displayLarge = style(40, 46, FontWeight.Bold, -1.0),
        displayMedium = style(36, 42, FontWeight.Bold, -0.8),
        displaySmall = style(34, 40, FontWeight.Bold, -0.6),
        headlineLarge = style(32, 38, FontWeight.Bold, -0.6),
        headlineMedium = style(26, 32, FontWeight.Bold, -0.4),
        headlineSmall = style(22, 28, FontWeight.SemiBold, -0.2),
        titleLarge = style(20, 26, FontWeight.SemiBold, -0.2),
        titleMedium = style(16, 22, FontWeight.SemiBold),
        titleSmall = style(14, 20, FontWeight.SemiBold),
        bodyLarge = style(15, 22, FontWeight.Normal),
        bodyMedium = style(14, 20, FontWeight.Normal),
        bodySmall = style(12, 16, FontWeight.Normal),
        labelLarge = style(14, 18, FontWeight.Medium),
        labelMedium = style(12, 16, FontWeight.Medium),
        labelSmall = style(11, 14, FontWeight.Medium),
    )
}

@Composable
internal fun sampleCodeStyle() = TextStyle(
    fontFamily = jetBrainsMono(),
    fontWeight = FontWeight.Normal,
    fontSize = 12.5.sp,
    lineHeight = 19.sp,
)
