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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DndDropZone(
    label: String,
    modifier: Modifier = Modifier,
    isHovered: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit = {},
) {
    val shape = MaterialTheme.shapes.large
    val borderColor = if (isHovered) accentColor else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isHovered) accentColor.copy(alpha = 0.08f) else Color.Transparent

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .border(
                width = if (isHovered) 2.dp else 1.dp,
                color = borderColor,
                shape = shape,
            )
            .clip(shape)
            .background(bgColor),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isHovered) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}
