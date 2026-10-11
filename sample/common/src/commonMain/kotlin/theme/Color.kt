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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * The sample's color tokens. Every screen reads colors from here, so the
 * whole app changes together between light and dark.
 */
@Immutable
data class SampleColors(
    /** Screen background. */
    val canvas: Color,
    /** Items and cards that sit on the canvas. */
    val surface: Color,
    /** Recessed areas: drop zones, placeholders, tracks. */
    val well: Color,
    /** 1dp borders and dividers. */
    val line: Color,
    val ink: Color,
    val inkMuted: Color,
    /** A drop target that accepts the dragged item, and selection. */
    val accent: Color,
    val onAccent: Color,
    /** A drop target that rejects the dragged item, and delete actions. */
    val danger: Color,
    val isDark: Boolean,
)

val LightSampleColors = SampleColors(
    canvas = Color(0xFFF2F3F5),
    surface = Color(0xFFFFFFFF),
    well = Color(0xFFE8EAEE),
    line = Color(0xFFDDE0E5),
    ink = Color(0xFF15171C),
    inkMuted = Color(0xFF666C77),
    accent = Color(0xFF2F5BFF),
    onAccent = Color(0xFFFFFFFF),
    danger = Color(0xFFE5484D),
    isDark = false,
)

val DarkSampleColors = SampleColors(
    canvas = Color(0xFF111317),
    surface = Color(0xFF1C1F25),
    well = Color(0xFF17191E),
    line = Color(0xFF2B2F37),
    ink = Color(0xFFEDEEF1),
    inkMuted = Color(0xFF9AA0AB),
    accent = Color(0xFF7C93FF),
    onAccent = Color(0xFF0E1330),
    danger = Color(0xFFFF6B6F),
    isDark = true,
)

/**
 * A content color pair: a soft [container] fill and a strong [onContainer]
 * for text and icons on it. Content (notes, photos, avatars, tags) only uses
 * these hues, so every screen shares one palette.
 */
@Immutable
data class Hue(
    val name: String,
    val container: Color,
    val onContainer: Color,
)

object Hues {
    val Butter = Hue("Butter", Color(0xFFFFE08A), Color(0xFF6B4B00))
    val Mint = Hue("Mint", Color(0xFFA8E6C1), Color(0xFF17613A))
    val Sky = Hue("Sky", Color(0xFFA9D2FF), Color(0xFF1A4F8C))
    val Rose = Hue("Rose", Color(0xFFFFB8CB), Color(0xFF8F2A4C))
    val Lilac = Hue("Lilac", Color(0xFFD2BFFF), Color(0xFF4E3399))
    val Peach = Hue("Peach", Color(0xFFFFC7A3), Color(0xFF8A4214))

    val all: List<Hue> = listOf(Butter, Mint, Sky, Rose, Lilac, Peach)

    /** A stable hue for any index, cycling through [all]. */
    fun at(index: Int): Hue = all[index.mod(all.size)]
}
