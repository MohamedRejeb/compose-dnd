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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DragAxis
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.dragHandle
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScreenScaffold
import components.GripDots

// -- Data model --

private enum class Priority { Low, Medium, High, Urgent }

private enum class Tag(
    val label: String,
    val color: Color
) {
    Frontend("Frontend", Color(0xFF5B8DEF)),
    Backend("Backend", Color(0xFF4CAF50)),
    Design("Design", Color(0xFFAB6FE8)),
    Bug("Bug", Color(0xFFE53935)),
    Feature("Feature", Color(0xFFF5A623)),
    Docs("Docs", Color(0xFF78909C)),
}

private data class KanbanCard(
    val id: String,
    val title: String,
    val description: String? = null,
    val assignee: String? = null,
    val priority: Priority = Priority.Medium,
    val tags: List<Tag> = emptyList(),
)

private data class KanbanColumn(
    val id: String,
    val title: String,
    val color: Color,
    val cards: List<KanbanCard>,
)

private fun initialColumns() = listOf(
    KanbanColumn(
        id = "todo",
        title = "To Do",
        color = Color(0xFF5B8DEF),
        cards = listOf(
            KanbanCard("1", "Set up CI/CD pipeline", "Configure GitHub Actions for automated builds and tests", "Alex", Priority.High, listOf(Tag.Backend)),
            KanbanCard("2", "Design login screen", null, "Sarah", Priority.Medium, listOf(Tag.Design, Tag.Frontend)),
            KanbanCard("3", "Write API documentation", "Document all REST endpoints with examples", null, Priority.Low, listOf(Tag.Docs, Tag.Backend)),
            KanbanCard("4", "Fix date picker crash", "App crashes when selecting Feb 29 on non-leap years", "Mike", Priority.Urgent, listOf(Tag.Bug, Tag.Frontend)),
            KanbanCard("5", "Add dark mode support", null, null, Priority.Medium, listOf(Tag.Feature, Tag.Design)),
            KanbanCard("6", "Database migration script", "Migrate user table to new schema with zero downtime", "Alex", Priority.High, listOf(Tag.Backend)),
        ),
    ),
    KanbanColumn(
        id = "progress",
        title = "In Progress",
        color = Color(0xFFF5A623),
        cards = listOf(
            KanbanCard("7", "Implement search feature", "Full-text search with filters and sorting", "Sarah", Priority.High, listOf(Tag.Feature, Tag.Frontend, Tag.Backend)),
            KanbanCard("8", "Update onboarding flow", null, "Mike", Priority.Medium, listOf(Tag.Design)),
            KanbanCard("9", "Fix memory leak in image loader", "Profile shows increasing heap usage after scrolling gallery", "Alex", Priority.Urgent, listOf(Tag.Bug)),
        ),
    ),
    KanbanColumn(
        id = "review",
        title = "Review",
        color = Color(0xFFAB6FE8),
        cards = listOf(
            KanbanCard("10", "Add unit tests for auth module", "Cover login, logout, token refresh, and session expiry", "Mike", Priority.Medium, listOf(Tag.Backend)),
            KanbanCard("11", "Redesign settings page", null, "Sarah", Priority.Low, listOf(Tag.Design, Tag.Frontend)),
        ),
    ),
    KanbanColumn(
        id = "done",
        title = "Done",
        color = Color(0xFF4CAF50),
        cards = listOf(
            KanbanCard("12", "Set up project structure", null, "Alex", Priority.High, listOf(Tag.Backend, Tag.Frontend)),
            KanbanCard("13", "Create color palette", "Define primary, secondary, and accent colors for the brand", "Sarah", Priority.Low, listOf(Tag.Design)),
            KanbanCard("14", "Configure linter", null, "Mike", Priority.Low, listOf(Tag.Docs)),
            KanbanCard("15", "Fix navigation crash on back press", null, "Alex", Priority.Urgent, listOf(Tag.Bug, Tag.Frontend)),
        ),
    ),
)

// -- Screen --

@Composable
fun KanbanBoardScreen(
    onBack: () -> Unit,
) {
    DemoScreenScaffold(
        title = "Kanban Board",
        onBack = onBack,
    ) { paddingValues ->
        KanbanBoardContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        )
    }
}

@OptIn(ExperimentalDndApi::class)
@Composable
private fun KanbanBoardContent(
    modifier: Modifier = Modifier,
) {
    val dndState = rememberDragAndDropState<KanbanCard>()
    val columnDndState = rememberDragAndDropState<KanbanColumn>()
    var columns by remember { mutableStateOf(initialColumns()) }
    val rowState = rememberLazyListState()

    DragAndDropContainer(
        state = columnDndState,
        modifier = modifier,
    ) {
        DragAndDropContainer(
            state = dndState,
            modifier = Modifier.fillMaxSize(),
        ) {
        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = dndState,
                    lazyListState = rowState,
                ).dragAutoScroll(
                    state = columnDndState,
                    lazyListState = rowState,
                ),
        ) {
            items(columns, key = { it.id }) { column ->
                KanbanColumnUi(
                    column = column,
                    dndState = dndState,
                    columnDndState = columnDndState,
                    onColumnEnter = { dragged, target ->
                        if (dragged.id != target.id) {
                            columns = columns.toMutableList().apply {
                                val from = indexOfFirst { it.id == dragged.id }
                                val to = indexOfFirst { it.id == target.id }
                                if (from != -1 && to != -1) add(to, removeAt(from))
                            }
                        }
                    },
                    onReorder = { draggedCard, targetCard ->
                        if (draggedCard.id == targetCard.id) return@KanbanColumnUi

                        columns = columns.map { col ->
                            val targetIndex = col.cards.indexOfFirst { it.id == targetCard.id }
                            if (targetIndex == -1) {
                                col.copy(cards = col.cards.filter { it.id != draggedCard.id })
                            } else {
                                col.copy(
                                    cards = col.cards
                                        .filter { it.id != draggedCard.id }
                                        .toMutableList()
                                        .apply { add(targetIndex.coerceAtMost(size), draggedCard) }
                                )
                            }
                        }
                    },
                    onDropInEmpty = { draggedCard ->
                        columns = columns.map { col ->
                            if (col.id == column.id) {
                                col.copy(cards = col.cards + draggedCard)
                            } else {
                                col.copy(cards = col.cards.filter { it.id != draggedCard.id })
                            }
                        }
                    },
                    modifier = Modifier.animateItem(),
                )
            }
        }
        }
    }
}

@OptIn(ExperimentalDndApi::class)
@Composable
private fun KanbanColumnUi(
    column: KanbanColumn,
    dndState: DragAndDropState<KanbanCard>,
    columnDndState: DragAndDropState<KanbanColumn>,
    onColumnEnter: (dragged: KanbanColumn, target: KanbanColumn) -> Unit,
    onReorder: (draggedCard: KanbanCard, targetCard: KanbanCard) -> Unit,
    onDropInEmpty: (draggedCard: KanbanCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colState = rememberLazyListState()
    val shape = MaterialTheme.shapes.large
    val isColumnDragging = columnDndState.isDragging("col-${column.id}")

    Column(
        modifier = modifier
            .graphicsLayer { alpha = if (isColumnDragging) 0f else 1f }
            .reorderableItem(
                key = "col-${column.id}",
                data = column,
                state = columnDndState,
                hasDragHandle = true,
                dragAxis = DragAxis.Horizontal,
                dropStrategy = DropStrategy.CenterDistance,
                onDragEnter = { state -> onColumnEnter(state.data, column) },
                draggableContent = {
                    ColumnDragPreview(column = column)
                },
            ).width(280.dp)
            .fillMaxHeight()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(8.dp),
    ) {
        // Column header doubles as the drag handle for reordering columns
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .dragHandle(
                    key = "col-${column.id}",
                    state = columnDndState,
                ).padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(column.color),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = column.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${column.cards.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            GripDots(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // Cards list
        LazyColumn(
            state = colState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .dragAutoScroll(
                    state = dndState,
                    lazyListState = colState,
                ),
        ) {
            items(column.cards, key = { it.id }) { card ->
                val isDragging = dndState.isDragging(card.id)

                KanbanCardUi(
                    card = card,
                    columnColor = column.color,
                    modifier = Modifier
                        .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                        .reorderableItem(
                            key = card.id,
                            data = card,
                            state = dndState,
                            dropStrategy = DropStrategy.CenterDistance,
                            dragAfterLongPress = true,
                            onDragEnter = { state -> onReorder(state.data, card) },
                            draggableContent = {
                                KanbanCardUi(
                                    card = card,
                                    columnColor = column.color,
                                    isDragShadow = true,
                                    modifier = Modifier.width(264.dp),
                                )
                            },
                        ).animateItem()
                        .fillMaxWidth(),
                )
            }

            // Empty column placeholder
            if (column.cards.isEmpty()) {
                item(key = "empty-${column.id}") {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.medium,
                            ).dropTarget(
                                key = "empty-${column.id}",
                                state = dndState,
                                dropAnimationEnabled = false,
                                onDrop = { state -> onDropInEmpty(state.data) },
                            ),
                    ) {
                        Text(
                            text = "Drop here",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnDragPreview(
    column: KanbanColumn,
) {
    val shape = MaterialTheme.shapes.large

    Column(
        modifier = Modifier
            .graphicsLayer { rotationZ = -2f }
            .shadow(elevation = 16.dp, shape = shape)
            .width(280.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(column.color),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = column.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            GripDots(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            column.cards.take(3).forEach { card ->
                KanbanCardUi(
                    card = card,
                    columnColor = column.color,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (column.cards.size > 3) {
                Text(
                    text = "+${column.cards.size - 3} more",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}

// -- Card UI --

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KanbanCardUi(
    card: KanbanCard,
    columnColor: Color,
    isDragShadow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    val priorityColor = when (card.priority) {
        Priority.Urgent -> Color(0xFFE53935)
        Priority.High -> Color(0xFFF5A623)
        Priority.Medium -> Color(0xFF5B8DEF)
        Priority.Low -> Color(0xFF78909C)
    }

    Column(
        modifier = modifier
            .then(
                if (isDragShadow) {
                    Modifier.shadow(elevation = 12.dp, shape = shape)
                } else {
                    Modifier
                }
            ).clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = shape,
            ).padding(12.dp),
    ) {
        // Priority indicator + title
        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(priorityColor),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = card.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Description (variable height)
        if (card.description != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = card.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
        }

        // Tags
        if (card.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                card.tags.forEach { tag ->
                    Text(
                        text = tag.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tag.color,
                        fontSize = 10.sp,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(tag.color.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }

        // Assignee
        if (card.assignee != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar circle with initial
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Text(
                        text = card.assignee.first().toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = card.assignee,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
