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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DragAxis
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AxisLockedDragScreen(
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Axis-Locked Drag",
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
            )
        },
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalSlotDemo()
            VerticalSlotDemo()
            FreeMovementDemo()
        }
    }
}

// --- Horizontal: Slide item between horizontal slots ---

@Composable
private fun HorizontalSlotDemo() {
    val slots = remember { listOf("h-slot-0", "h-slot-1", "h-slot-2", "h-slot-3") }
    var itemSlot by remember { mutableStateOf(0) }
    val state = rememberDragAndDropState<Int>()

    Text(
        text = "Horizontal Only — slide between slots",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )

    DragAndDropContainer(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            slots.forEachIndexed { index, slotKey ->
                val isOccupied = index == itemSlot
                val isHovered = state.hoveredDropTargetKey == slotKey

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = when {
                                isHovered -> MaterialTheme.colorScheme.primary
                                isOccupied -> Color.Transparent
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(16.dp),
                        )
                        .dropTarget(
                            key = slotKey,
                            state = state,
                            onDrop = { itemSlot = index },
                        )
                ) {
                    if (isOccupied) {
                        DraggableItem(
                            state = state,
                            key = "h-item",
                            data = index,
                            dragAxis = DragAxis.Horizontal,
                        ) {
                            ColorChip(
                                label = "H",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = if (isDragging) 0.3f else 1f
                                    }
                            )
                        }
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    }
}

// --- Vertical: Slide item between vertical slots ---

@Composable
private fun VerticalSlotDemo() {
    val slots = remember { listOf("v-slot-0", "v-slot-1", "v-slot-2") }
    var itemSlot by remember { mutableStateOf(0) }
    val state = rememberDragAndDropState<Int>()

    Text(
        text = "Vertical Only — slide between slots",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )

    DragAndDropContainer(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            slots.forEachIndexed { index, slotKey ->
                val isOccupied = index == itemSlot
                val isHovered = state.hoveredDropTargetKey == slotKey

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = 2.dp,
                            color = when {
                                isHovered -> MaterialTheme.colorScheme.tertiary
                                isOccupied -> Color.Transparent
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(16.dp),
                        )
                        .dropTarget(
                            key = slotKey,
                            state = state,
                            onDrop = { itemSlot = index },
                        )
                ) {
                    if (isOccupied) {
                        DraggableItem(
                            state = state,
                            key = "v-item",
                            data = index,
                            dragAxis = DragAxis.Vertical,
                        ) {
                            ColorChip(
                                label = "V",
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = if (isDragging) 0.3f else 1f
                                    }
                            )
                        }
                    } else {
                        Text(
                            text = "Slot ${index + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    }
}

// --- Free: Move items freely between grid positions ---

@Composable
private fun FreeMovementDemo() {
    // 2x3 grid positions, two items can be placed
    val gridSlots = remember {
        listOf(
            "g-0-0", "g-0-1", "g-0-2",
            "g-1-0", "g-1-1", "g-1-2",
        )
    }
    var itemASlot by remember { mutableStateOf(0) } // top-left
    var itemBSlot by remember { mutableStateOf(5) } // bottom-right
    val state = rememberDragAndDropState<String>()

    Text(
        text = "Free (both axes) — move in a grid",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
    )

    DragAndDropContainer(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            for (row in 0..1) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (col in 0..2) {
                        val slotIndex = row * 3 + col
                        val slotKey = gridSlots[slotIndex]
                        val hasItemA = slotIndex == itemASlot
                        val hasItemB = slotIndex == itemBSlot
                        val isHovered = state.hoveredDropTargetKey == slotKey

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = 2.dp,
                                    color = when {
                                        isHovered -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                )
                                .dropTarget(
                                    key = slotKey,
                                    state = state,
                                    canDrop = !hasItemA && !hasItemB,
                                    onDrop = { droppedItem ->
                                        when (droppedItem.data) {
                                            "A" -> itemASlot = slotIndex
                                            "B" -> itemBSlot = slotIndex
                                        }
                                    },
                                )
                        ) {
                            when {
                                hasItemA -> {
                                    DraggableItem(
                                        state = state,
                                        key = "item-a",
                                        data = "A",
                                        dragAxis = DragAxis.Free,
                                    ) {
                                        ColorChip(
                                            label = "A",
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    alpha = if (isDragging) 0.3f else 1f
                                                }
                                        )
                                    }
                                }
                                hasItemB -> {
                                    DraggableItem(
                                        state = state,
                                        key = "item-b",
                                        data = "B",
                                        dragAxis = DragAxis.Free,
                                    ) {
                                        ColorChip(
                                            label = "B",
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier
                                                .graphicsLayer {
                                                    alpha = if (isDragging) 0.3f else 1f
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorChip(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(width = 56.dp, height = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
    }
}
