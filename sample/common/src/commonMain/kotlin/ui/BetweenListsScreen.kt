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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScaffold
import components.DropWell
import components.SectionLabel
import components.SegmentedControl
import components.WellState
import components.dragPlaceholder
import components.lifted
import components.wellState
import navigation.Demo
import theme.SampleTheme

/** How a person lands in the other team. */
internal enum class BetweenListsMode(
    val label: String,
    val caption: String,
) {
    /** The team column is the only drop target: the person goes to the end. */
    Append("Append", "Drop on a team to add the person at the end."),

    /** Rows are reorderable across both lists: the person goes where you hover. */
    Insert("Insert anywhere", "Hover a row to place the person right there."),
}

/** Applies a change to the teams, always on their latest value. */
private typealias UpdateTeams = ((List<TeamRoster>) -> List<TeamRoster>) -> Unit

private val ColumnGap = 8.dp
private val RowGap = 8.dp

@Composable
fun BetweenListsScreen(
    onBack: () -> Unit,
) {
    BetweenListsScreen(onBack = onBack, initialMode = BetweenListsMode.Append)
}

/** Opens the screen in a given mode, for previews and snapshots. */
@Composable
internal fun BetweenListsScreen(
    onBack: () -> Unit,
    initialMode: BetweenListsMode,
) {
    var mode by remember { mutableStateOf(initialMode) }
    var teams by remember { mutableStateOf(InitialRosters) }
    val update: UpdateTeams = { transform -> teams = transform(teams) }

    DemoScaffold(
        title = Demo.BetweenLists.title,
        hint = "Drag a person to the other team.",
        code = if (mode == BetweenListsMode.Append) BetweenListsAppendCode else BetweenListsInsertCode,
        onBack = onBack,
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = insets.calculateBottomPadding() + 16.dp),
        ) {
            SegmentedControl(
                options = BetweenListsMode.entries,
                selected = mode,
                onSelect = { mode = it },
                label = { it.label },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = mode.caption,
                style = MaterialTheme.typography.bodySmall,
                color = SampleTheme.colors.inkMuted,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp),
            )
            // A fresh drag state per mode, the teams themselves carry over.
            key(mode) {
                TeamBoard(
                    mode = mode,
                    teams = teams,
                    update = update,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TeamBoard(
    mode: BetweenListsMode,
    teams: List<TeamRoster>,
    update: UpdateTeams,
    modifier: Modifier = Modifier,
) {
    // One state for every list on the screen.
    val state = rememberDragAndDropState<Teammate>()

    DragAndDropContainer(
        state = state,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(ColumnGap),
            modifier = Modifier.fillMaxSize(),
        ) {
            teams.forEachIndexed { teamIndex, team ->
                TeamColumn(
                    team = team,
                    teamIndex = teamIndex,
                    otherTeams = teams.map { it.name } - team.name,
                    mode = mode,
                    state = state,
                    update = update,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun TeamColumn(
    team: TeamRoster,
    teamIndex: Int,
    otherTeams: List<String>,
    mode: BetweenListsMode,
    state: DragAndDropState<Teammate>,
    update: UpdateTeams,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    DropWell(
        state = teamWellState(team, mode, state),
        modifier = modifier.dropTarget(
            key = team.name,
            state = state,
            dropAnimationEnabled = false,
            onDrop = { dropped ->
                if (mode == BetweenListsMode.Append) update { it.appendTo(teamIndex, dropped.data) }
            },
            onDragEnter = { entered ->
                if (mode == BetweenListsMode.Insert) update { it.appendTo(teamIndex, entered.data) }
            },
        ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionLabel(
                text = team.name,
                trailing = team.members.size.toString(),
                modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 10.dp),
            )
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(RowGap),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (mode == BetweenListsMode.Insert) {
                            Modifier.dragAutoScroll(state = state, lazyListState = listState)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                items(team.members, key = { it.name }) { person ->
                    MemberRow(person, teamIndex, otherTeams, mode, state, update)
                }
            }
        }
        if (team.members.isEmpty()) {
            EmptyTeam(Modifier.align(Alignment.Center))
        }
    }
}

/**
 * Append: the other team is ready, and accepts once hovered.
 * Insert: the team holding the person accepts, since that is where it lands.
 */
private fun teamWellState(
    team: TeamRoster,
    mode: BetweenListsMode,
    state: DragAndDropState<Teammate>,
): WellState {
    val dragged = state.draggedItem?.data ?: return WellState.Idle
    val holdsDragged = dragged in team.members
    return when (mode) {
        BetweenListsMode.Append -> wellState(
            dragging = !holdsDragged,
            hovered = state.hoveredDropTargetKey == team.name,
        )

        BetweenListsMode.Insert -> wellState(dragging = true, hovered = holdsDragged)
    }
}

@Composable
private fun LazyItemScope.MemberRow(
    person: Teammate,
    teamIndex: Int,
    otherTeams: List<String>,
    mode: BetweenListsMode,
    state: DragAndDropState<Teammate>,
    update: UpdateTeams,
) {
    val lifted: @Composable () -> Unit = { TeammateRow(person, Modifier.lifted()) }
    val base = Modifier
        .animateItem()
        .dragPlaceholder(active = state.isDragging(person.name))
    val dragModifier = when (mode) {
        BetweenListsMode.Append -> base.draggableItem(
            key = person.name,
            data = person,
            state = state,
            dropTargets = otherTeams,
            draggableContent = lifted,
        )

        BetweenListsMode.Insert -> base.reorderableItem(
            key = person.name,
            data = person,
            state = state,
            zIndex = 1f,
            dropStrategy = DropStrategy.CenterDistance,
            onDragEnter = { dragged -> update { it.insertAt(teamIndex, person, dragged.data) } },
            draggableContent = lifted,
        )
    }
    TeammateRow(person = person, modifier = dragModifier)
}

@Composable
private fun EmptyTeam(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Drop people here",
            style = MaterialTheme.typography.bodySmall,
            color = SampleTheme.colors.inkMuted,
        )
    }
}
