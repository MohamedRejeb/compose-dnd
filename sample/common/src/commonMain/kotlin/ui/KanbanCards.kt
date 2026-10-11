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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.Avatar
import components.ItemSurface
import theme.Hue
import theme.Hues
import theme.SampleTheme

// -- Data model --

internal enum class Priority(
    val label: String
) {
    Low("Low"),
    Medium("Medium"),
    High("High"),
    Urgent("Urgent"),
}

internal enum class Tag(
    val label: String,
    val hue: Hue,
) {
    Frontend("Frontend", Hues.Sky),
    Backend("Backend", Hues.Mint),
    Design("Design", Hues.Lilac),
    Bug("Bug", Hues.Rose),
    Feature("Feature", Hues.Butter),
    Docs("Docs", Hues.Peach),
}

internal data class Person(
    val name: String,
    val hue: Hue,
)

private val Alex = Person("Alex", Hues.Sky)
private val Sarah = Person("Sarah", Hues.Rose)
private val Mike = Person("Mike", Hues.Mint)

internal data class KanbanCard(
    val id: String,
    val title: String,
    val description: String? = null,
    val assignee: Person? = null,
    val priority: Priority = Priority.Medium,
    val tags: List<Tag> = emptyList(),
)

internal data class KanbanColumn(
    val id: String,
    val title: String,
    val hue: Hue,
    val cards: List<KanbanCard>,
)

internal fun initialKanbanColumns(): List<KanbanColumn> = listOf(
    KanbanColumn(
        id = "todo",
        title = "To do",
        hue = Hues.Sky,
        cards = listOf(
            KanbanCard("1", "Set up CI pipeline", "Build and test every pull request.", Alex, Priority.High, listOf(Tag.Backend)),
            KanbanCard("2", "Design the login screen", null, Sarah, Priority.Medium, listOf(Tag.Design, Tag.Frontend)),
            KanbanCard("3", "Write API documentation", "Document every REST endpoint with an example.", null, Priority.Low, listOf(Tag.Docs, Tag.Backend)),
            KanbanCard("4", "Fix date picker crash", "Picking Feb 29 in a non-leap year crashes the app.", Mike, Priority.Urgent, listOf(Tag.Bug, Tag.Frontend)),
            KanbanCard("5", "Add dark mode", null, null, Priority.Medium, listOf(Tag.Feature, Tag.Design)),
            KanbanCard("6", "Migrate the user table", "Move to the new schema with zero downtime.", Alex, Priority.High, listOf(Tag.Backend)),
        ),
    ),
    KanbanColumn(
        id = "progress",
        title = "In progress",
        hue = Hues.Butter,
        cards = listOf(
            KanbanCard("7", "Search with filters", "Full-text search with filters and sorting.", Sarah, Priority.High, listOf(Tag.Feature, Tag.Frontend)),
            KanbanCard("8", "Update onboarding flow", null, Mike, Priority.Medium, listOf(Tag.Design)),
            KanbanCard("9", "Fix image loader leak", "Heap keeps growing while the gallery scrolls.", Alex, Priority.Urgent, listOf(Tag.Bug)),
        ),
    ),
    KanbanColumn(
        id = "review",
        title = "Review",
        hue = Hues.Lilac,
        cards = listOf(
            KanbanCard("10", "Test the auth module", "Cover login, logout and token refresh.", Mike, Priority.Medium, listOf(Tag.Backend)),
            KanbanCard("11", "Redesign settings page", null, Sarah, Priority.Low, listOf(Tag.Design, Tag.Frontend)),
        ),
    ),
    KanbanColumn(
        id = "done",
        title = "Done",
        hue = Hues.Mint,
        cards = listOf(
            KanbanCard("12", "Set up project structure", null, Alex, Priority.High, listOf(Tag.Backend, Tag.Frontend)),
            KanbanCard("13", "Create the color palette", "Pick brand, accent and neutral colors.", Sarah, Priority.Low, listOf(Tag.Design)),
            KanbanCard("14", "Configure the linter", null, Mike, Priority.Low, listOf(Tag.Docs)),
            KanbanCard("15", "Fix crash on back press", null, Alex, Priority.Urgent, listOf(Tag.Bug, Tag.Frontend)),
        ),
    ),
)

// -- Immutable board updates --

/** Moves the column [draggedId] to the slot of [targetId]. */
internal fun List<KanbanColumn>.moveColumn(draggedId: String, targetId: String): List<KanbanColumn> {
    val from = indexOfFirst { it.id == draggedId }
    val to = indexOfFirst { it.id == targetId }
    if (from == -1 || to == -1 || from == to) return this
    val dragged = this[from]
    return filterIndexed { index, _ -> index != from }.let { rest ->
        rest.take(to) + dragged + rest.drop(to)
    }
}

/** Takes [card] out of its column and puts it in the slot of [targetId], in whichever column that is. */
internal fun List<KanbanColumn>.moveCard(card: KanbanCard, targetId: String): List<KanbanColumn> {
    if (card.id == targetId) return this
    return map { column ->
        val rest = column.cards.filter { it.id != card.id }
        val targetIndex = column.cards.indexOfFirst { it.id == targetId }
        if (targetIndex == -1) {
            column.copy(cards = rest)
        } else {
            val slot = targetIndex.coerceAtMost(rest.size)
            column.copy(cards = rest.take(slot) + card + rest.drop(slot))
        }
    }
}

/** Takes [card] out of its column and adds it at the end of [columnId]. */
internal fun List<KanbanColumn>.moveCardToEnd(card: KanbanCard, columnId: String): List<KanbanColumn> =
    map { column ->
        val rest = column.cards.filter { it.id != card.id }
        column.copy(cards = if (column.id == columnId) rest + card else rest)
    }

// -- Card UI --

private val CardPadding = 14.dp
private val AvatarSize = 24.dp
private const val DescriptionMaxLines = 2

@Composable
internal fun KanbanCardView(
    card: KanbanCard,
    modifier: Modifier = Modifier,
) {
    ItemSurface(modifier = modifier) {
        Column(modifier = Modifier.padding(CardPadding)) {
            if (card.tags.isNotEmpty()) {
                TagRow(card.tags)
                Spacer(Modifier.height(10.dp))
            }
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                color = SampleTheme.colors.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (card.description != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = SampleTheme.colors.inkMuted,
                    maxLines = DescriptionMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(12.dp))
            CardFooter(card)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagRow(tags: List<Tag>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        tags.forEach { tag -> TagChip(tag) }
    }
}

@Composable
private fun TagChip(tag: Tag) {
    Text(
        text = tag.label,
        style = MaterialTheme.typography.labelSmall,
        color = tag.hue.onContainer,
        modifier = Modifier
            .clip(SampleTheme.shapes.pill)
            .background(tag.hue.container)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun CardFooter(card: KanbanCard) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(AvatarSize),
    ) {
        if (card.assignee != null) {
            Avatar(name = card.assignee.name, hue = card.assignee.hue, size = AvatarSize)
            Spacer(Modifier.width(8.dp))
            Text(
                text = card.assignee.name,
                style = MaterialTheme.typography.labelMedium,
                color = colors.inkMuted,
            )
        } else {
            Text(
                text = "Unassigned",
                style = MaterialTheme.typography.labelMedium,
                color = colors.inkMuted,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = card.priority.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (card.priority == Priority.Urgent) colors.danger else colors.inkMuted,
        )
    }
}
