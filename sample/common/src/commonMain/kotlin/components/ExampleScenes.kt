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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.ExampleBrandColors

/**
 * Mini drag-and-drop scenes used as covers on the real example cards.
 * Each one freezes a moment mid-drag, drawn with plain shapes.
 */

@Composable
fun KanbanMiniScene(
    modifier: Modifier = Modifier,
) {
    SceneSurface(
        brandColor = ExampleBrandColors.Kanban,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            MiniColumn(cardCount = 3)
            MiniColumn(cardCount = 2)
            MiniColumn(cardCount = 2)
        }

        // A card caught mid-drag between the second and third column
        MiniCard(
            width = 48.dp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 38.dp, y = (-6).dp)
                .graphicsLayer { rotationZ = -6f }
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(4.dp)),
        )
    }
}

@Composable
fun FormBuilderMiniScene(
    modifier: Modifier = Modifier,
) {
    SceneSurface(
        brandColor = ExampleBrandColors.FormBuilder,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The palette: a stack of field chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniCard(width = 34.dp, color = Color.White.copy(alpha = 0.75f))
                MiniCard(width = 34.dp, color = Color.White.copy(alpha = 0.75f))
                MiniCard(width = 34.dp, color = Color.White.copy(alpha = 0.75f))
            }

            // The form: rows with a dashed insertion slot
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniFormRow()
                MiniInsertionSlot()
                MiniFormRow()
            }
        }

        // A palette chip caught mid-flight toward the insertion slot
        MiniCard(
            width = 34.dp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-6).dp, y = (-14).dp)
                .graphicsLayer { rotationZ = 6f }
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(4.dp)),
        )
    }
}

@Composable
private fun SceneSurface(
    brandColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(
                brush = Brush.linearGradient(
                    listOf(brandColor, brandColor.copy(alpha = 0.78f)),
                ),
            ),
        content = content,
    )
}

@Composable
private fun MiniColumn(
    cardCount: Int,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(5.dp),
    ) {
        repeat(cardCount) {
            MiniCard(width = 40.dp, color = Color.White.copy(alpha = 0.75f))
        }
    }
}

@Composable
private fun MiniCard(
    width: Dp,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = width, height = 12.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color),
    )
}

@Composable
private fun MiniFormRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.22f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.7f)),
        )
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.White.copy(alpha = 0.6f)),
        )
    }
}

@Composable
private fun MiniInsertionSlot() {
    Box(
        modifier = Modifier
            .width(86.dp)
            .height(18.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.9f),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 7f)),
                    ),
                )
            },
    )
}
