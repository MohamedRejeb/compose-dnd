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

import androidx.compose.ui.graphics.Color

/**
 * Accent colors for home screen sections and feature cards.
 * Kept outside the Material color scheme so they read the same in light and dark.
 */
object SampleAccents {
    val Sky = Color(0xFF0EA5E9)
    val Indigo = Color(0xFF6366F1)
    val Violet = Color(0xFF8B5CF6)
    val Magenta = Color(0xFFEC4899)
    val Amber = Color(0xFFF59E0B)
    val Emerald = Color(0xFF10B981)
    val Teal = Color(0xFF14B8A6)
}

/**
 * Brand-style colors for the real example showcase cards.
 */
object ExampleBrandColors {
    val Kanban = Color(0xFF0079BF)
    val FormBuilder = Color(0xFF5E6AD2)
}
