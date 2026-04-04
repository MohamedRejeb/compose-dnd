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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.DemoScreenScaffold

@Composable
fun ConditionalDropScreen(
    onBack: () -> Unit,
) {
    DemoScreenScaffold(
        title = "Conditional Drop",
        onBack = onBack,
    ) { paddingValues ->
        ConditionalDropContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        )
    }
}

@Composable
private fun ConditionalDropContent(
    modifier: Modifier = Modifier,
) {
    val state = rememberDragAndDropState<String>()

    var evenTargetCount by remember { mutableStateOf(0) }
    var oddTargetCount by remember { mutableStateOf(0) }

    val evenFull = evenTargetCount >= 2
    val oddFull = oddTargetCount >= 2

    DragAndDropContainer(
        state = state,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(
                text = "Drag numbers to matching targets. Each target accepts max 2 items.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                listOf("1", "2", "3", "4", "5", "6").forEach { number ->
                    val isEven = number.toInt() % 2 == 0

                    DraggableItem(
                        state = state,
                        key = number,
                        data = number,
                    ) {
                        NumberChip(
                            number = number,
                            isEven = isEven,
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0.3f else 1f
                                },
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                DropZone(
                    label = "Even numbers",
                    count = evenTargetCount,
                    isFull = evenFull,
                    isHovered = state.hoveredDropTargetKey == "even",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .dropTarget(
                            key = "even",
                            state = state,
                            canDrop = !evenFull,
                            onDrop = { droppedItem ->
                                val num = droppedItem.data.toIntOrNull() ?: return@dropTarget
                                if (num % 2 == 0) {
                                    evenTargetCount++
                                }
                            },
                        ),
                )

                DropZone(
                    label = "Odd numbers",
                    count = oddTargetCount,
                    isFull = oddFull,
                    isHovered = state.hoveredDropTargetKey == "odd",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .dropTarget(
                            key = "odd",
                            state = state,
                            canDrop = !oddFull,
                            onDrop = { droppedItem ->
                                val num = droppedItem.data.toIntOrNull() ?: return@dropTarget
                                if (num % 2 != 0) {
                                    oddTargetCount++
                                }
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun NumberChip(
    number: String,
    isEven: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (isEven) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    val contentColor = if (isEven) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onTertiary
    }
    val shape = MaterialTheme.shapes.medium

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .background(color),
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
        )
    }
}

@Composable
private fun DropZone(
    label: String,
    count: Int,
    isFull: Boolean,
    isHovered: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val borderColor = when {
        isFull -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        isHovered -> color
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .border(
                width = if (isHovered) 2.dp else 1.dp,
                color = borderColor,
                shape = shape,
            ).clip(shape)
            .background(
                if (isHovered && !isFull) {
                    color.copy(alpha = 0.08f)
                } else {
                    Color.Transparent
                }
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (isFull) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Text(
                text = "$count / 2",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isFull) {
                Text(
                    text = "Full",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
