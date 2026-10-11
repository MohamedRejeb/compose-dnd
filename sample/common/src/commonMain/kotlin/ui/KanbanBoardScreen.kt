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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DragAxis
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.dragHandle
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScaffold
import components.DropWell
import components.GripDots
import components.WellState
import components.dragPlaceholder
import components.lifted
import navigation.Demo
import theme.SampleTheme

private val BoardGutter = 16.dp
private val ColumnSpacing = 12.dp
private val ColumnPadding = 8.dp
private val CardSpacing = 8.dp

/** A column never gets narrower than this when the whole board fits on screen. */
private val MinFitColumnWidth = 260.dp
private val MaxColumnWidth = 340.dp

/** On narrow screens a column leaves this much room so the next one peeks in. */
private val NextColumnPeek = 72.dp
private val PreferredColumnWidth = 296.dp

private const val CardTilt = 3f

private const val KanbanCode = """
// Cards share one state across columns
Modifier.reorderableItem(
    key = card.id, data = card,
    state = cardState,
    onDragEnter = { move(it.data, card) },
    draggableContent = { CardView(card) },
)
// Columns move by their header grip
Modifier.reorderableItem(
    key = column.id, data = column,
    state = columnState, hasDragHandle = true,
    dragAxis = DragAxis.Horizontal, ...
)
Modifier.dragHandle(column.id, columnState)
"""

@Composable
fun KanbanBoardScreen(
    onBack: () -> Unit,
) {
    DemoScaffold(
        title = Demo.Kanban.title,
        hint = "Long-press a card to move it. Drag a column by its header.",
        code = KanbanCode,
        onBack = onBack,
    ) { insets ->
        KanbanBoard(
            contentPadding = insets,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun KanbanBoard(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val cardState = rememberDragAndDropState<KanbanCard>(dragAfterLongPress = true)
    val columnState = rememberDragAndDropState<KanbanColumn>()
    var columns by remember { mutableStateOf(initialKanbanColumns()) }
    val rowState = rememberLazyListState()

    DragAndDropContainer(state = columnState, modifier = modifier) {
        DragAndDropContainer(state = cardState, modifier = Modifier.fillMaxSize()) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val columnWidth = columnWidthFor(maxWidth, columns.size)
                LazyRow(
                    state = rowState,
                    contentPadding = PaddingValues(
                        start = BoardGutter,
                        end = BoardGutter,
                        top = 4.dp,
                        bottom = contentPadding.calculateBottomPadding() + BoardGutter,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(ColumnSpacing),
                    modifier = Modifier
                        .fillMaxSize()
                        .dragAutoScroll(state = cardState, lazyListState = rowState)
                        .dragAutoScroll(state = columnState, lazyListState = rowState),
                ) {
                    items(columns, key = { it.id }) { column ->
                        BoardColumn(
                            column = column,
                            cardState = cardState,
                            columnState = columnState,
                            onColumnEnter = { dragged -> columns = columns.moveColumn(dragged.id, column.id) },
                            onCardEnter = { dragged, target -> columns = columns.moveCard(dragged, target.id) },
                            onDropInEmpty = { dragged -> columns = columns.moveCardToEnd(dragged, column.id) },
                            modifier = Modifier
                                .animateItem()
                                .width(columnWidth)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Wide windows split the width between all columns. Narrow ones give each column most of
 * the screen and let the next one peek in, which tells the user the board scrolls.
 */
private fun columnWidthFor(available: Dp, columnCount: Int): Dp {
    val gaps = ColumnSpacing * (columnCount - 1)
    val fitAll = (available - BoardGutter * 2 - gaps) / columnCount
    return if (fitAll >= MinFitColumnWidth) {
        fitAll.coerceAtMost(MaxColumnWidth)
    } else {
        (available - BoardGutter - ColumnSpacing - NextColumnPeek).coerceAtMost(PreferredColumnWidth)
    }
}

private fun columnKey(column: KanbanColumn) = "col-${column.id}"

@Composable
private fun BoardColumn(
    column: KanbanColumn,
    cardState: DragAndDropState<KanbanCard>,
    columnState: DragAndDropState<KanbanColumn>,
    onColumnEnter: (dragged: KanbanColumn) -> Unit,
    onCardEnter: (dragged: KanbanCard, target: KanbanCard) -> Unit,
    onDropInEmpty: (dragged: KanbanCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    val key = columnKey(column)
    val shape = SampleTheme.shapes.well
    DropWell(
        state = columnWellState(column, cardState),
        modifier = modifier
            .dragPlaceholder(active = columnState.isDragging(key), shape = shape)
            .reorderableItem(
                key = key,
                data = column,
                state = columnState,
                hasDragHandle = true,
                dragAxis = DragAxis.Horizontal,
                dropStrategy = DropStrategy.CenterDistance,
                onDragEnter = { dragged -> onColumnEnter(dragged.data) },
                draggableContent = { LiftedColumn(column) },
            ),
    ) {
        Column(modifier = Modifier.padding(ColumnPadding)) {
            ColumnHeader(
                column = column,
                modifier = Modifier.dragHandle(key = key, state = columnState),
            )
            CardList(
                column = column,
                cardState = cardState,
                onCardEnter = onCardEnter,
                onDropInEmpty = onDropInEmpty,
            )
        }
    }
}

/**
 * Cards reorder as soon as they enter a slot, so the column that holds the dragged card
 * is the one it will land in.
 */
private fun columnWellState(
    column: KanbanColumn,
    cardState: DragAndDropState<KanbanCard>,
): WellState {
    val dragged = cardState.draggedItem ?: return WellState.Idle
    val holdsCard = column.cards.any { it.id == dragged.key }
    val overEmptySlot = cardState.isDropTargetHovered(emptySlotKey(column))
    return if (holdsCard || overEmptySlot) WellState.Accept else WellState.Idle
}

private fun emptySlotKey(column: KanbanColumn) = "empty-${column.id}"

@Composable
private fun CardList(
    column: KanbanColumn,
    cardState: DragAndDropState<KanbanCard>,
    onCardEnter: (dragged: KanbanCard, target: KanbanCard) -> Unit,
    onDropInEmpty: (dragged: KanbanCard) -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(CardSpacing),
        contentPadding = PaddingValues(bottom = ColumnPadding),
        modifier = Modifier
            .fillMaxSize()
            .dragAutoScroll(state = cardState, lazyListState = listState),
    ) {
        items(column.cards, key = { it.id }) { card ->
            KanbanCardView(
                card = card,
                modifier = Modifier
                    .animateItem()
                    .dragPlaceholder(active = cardState.isDragging(card.id))
                    .reorderableItem(
                        key = card.id,
                        data = card,
                        state = cardState,
                        dropStrategy = DropStrategy.CenterDistance,
                        onDragEnter = { dragged -> onCardEnter(dragged.data, card) },
                        draggableContent = {
                            KanbanCardView(
                                card = card,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .lifted(tilt = CardTilt),
                            )
                        },
                    ).fillMaxWidth(),
            )
        }
        if (column.cards.isEmpty()) {
            item(key = emptySlotKey(column)) {
                EmptyColumnSlot(
                    column = column,
                    cardState = cardState,
                    onDrop = onDropInEmpty,
                )
            }
        }
    }
}

private val EmptySlotHeight = 96.dp

@Composable
private fun EmptyColumnSlot(
    column: KanbanColumn,
    cardState: DragAndDropState<KanbanCard>,
    onDrop: (KanbanCard) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(EmptySlotHeight)
            .dragPlaceholder(active = true)
            .dropTarget(
                key = emptySlotKey(column),
                state = cardState,
                dropAnimationEnabled = false,
                onDrop = { dragged -> onDrop(dragged.data) },
            ),
    ) {
        Text(
            text = "No cards yet",
            style = MaterialTheme.typography.bodySmall,
            color = SampleTheme.colors.inkMuted,
        )
    }
}

@Composable
private fun ColumnHeader(
    column: KanbanColumn,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(SampleTheme.shapes.pill)
                .background(column.hue.container),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = column.title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = column.cards.size.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.inkMuted,
        )
        Spacer(Modifier.weight(1f))
        GripDots(color = colors.inkMuted.copy(alpha = 0.6f))
    }
}

/** The column as it follows the pointer: the same well, lifted, with its cards. */
@Composable
private fun LiftedColumn(column: KanbanColumn) {
    val shape = SampleTheme.shapes.well
    Box(
        modifier = Modifier
            .fillMaxSize()
            .lifted(shape = shape)
            .clip(shape)
            .background(SampleTheme.colors.well),
    ) {
        Column(modifier = Modifier.padding(ColumnPadding)) {
            ColumnHeader(column = column)
            Column(verticalArrangement = Arrangement.spacedBy(CardSpacing)) {
                column.cards.forEach { card ->
                    KanbanCardView(card = card, modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
