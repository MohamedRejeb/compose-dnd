/*
 * Copyright 2025, Mohamed Ben Rejeb and the Compose Dnd project contributors
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.DndSettingsDrawer
import components.RedBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropStrategiesPlaygroundScreen(
    onBack: () -> Unit,
) {
    var dragAfterLongPress by remember { mutableStateOf(false) }
    var requireFirstDownUnconsumed by remember { mutableStateOf(false) }
    val drawerState = androidx.compose.material3.rememberDrawerState(
        initialValue = androidx.compose.material3.DrawerValue.Closed
    )
    val scope = rememberCoroutineScope()

    DndSettingsDrawer(
        drawerState = drawerState,
        dragAfterLongPress = dragAfterLongPress,
        onDragAfterLongPressChange = { dragAfterLongPress = it },
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        onRequireFirstDownUnconsumedChange = { requireFirstDownUnconsumed = it },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Drop Strategies")
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                        }
                    },
                )
            },
        ) { paddingValues ->
            DropStrategiesPlaygroundContent(
                dragAfterLongPress = dragAfterLongPress,
                requireFirstDownUnconsumed = requireFirstDownUnconsumed,
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(paddingValues)
                    .padding(20.dp)
            )
        }
    }
}

@Composable
private fun DropStrategiesPlaygroundContent(
    dragAfterLongPress: Boolean,
    requireFirstDownUnconsumed: Boolean,
    modifier: Modifier = Modifier,
) {
    val dndState = rememberDragAndDropState<Int>(
        dragAfterLongPress = dragAfterLongPress,
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
    )

    var selectedStrategy by remember {
        mutableStateOf<DropStrategy>(DropStrategy.SurfacePercentage)
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {
        StrategySelector(
            selectedStrategy = selectedStrategy,
            onSelectedStrategyChange = { selectedStrategy = it },
        )

        StrategyDescription(selectedStrategy)

        // HUD
        val hoveredKey = dndState.hoveredDropTargetKey
        Text(
            text = if (hoveredKey != "") "Hovered: $hoveredKey" else "Hovered: none",
            color = if (hoveredKey != "") {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
            style = MaterialTheme.typography.labelMedium,
        )

        Text(
            text = "Overlapping targets — drag the box across them to see which one gets selected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OverlappingTargetsDemo(dndState = dndState, selectedStrategy = selectedStrategy)

        Text(
            text = "Different sizes — the small target covers faster by percentage",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )

        DifferentSizesDemo(dndState = dndState, selectedStrategy = selectedStrategy)
    }
}

// --- Demo 1: Three overlapping targets ---

@Composable
private fun OverlappingTargetsDemo(
    dndState: DragAndDropState<Int>,
    selectedStrategy: DropStrategy,
) {
    DragAndDropContainer(
        state = dndState,
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Draggable item
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp),
                    )
            ) {
                DraggableItem(
                    state = dndState,
                    key = "draggable-1",
                    data = 1,
                    dropTargets = listOf("a", "b", "c"),
                    dropStrategy = selectedStrategy,
                    modifier = Modifier.size(80.dp),
                ) {
                    RedBox(
                        modifier = Modifier
                            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                            .fillMaxSize()
                    )
                }
            }

            // Three targets placed close together with overlap via negative spacing
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val totalWidth = maxWidth
                // Targets overlap: each one is offset so they share horizontal space
                val targetWidth = totalWidth * 0.45f

                TargetBox(
                    key = "a",
                    label = "A",
                    dndState = dndState,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .width(targetWidth)
                        .height(150.dp)
                        .align(Alignment.CenterStart)
                )

                TargetBox(
                    key = "b",
                    label = "B",
                    dndState = dndState,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .width(targetWidth)
                        .height(120.dp)
                        .align(Alignment.Center)
                )

                TargetBox(
                    key = "c",
                    label = "C",
                    dndState = dndState,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .width(targetWidth)
                        .height(150.dp)
                        .align(Alignment.CenterEnd)
                )
            }
        }
    }
}

// --- Demo 2: Different sized targets side by side ---

@Composable
private fun DifferentSizesDemo(
    dndState: DragAndDropState<Int>,
    selectedStrategy: DropStrategy,
) {
    DragAndDropContainer(
        state = dndState,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Draggable item
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp),
                    )
            ) {
                DraggableItem(
                    state = dndState,
                    key = "draggable-2",
                    data = 2,
                    dropTargets = listOf("small", "medium", "large"),
                    dropStrategy = selectedStrategy,
                    modifier = Modifier.size(60.dp),
                ) {
                    RedBox(
                        modifier = Modifier
                            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                            .fillMaxSize()
                    )
                }
            }

            // Three targets: small, medium, large — adjacent
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                TargetBox(
                    key = "small",
                    label = "Small",
                    dndState = dndState,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .width(80.dp)
                        .height(80.dp)
                )

                TargetBox(
                    key = "medium",
                    label = "Medium",
                    dndState = dndState,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .width(120.dp)
                        .height(120.dp)
                )

                TargetBox(
                    key = "large",
                    label = "Large",
                    dndState = dndState,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                )
            }
        }
    }
}

// --- Shared components ---

@Composable
private fun TargetBox(
    key: Any,
    label: String,
    dndState: DragAndDropState<Int>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val isHovered = dndState.hoveredDropTargetKey == key

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isHovered) 3.dp else 1.dp,
                color = if (isHovered) color else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                if (isHovered) color.copy(alpha = 0.12f) else Color.Transparent
            )
            .dropTarget(
                key = key,
                state = dndState,
            )
    ) {
        Text(
            text = label,
            style = if (isHovered) {
                MaterialTheme.typography.titleMedium
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = if (isHovered) color else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StrategySelector(
    selectedStrategy: DropStrategy,
    onSelectedStrategyChange: (DropStrategy) -> Unit,
) {
    val strategies = remember {
        listOf(
            DropStrategy.Surface,
            DropStrategy.SurfacePercentage,
            DropStrategy.CenterDistance,
        )
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        strategies.forEach { strategy ->
            val selected = strategy == selectedStrategy
            val name = when (strategy) {
                DropStrategy.Surface -> "Surface"
                DropStrategy.SurfacePercentage -> "Surface %"
                DropStrategy.CenterDistance -> "Center Dist."
                else -> ""
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = RoundedCornerShape(50),
                    )
                    .background(
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        } else {
                            Color.Transparent
                        },
                    )
                    .clickable { onSelectedStrategyChange(strategy) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = name,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}

@Composable
private fun StrategyDescription(strategy: DropStrategy) {
    val description = when (strategy) {
        DropStrategy.Surface ->
            "Picks the target with the most overlap area (pixels). Favors larger targets."

        DropStrategy.SurfacePercentage ->
            "Picks the target with the highest overlap relative to its own size. Favors smaller targets."

        DropStrategy.CenterDistance ->
            "Picks the target whose center is closest to the dragged item. Size doesn't matter."

        else -> ""
    }

    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
