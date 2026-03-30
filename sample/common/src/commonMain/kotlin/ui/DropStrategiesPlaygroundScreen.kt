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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTooltipState
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
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
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
                        Text(
                            text = "Drop Strategies Playground",
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } }
                        ) {
                            Icon(
                                Icons.Rounded.Settings,
                                contentDescription = "Settings",
                            )
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
    } // default SurfacePercentage

    DragAndDropContainer(
        state = dndState,
        modifier = modifier
    ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Simple selector row
                StrategySelector(
                    selectedStrategy = selectedStrategy,
                    onSelectedStrategyChange = { selectedStrategy = it }
                )

                // HUD
                Text(
                    text = "Hovered: ${dndState.hoveredDropTargetKey} | Strategy: ${selectedStrategy::class.simpleName}",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                )

                // Draggable item area
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(24.dp),
                        )
                ) {
                    DraggableItem(
                        state = dndState,
                        key = "draggable",
                        data = 42,
                        dropTargets = listOf("left", "right"),
                        dropStrategy = selectedStrategy,
                        modifier = Modifier.size(140.dp)
                    ) {
                        RedBox(
                            modifier = Modifier
                                .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                .fillMaxSize()
                        )
                    }
                }

                // Drop targets row (close to each other, different sizes)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    DropBox(
                        key = "left",
                        title = "Left",
                        dndState = dndState,
                        modifier = Modifier.width(140.dp)
                    )
                    Box(modifier = Modifier.width(8.dp))
                    DropBox(
                        key = "right",
                        title = "Right",
                        dndState = dndState,
                        modifier = Modifier.width(220.dp)
                    )
                }
            }
        }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun StrategySelector(
    selectedStrategy: DropStrategy,
    onSelectedStrategyChange: (DropStrategy) -> Unit,
) {
    val strategies = remember {
        listOf(
            DropStrategy.Surface,
            DropStrategy.SurfacePercentage,
            DropStrategy.CenterDistance
        )
    }

    fun getDescriptions(strategy: DropStrategy): String =
        when (strategy) {
            DropStrategy.Surface ->
                "Chooses the target with the largest absolute overlap area (in pixels)."

            DropStrategy.SurfacePercentage ->
                "Chooses the target with the largest overlap relative to that target's size (percentage)."

            DropStrategy.CenterDistance ->
                "Chooses the target whose center is closest to the dragged item's center."

            else ->
                ""
        }

    val tooltipState = rememberTooltipState()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            strategies.forEach { strategy ->
                val selected = strategy == selectedStrategy

                TooltipBox(
                    state = tooltipState,
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(
                                text = getDescriptions(strategy),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                shape = RoundedCornerShape(50)
                            )
                            .background(
                                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
                            )
                            .clip(RoundedCornerShape(50))
                            .clickable { onSelectedStrategyChange(strategy) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = strategy::class.simpleName.orEmpty(),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DropBox(
    key: Any,
    title: String,
    dndState: com.mohamedrejeb.compose.dnd.DragAndDropState<Int>,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(180.dp)
            .border(
                width = 1.dp,
                color = with(MaterialTheme.colorScheme) {
                    if (dndState.hoveredDropTargetKey == key) primary else onSurface
                },
                shape = RoundedCornerShape(24.dp),
            )
            .dropTarget(
                key = key,
                state = dndState,
            )
    ) {
        if (dndState.hoveredDropTargetKey == key) {
            Text(
                text = "Hovering",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
            )
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
