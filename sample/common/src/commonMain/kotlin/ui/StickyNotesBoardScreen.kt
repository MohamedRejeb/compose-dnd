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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.DemoScreenScaffold
import kotlin.math.roundToInt

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

    DemoScreenScaffold(
        title = "Sticky Notes Board",
        onBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    notes = notes + newNote(nextId, density)
                    nextId++
                },
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add note")
            }
        },
    ) { paddingValues ->
        StickyNotesBoard(
            notes = notes,
            onNotesChange = { notes = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        )
    }
}

private const val BOARD_KEY = "board"
private const val TRASH_TARGET_KEY = "trash"
private val NoteSize = 112.dp
private val GridStep = 24.dp
private val NoteSpacing = GridStep * 6

private val NoteColors = listOf(
    Color(0xFFFFE082),
    Color(0xFFA5D6A7),
    Color(0xFF90CAF9),
    Color(0xFFF48FB1),
    Color(0xFFCE93D8),
)

@Immutable
private data class Note(
    val id: Int,
    val text: String,
    val color: Color,
    /** Top-left on the board, in px. */
    val position: Offset,
)

private val NoteTexts = listOf("Ship 1.0 alpha", "Write docs", "Record demo", "Fix #73", "Plan beta")

/** Notes start on a two-column layout aligned with the snapping grid. */
private fun newNote(id: Int, density: Density): Note =
    with(density) {
        Note(
            id = id,
            text = NoteTexts[id % NoteTexts.size],
            color = NoteColors[id % NoteColors.size],
            position = Offset(
                x = (GridStep + NoteSpacing * (id % 2)).toPx(),
                y = (GridStep + NoteSpacing * ((id / 2) % 4)).toPx(),
            ),
        )
    }

private fun initialNotes(density: Density): List<Note> = List(4) { newNote(it, density) }

@Composable
private fun StickyNotesBoard(
    notes: List<Note>,
    onNotesChange: (List<Note>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberDragAndDropState<Int>()
    val density = LocalDensity.current
    val gridStepPx = with(density) { GridStep.toPx() }
    val noteSizePx = with(density) { NoteSize.toPx() }
    var snapToGrid by remember { mutableStateOf(true) }
    var boardSize by remember { mutableStateOf(IntSize.Zero) }

    /** Where the dragged note would land: moved by the drag, snapped and kept on the board. */
    fun landingPosition(note: Note, dragAmount: Offset): Offset {
        val moved = note.position + dragAmount
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

    val draggedNote = state.draggedItem?.let { dragged -> notes.find { it.id == dragged.data } }
    val landing = draggedNote?.let { note ->
        landingPosition(note, state.draggedItem?.dragAmount ?: Offset.Zero)
    }

    DragAndDropContainer(
        state = state,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Drag notes anywhere. Drop one on the trash to delete it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = snapToGrid,
                    onClick = { snapToGrid = !snapToGrid },
                    label = { Text("Snap to grid") },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .onSizeChanged { boardSize = it }
                    .dropTarget(
                        key = BOARD_KEY,
                        state = state,
                        dropAlignment = Alignment.TopStart,
                        // Land the drop animation exactly on the snapped position
                        dropOffset = landing ?: Offset.Zero,
                        onDrop = { dragged ->
                            onNotesChange(
                                notes.map { note ->
                                    if (note.id == dragged.data) {
                                        note.copy(position = landingPosition(note, dragged.dragAmount))
                                    } else {
                                        note
                                    }
                                },
                            )
                        },
                    ),
            ) {
                if (snapToGrid) {
                    GridDots(stepPx = gridStepPx)
                }

                // A dashed preview of where the dragged note will land
                if (landing != null && state.isDropTargetHovered(BOARD_KEY)) {
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(landing.x.roundToInt(), landing.y.roundToInt()) }
                            .size(NoteSize)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                    )
                }

                notes.forEach { note ->
                    StickyNote(
                        note = note,
                        modifier = Modifier
                            .offset { IntOffset(note.position.x.roundToInt(), note.position.y.roundToInt()) }
                            .graphicsLayer { alpha = if (state.isDragging(note.id)) 0f else 1f }
                            .draggableItem(
                                key = note.id,
                                data = note.id,
                                state = state,
                                draggableContent = {
                                    StickyNote(note = note, isLifted = true)
                                },
                            ),
                    )
                }

                TrashTarget(
                    isVisible = state.draggedItem != null,
                    isHovered = state.isDropTargetHovered(TRASH_TARGET_KEY),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .dropTarget(
                            key = TRASH_TARGET_KEY,
                            state = state,
                            // Wins over the board it overlaps
                            zIndex = 1f,
                            onDrop = { dragged ->
                                onNotesChange(notes.filterNot { it.id == dragged.data })
                            },
                        ),
                )
            }
        }
    }
}

@Composable
private fun StickyNote(
    note: Note,
    modifier: Modifier = Modifier,
    isLifted: Boolean = false,
) {
    Box(
        modifier = modifier
            .size(NoteSize)
            .graphicsLayer { rotationZ = if (isLifted) -3f else 0f }
            .shadow(if (isLifted) 12.dp else 2.dp, RoundedCornerShape(4.dp))
            .background(note.color, RoundedCornerShape(4.dp))
            .padding(12.dp),
    ) {
        Text(
            text = note.text,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Black.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun TrashTarget(
    isVisible: Boolean,
    isHovered: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.error

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                alpha = if (isVisible) 1f else 0.4f
                val scale = if (isHovered) 1.2f else 1f
                scaleX = scale
                scaleY = scale
            }.size(64.dp)
            .background(if (isHovered) color else color.copy(alpha = 0.15f), CircleShape),
    ) {
        Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = "Trash",
            tint = if (isHovered) MaterialTheme.colorScheme.onError else color,
        )
    }
}

@Composable
private fun GridDots(stepPx: Float) {
    val dotColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        var x = 0f
        while (x <= size.width) {
            var y = 0f
            while (y <= size.height) {
                drawCircle(color = dotColor, radius = 1.5f, center = Offset(x, y))
                y += stepPx
            }
            x += stepPx
        }
    }
}
