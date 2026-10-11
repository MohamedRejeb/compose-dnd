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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.CircleIconButton
import components.DemoScaffold
import components.DropWell
import components.OptionToggle
import components.SectionLabel
import components.dragPlaceholder
import kotlin.math.roundToInt
import navigation.Demo
import theme.SampleTheme

private val StickyNotesCode = """
    Modifier.dropTarget(
      key = "board",
      state = state,
      dropAlignment = Alignment.TopStart,
      // The drop lands on the snapped spot
      dropOffset = landingPosition(dragAmount),
      onDrop = { move(it.data, it.dragAmount) },
    )

    notes.forEach { note ->
      StickyNote(note, Modifier.draggableItem(
        key = note.id,
        data = note.id,
        state = state,
        draggableContent = { StickyNote(note) },
      ))
    }
""".trimIndent()

private const val BoardKey = "board"
private const val TrashKey = "trash"
private val BoardGutter = 16.dp

/**
 * Free-form placement: notes live at absolute positions on a board.
 *
 * The board is one big drop target. Its dropOffset is recomputed from the
 * dragged note's dragAmount while it moves, so the drop animation lands the
 * note on its snapped grid position before onDrop commits it. A trash target
 * with a higher zIndex overlaps the board and wins while hovered.
 */
@Composable
fun StickyNotesBoardScreen(
    onBack: () -> Unit,
) {
    val density = LocalDensity.current
    var notes by remember { mutableStateOf(initialNotes(density)) }
    var nextId by remember { mutableIntStateOf(notes.size) }

    DemoScaffold(
        title = Demo.StickyNotes.title,
        hint = "Drag a note anywhere on the board, or onto the trash to delete it.",
        code = StickyNotesCode,
        onBack = onBack,
        actions = {
            CircleIconButton(
                icon = Icons.Rounded.Add,
                contentDescription = "Add note",
                onClick = {
                    notes = notes + newNote(nextId, density)
                    nextId++
                },
            )
        },
    ) { insets ->
        StickyNotesBoard(
            notes = notes,
            onNotesChange = { notes = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(start = BoardGutter, end = BoardGutter, bottom = BoardGutter),
        )
    }
}

@Composable
private fun StickyNotesBoard(
    notes: List<BoardNote>,
    onNotesChange: (List<BoardNote>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberDragAndDropState<Int>()
    val density = LocalDensity.current
    var snapToGrid by remember { mutableStateOf(true) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }
    val placement = BoardPlacement(
        gridStepPx = with(density) { NoteGridStep.toPx() },
        noteSizePx = with(density) { NoteSize.toPx() },
        boardSize = boardSize,
        snapToGrid = snapToGrid,
    )

    val dragged = state.draggedItem
    val draggedNote = dragged?.let { item -> notes.find { it.id == item.data } }
    val landing = draggedNote?.let { placement.landingPosition(it.position, dragged.dragAmount) }

    DragAndDropContainer(
        state = state,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            BoardToolbar(
                noteCount = notes.size,
                snapToGrid = snapToGrid,
                onSnapToGridChange = { snapToGrid = it },
            )
            DropWell(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { boardSize = it }
                    .dropTarget(
                        key = BoardKey,
                        state = state,
                        dropAlignment = Alignment.TopStart,
                        // Land the drop animation exactly on the snapped position
                        dropOffset = landing ?: Offset.Zero,
                        onDrop = { item ->
                            onNotesChange(notes.moveToFront(item.data) { placement.landingPosition(it, item.dragAmount) })
                        },
                    ),
            ) {
                if (snapToGrid) BoardDots(step = NoteGridStep)
                BoardNotes(notes = notes, state = state)
                if (landing != null && state.isDropTargetHovered(BoardKey)) {
                    LandingPreview(Modifier.offset { landing.toIntOffset() })
                }
                TrashTarget(
                    isDragging = dragged != null,
                    isHovered = state.isDropTargetHovered(TrashKey),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(BoardGutter)
                        .dropTarget(
                            key = TrashKey,
                            state = state,
                            // Wins over the board it overlaps
                            zIndex = 1f,
                            onDrop = { item -> onNotesChange(notes.filterNot { it.id == item.data }) },
                        ),
                )
            }
        }
    }
}

@Composable
private fun BoardToolbar(
    noteCount: Int,
    snapToGrid: Boolean,
    onSnapToGridChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SectionLabel(
            text = "Board",
            trailing = if (noteCount == 1) "1 note" else "$noteCount notes",
        )
        OptionToggle(
            label = "Snap to grid",
            checked = snapToGrid,
            onCheckedChange = onSnapToGridChange,
        )
    }
}

@Composable
private fun BoardNotes(
    notes: List<BoardNote>,
    state: DragAndDropState<Int>,
) {
    val overTrash = state.isDropTargetHovered(TrashKey)
    notes.forEach { note ->
        StickyNote(
            note = note,
            modifier = Modifier
                .offset { note.position.toIntOffset() }
                .dragPlaceholder(active = state.isDragging(note.id), shape = SampleTheme.shapes.small)
                .draggableItem(
                    key = note.id,
                    data = note.id,
                    state = state,
                    draggableContent = {
                        LiftedStickyNote(note = note, overTrash = overTrash)
                    },
                ),
        )
    }
}

/** Where notes land on the board: moved by the drag, snapped if asked, and kept inside. */
private class BoardPlacement(
    val gridStepPx: Float,
    val noteSizePx: Float,
    val boardSize: IntSize,
    val snapToGrid: Boolean,
) {
    fun landingPosition(position: Offset, dragAmount: Offset): Offset {
        val moved = position + dragAmount
        val snapped = if (snapToGrid) {
            Offset(
                x = (moved.x / gridStepPx).roundToInt() * gridStepPx,
                y = (moved.y / gridStepPx).roundToInt() * gridStepPx,
            )
        } else {
            moved
        }
        return Offset(
            x = snapped.x.coerceIn(0f, (boardSize.width - noteSizePx).coerceAtLeast(0f)),
            y = snapped.y.coerceIn(0f, (boardSize.height - noteSizePx).coerceAtLeast(0f)),
        )
    }
}

/** Moves the note with [id] to its new position and to the top of the pile. */
private fun List<BoardNote>.moveToFront(
    id: Int,
    newPosition: (Offset) -> Offset,
): List<BoardNote> {
    val note = find { it.id == id } ?: return this
    return filterNot { it.id == id } + note.copy(position = newPosition(note.position))
}

private fun Offset.toIntOffset() = IntOffset(x.roundToInt(), y.roundToInt())
