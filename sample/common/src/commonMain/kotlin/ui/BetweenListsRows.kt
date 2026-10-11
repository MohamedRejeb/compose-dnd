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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.Avatar
import components.ItemSurface
import theme.Hue
import theme.Hues
import theme.SampleTheme

@Immutable
internal data class Teammate(
    val name: String,
    val role: String,
    val hue: Hue,
)

@Immutable
internal data class TeamRoster(
    /** Also the team's drop target key. */
    val name: String,
    val members: List<Teammate>,
)

internal val InitialRosters: List<TeamRoster> = listOf(
    TeamRoster(
        name = "Design",
        members = listOf(
            Teammate("Maya Chen", "Product", Hues.Rose),
            Teammate("Leo Park", "Research", Hues.Sky),
            Teammate("Ines Duarte", "Brand", Hues.Butter),
            Teammate("Omar Aziz", "Design lead", Hues.Lilac),
        ),
    ),
    TeamRoster(
        name = "Platform",
        members = listOf(
            Teammate("Sara Kim", "Android", Hues.Mint),
            Teammate("Tom Weber", "Backend", Hues.Peach),
            Teammate("Nia Okafor", "iOS", Hues.Sky),
            Teammate("Ravi Patel", "Web", Hues.Butter),
        ),
    ),
)

/**
 * Moves [person] to the end of the team at [teamIndex], only if it currently
 * belongs to another team. Returns the same list otherwise.
 */
internal fun List<TeamRoster>.appendTo(teamIndex: Int, person: Teammate): List<TeamRoster> {
    val comesFromOtherTeam = filterIndexed { i, _ -> i != teamIndex }.any { person in it.members }
    if (!comesFromOtherTeam) return this
    return mapIndexed { i, team ->
        val without = team.members - person
        team.copy(members = if (i == teamIndex) without + person else without)
    }
}

/**
 * Puts [person] where [anchor] sits in the team at [teamIndex], taking it out
 * of whichever team holds it now. Returns the same list if [anchor] is not there.
 */
internal fun List<TeamRoster>.insertAt(teamIndex: Int, anchor: Teammate, person: Teammate): List<TeamRoster> {
    val index = this[teamIndex].members.indexOf(anchor)
    if (index == -1) return this
    return mapIndexed { i, team ->
        val without = team.members - person
        team.copy(members = if (i == teamIndex) without.take(index) + person + without.drop(index) else without)
    }
}

private val AvatarSize = 28.dp

/** A compact person row, sized to fit two columns side by side on a phone. */
@Composable
internal fun TeammateRow(
    person: Teammate,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    ItemSurface(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Avatar(name = person.name, hue = person.hue, size = AvatarSize)
            Column {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = person.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

internal const val BetweenListsAppendCode = """
val dnd = rememberDragAndDropState<Teammate>()

Modifier.dropTarget(
    key = "Platform",
    state = dnd,
    onDrop = { dropped ->
        teams = teams.appendTo(1, dropped.data)
    },
)

Modifier.draggableItem(
    key = person.name, data = person, state = dnd,
    dropTargets = listOf("Platform"),
) { TeammateRow(person) }
"""

internal const val BetweenListsInsertCode = """
// One state spans both lists
val dnd = rememberDragAndDropState<Teammate>()

Modifier.reorderableItem(
    key = person.name,
    data = person,
    state = dnd,
    onDragEnter = { dragged ->
        // Leaves its list, lands here
        teams = teams.insertAt(
            teamIndex, person, dragged.data,
        )
    },
) { TeammateRow(person) }
"""
