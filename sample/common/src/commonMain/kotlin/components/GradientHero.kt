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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import theme.SampleAccents

private val heroChips = listOf("Design", "Build", "Ship")

/**
 * Landing hero used at the top of the home screen.
 * A gradient board with a dot grid and a tiny live drag-and-drop playground:
 * grip-handle chips and a marching-ants drop slot, powered by the modifier API.
 */
@Composable
fun GradientHero(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        SampleAccents.Sky,
                        SampleAccents.Indigo,
                        SampleAccents.Violet,
                    ),
                ),
            ).drawBehind { drawDotGrid() }
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "COMPOSE DND",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Text(
                text = "Drag and drop\nfor every Compose target.",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Text(
                text = "Android · iOS · Desktop · Web. One modifier, every platform.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )

            Spacer(Modifier.size(4.dp))

            HeroPlayground()
        }
    }
}

/**
 * The pegboard texture behind the hero: a grid of faint dots, the surface
 * every drag interaction lives on.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDotGrid() {
    val step = 22.dp.toPx()
    val radius = 1.2.dp.toPx()
    val color = Color.White.copy(alpha = 0.12f)

    var y = step / 2
    while (y < size.height) {
        var x = step / 2
        while (x < size.width) {
            drawCircle(color = color, radius = radius, center = Offset(x, y))
            x += step
        }
        y += step
    }
}

@Composable
private fun HeroPlayground() {
    val dndState = rememberDragAndDropState<String>()
    var dropped by remember { mutableStateOf<String?>(null) }

    DragAndDropContainer(
        state = dndState,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.14f))
                .padding(12.dp),
        ) {
            Text(
                text = "Try it - grab a chip by its dots.",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                ChipTray(
                    dndState = dndState,
                    dropped = dropped,
                    onChipReturned = { dropped = null },
                    modifier = Modifier.weight(1f),
                )

                DropSlot(
                    dndState = dndState,
                    dropped = dropped,
                    onDropChip = { dropped = it },
                )
            }
        }
    }
}

@Composable
private fun ChipTray(
    dndState: DragAndDropState<String>,
    dropped: String?,
    onChipReturned: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .dropTarget(
                key = "hero-tray",
                state = dndState,
                dropAnimationEnabled = false,
                onDrop = { onChipReturned() },
            ).padding(vertical = 4.dp),
    ) {
        heroChips
            .filter { it != dropped }
            .forEach { label ->
                HeroChip(
                    label = label,
                    dndState = dndState,
                )
            }
    }
}

@Composable
private fun DropSlot(
    dndState: DragAndDropState<String>,
    dropped: String?,
    onDropChip: (String) -> Unit,
) {
    val isHovered = dndState.hoveredDropTargetKey == "hero-slot"
    val isDraggingAny = dndState.draggedItem != null

    // Marching ants around the slot while a chip is in the air
    val transition = rememberInfiniteTransition()
    val dashPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
    )

    val borderAlpha = when {
        isHovered -> 1f
        isDraggingAny -> 0.9f
        else -> 0.5f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isHovered) Color.White.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.10f),
            ).drawBehind {
                val stroke = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(12f, 12f),
                        phase = if (isDraggingAny) -dashPhase else 0f,
                    ),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = borderAlpha),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = stroke,
                )
            }.dropTarget(
                key = "hero-slot",
                state = dndState,
                dropAnimationEnabled = false,
                onDrop = { state -> onDropChip(state.data) },
            ).padding(horizontal = 12.dp),
    ) {
        if (dropped != null) {
            HeroChip(
                label = dropped,
                dndState = dndState,
            )
        } else {
            Text(
                text = "Drop here",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun HeroChip(
    label: String,
    dndState: DragAndDropState<String>,
) {
    val isDragging = dndState.isDragging(label)

    HeroChipContent(
        label = label,
        modifier = Modifier
            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
            .draggableItem(
                key = label,
                data = label,
                state = dndState,
                draggableContent = {
                    HeroChipContent(
                        label = label,
                        isDragShadow = true,
                    )
                },
            ),
    )
}

@Composable
private fun HeroChipContent(
    label: String,
    isDragShadow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .then(
                if (isDragShadow) {
                    Modifier
                        .graphicsLayer { rotationZ = -3f }
                        .shadow(elevation = 10.dp, shape = shape)
                } else {
                    Modifier
                }
            ).clip(shape)
            .background(Color.White.copy(alpha = if (isDragShadow) 0.4f else 0.22f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        GripDots(color = Color.White.copy(alpha = 0.7f))

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}
