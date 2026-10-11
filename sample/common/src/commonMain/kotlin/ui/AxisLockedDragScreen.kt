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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DragAxis
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.DemoScaffold
import components.DropWell
import components.WellState
import components.dragPlaceholder
import components.lifted
import components.wellState
import navigation.Demo
import theme.Hues
import theme.SampleTheme

private val Speeds = listOf("0.5x", "1x", "1.5x", "2x")
private const val SpeedKey = "speed"

private fun speedStopKey(index: Int) = "speed-$index"
private const val InitialSpeed = 1
private val SpeedTokenWidth = 64.dp
private val RailHeight = 56.dp

private val Priorities = listOf("Urgent", "This week", "Later")
private const val TaskKey = "task"
private const val TaskLabel = "Login crash"
private val LadderStepHeight = 60.dp

private const val DeskColumns = 3
private const val DeskRows = 2
private val DeskHeight = 72.dp
private val InitialSeats = mapOf("Maya" to 0, "Leo" to 5)
private val SeatHues = mapOf("Maya" to Hues.Rose, "Leo" to Hues.Sky)

private val SectionGap = 28.dp

@Composable
fun AxisLockedDragScreen(
    onBack: () -> Unit,
) {
    DemoScaffold(
        title = Demo.AxisLock.title,
        hint = "Drag each item. Some can only move one way.",
        code = AxisLockCode,
        onBack = onBack,
    ) { insets ->
        Column(
            verticalArrangement = Arrangement.spacedBy(SectionGap),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = insets.calculateBottomPadding() + 16.dp),
        ) {
            SpeedRail()
            PriorityLadder()
            SeatingGrid()
        }
    }
}

// -- Horizontal: a speed knob that slides along a rail --

@Composable
private fun SpeedRail() {
    val stops = remember { Speeds.indices.map(::speedStopKey) }
    var selected by remember { mutableIntStateOf(InitialSpeed) }
    val state = rememberDragAndDropState<Int>()
    val colors = SampleTheme.colors
    val pill = SampleTheme.shapes.pill

    Column {
        AxisSectionHeader(title = "Horizontal only", api = "DragAxis.Horizontal")
        DragAndDropContainer(state = state, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(RailHeight)
                    .clip(pill)
                    .background(colors.well)
                    .padding(horizontal = 24.dp)
                    .railLine(horizontal = true)
                    .padding(vertical = 4.dp),
            ) {
                stops.forEachIndexed { i, stop ->
                    DropWell(
                        state = wellState(state.draggedItem != null, state.hoveredDropTargetKey == stop),
                        shape = pill,
                        color = Color.Transparent,
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .dropTarget(key = stop, state = state, onDrop = { selected = i }),
                    ) {
                        if (i == selected) SpeedToken(index = i, state = state) else AxisRailStop()
                    }
                }
            }
        }
        SpeedScale(selected = selected)
    }
}

@Composable
private fun SpeedToken(
    index: Int,
    state: DragAndDropState<Int>,
) {
    val speed = Speeds[index]
    val pill = SampleTheme.shapes.pill
    AxisHueToken(
        label = speed,
        hue = Hues.Sky,
        modifier = Modifier
            .width(SpeedTokenWidth)
            .dragPlaceholder(active = state.isDragging(SpeedKey), shape = pill)
            .draggableItem(
                key = SpeedKey,
                data = index,
                state = state,
                dragAxis = DragAxis.Horizontal,
                draggableContent = {
                    // While it is dragged, the knob reads the speed of the stop it is over
                    val hovered = Speeds.indices.firstOrNull { state.hoveredDropTargetKey == speedStopKey(it) }
                    AxisHueToken(
                        label = Speeds[hovered ?: index],
                        hue = Hues.Sky,
                        modifier = Modifier.width(SpeedTokenWidth).lifted(shape = pill),
                    )
                },
            ),
    )
}

/** The speed names under the rail, lined up with its stops. */
@Composable
private fun SpeedScale(selected: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp),
    ) {
        Speeds.forEachIndexed { i, speed ->
            Text(
                text = speed,
                style = MaterialTheme.typography.labelMedium,
                color = if (i == selected) SampleTheme.colors.ink else SampleTheme.colors.inkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

// -- Vertical: a task that moves up and down a priority ladder --

@Composable
private fun PriorityLadder() {
    val steps = remember { Priorities.indices.map { "priority-$it" } }
    var selected by remember { mutableIntStateOf(0) }
    val state = rememberDragAndDropState<Int>()

    Column {
        AxisSectionHeader(title = "Vertical only", api = "DragAxis.Vertical")
        DragAndDropContainer(state = state, modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SampleTheme.shapes.well)
                    .background(SampleTheme.colors.well)
                    .padding(4.dp),
            ) {
                steps.forEachIndexed { i, step ->
                    DropWell(
                        state = wellState(state.draggedItem != null, state.hoveredDropTargetKey == step),
                        shape = SampleTheme.shapes.item,
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(LadderStepHeight)
                            .dropTarget(key = step, state = state, onDrop = { selected = i }),
                    ) {
                        LadderStep(label = Priorities[i]) {
                            if (i == selected) TaskToken(index = i, state = state) else AxisRailStop()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LadderStep(
    label: String,
    slot: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize(),
    ) {
        AxisLadderLabel(label)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .railLine(horizontal = false),
        ) {
            slot()
        }
        // Balances the label so the rail sits in the middle of the free space.
        Box(Modifier.width(AxisLadderLabelWidth / 2))
    }
}

@Composable
private fun TaskToken(
    index: Int,
    state: DragAndDropState<Int>,
) {
    val pill = SampleTheme.shapes.pill
    AxisHueToken(
        label = TaskLabel,
        hue = Hues.Rose,
        modifier = Modifier
            .dragPlaceholder(active = state.isDragging(TaskKey), shape = pill)
            .draggableItem(
                key = TaskKey,
                data = index,
                state = state,
                dragAxis = DragAxis.Vertical,
                draggableContent = { AxisHueToken(TaskLabel, Hues.Rose, Modifier.lifted(shape = pill)) },
            ),
    )
}

// -- Free: people who can move to any empty desk --

@Composable
private fun SeatingGrid() {
    var seats by remember { mutableStateOf(InitialSeats) }
    val state = rememberDragAndDropState<String>()

    Column {
        AxisSectionHeader(title = "Free", api = "DragAxis.Free")
        DragAndDropContainer(state = state, modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(DeskRows) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(DeskColumns) { column ->
                            val desk = row * DeskColumns + column
                            Desk(
                                desk = desk,
                                occupant = seats.entries.firstOrNull { it.value == desk }?.key,
                                state = state,
                                onSeat = { name -> seats = seats + (name to desk) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Desk(
    desk: Int,
    occupant: String?,
    state: DragAndDropState<String>,
    onSeat: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val key = "desk-$desk"
    val dragged = state.draggedItem?.data
    // The desk a person was picked up from shows their placeholder, not a rejection.
    val wellState = if (occupant != null && occupant == dragged) {
        WellState.Idle
    } else {
        wellState(dragged != null, state.hoveredDropTargetKey == key, canDrop = occupant == null)
    }

    DropWell(
        state = wellState,
        shape = SampleTheme.shapes.item,
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(DeskHeight)
            .dropTarget(
                key = key,
                state = state,
                canDrop = occupant == null,
                onDrop = { dropped -> onSeat(dropped.data) },
            ),
    ) {
        if (occupant == null) {
            AxisSlotLabel("Desk ${desk + 1}")
            return@DropWell
        }
        val hue = SeatHues.getValue(occupant)
        val pill = SampleTheme.shapes.pill
        AxisPersonToken(
            name = occupant,
            hue = hue,
            modifier = Modifier
                .dragPlaceholder(active = state.isDragging(occupant), shape = pill)
                .draggableItem(
                    key = occupant,
                    data = occupant,
                    state = state,
                    dragAxis = DragAxis.Free,
                    draggableContent = { AxisPersonToken(occupant, hue, Modifier.lifted(shape = pill, tilt = 3f)) },
                ),
        )
    }
}
