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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.CircleIconButton
import components.DemoScaffold
import components.SectionLabel
import components.WellState
import components.dragPlaceholder
import components.lifted
import components.wellState
import theme.SampleTheme

private const val FileTilt = 3f
private const val GridColumns = 2

private val ConditionalDropCode = """
    val dragged = dndState.draggedItem?.data

    Modifier.dropTarget(
        key = folder,
        state = dndState,
        canDrop = dragged == null ||
            folder.accepts(dragged, count),
        onDrop = { file ->
            placement += file.data to folder
        },
    )
"""

/**
 * `canDrop` in action: folders only take their own file type and hold
 * [FolderCapacity] files. A rejecting folder is never hovered by the library,
 * so the screen finds it with a little geometry to show why it says no.
 */
@Composable
fun ConditionalDropScreen(
    onBack: () -> Unit,
) {
    ConditionalDrop(initialPlacement = emptyMap(), onBack = onBack)
}

/** The screen with some files already filed, for previews and snapshots. */
@Composable
internal fun ConditionalDrop(
    initialPlacement: Map<SampleFile, Folder>,
    onBack: () -> Unit,
) {
    var placement by remember { mutableStateOf(initialPlacement) }

    DemoScaffold(
        title = "Conditional drop",
        hint = "Drag each file into a folder. Folders only take their own type.",
        code = ConditionalDropCode,
        onBack = onBack,
        actions = {
            CircleIconButton(
                icon = Icons.Rounded.Refresh,
                contentDescription = "Reset files",
                onClick = { placement = emptyMap() },
            )
        },
    ) { insets ->
        ConditionalDropContent(
            placement = placement,
            onFile = { file, folder -> placement = placement + (file to folder) },
            insets = insets,
        )
    }
}

@Composable
private fun ConditionalDropContent(
    placement: Map<SampleFile, Folder>,
    onFile: (SampleFile, Folder) -> Unit,
    insets: PaddingValues,
) {
    val state = rememberDragAndDropState<SampleFile>()
    var fileBounds by remember { mutableStateOf(emptyMap<SampleFile, Rect>()) }
    var folderBounds by remember { mutableStateOf(emptyMap<Folder, Rect>()) }
    val looseFiles = SampleFiles.filter { it !in placement }
    val rejecting = rejectingFolder(state, placement, fileBounds, folderBounds)

    DragAndDropContainer(
        state = state,
        modifier = Modifier
            .fillMaxSize()
            .padding(insets)
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SectionLabel(
                text = "Files",
                trailing = "${looseFiles.size} left",
                modifier = Modifier.padding(bottom = 12.dp),
            )
            FileGrid(
                files = looseFiles,
                state = state,
                rejected = rejecting != null,
                onPlaced = { file, rect ->
                    if (fileBounds[file] != rect) fileBounds = fileBounds + (file to rect)
                },
            )
            Spacer(Modifier.height(20.dp))
            SectionLabel(text = "Folders", modifier = Modifier.padding(bottom = 12.dp))
            FolderRow(
                placement = placement,
                state = state,
                rejecting = rejecting,
                onFile = onFile,
                onPlaced = { folder, rect ->
                    if (folderBounds[folder] != rect) folderBounds = folderBounds + (folder to rect)
                },
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
    }
}

@Composable
private fun FolderRow(
    placement: Map<SampleFile, Folder>,
    state: DragAndDropState<SampleFile>,
    rejecting: Folder?,
    onFile: (SampleFile, Folder) -> Unit,
    onPlaced: (Folder, Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Folder.entries.forEach { folder ->
            FolderTarget(
                folder = folder,
                files = SampleFiles.filter { placement[it] == folder },
                state = state,
                rejectedHere = rejecting == folder,
                onDrop = { file -> onFile(file, folder) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .onPlaced { onPlaced(folder, it.boundsInRoot()) },
            )
        }
    }
}

@Composable
private fun FileGrid(
    files: List<SampleFile>,
    state: DragAndDropState<SampleFile>,
    rejected: Boolean,
    onPlaced: (SampleFile, Rect) -> Unit,
) {
    if (files.isEmpty()) {
        Text(
            text = "Every file is in a folder.",
            style = MaterialTheme.typography.bodyMedium,
            color = SampleTheme.colors.inkMuted,
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        files.chunked(GridColumns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { file ->
                    DraggableFile(
                        file = file,
                        state = state,
                        rejected = rejected,
                        modifier = Modifier
                            .weight(1f)
                            .onPlaced { onPlaced(file, it.boundsInRoot()) },
                    )
                }
                repeat(GridColumns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DraggableFile(
    file: SampleFile,
    state: DragAndDropState<SampleFile>,
    rejected: Boolean,
    modifier: Modifier = Modifier,
) {
    FileCard(
        file = file,
        modifier = modifier
            .dragPlaceholder(active = state.isDragging(file.name))
            .draggableItem(
                key = file.name,
                data = file,
                state = state,
                draggableContent = {
                    FileCard(
                        file = file,
                        rejected = rejected,
                        modifier = Modifier.lifted(tilt = FileTilt),
                    )
                },
            ),
    )
}

@Composable
private fun FolderTarget(
    folder: Folder,
    files: List<SampleFile>,
    state: DragAndDropState<SampleFile>,
    rejectedHere: Boolean,
    onDrop: (SampleFile) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dragged = state.draggedItem?.data
    val reason = dragged?.let { rejectReason(folder, it, files.size) }
    val wellState = if (rejectedHere) {
        WellState.Reject
    } else {
        wellState(
            dragging = dragged != null,
            hovered = state.isDropTargetHovered(folder),
            canDrop = reason == null,
        )
    }
    FolderWell(
        folder = folder,
        files = files,
        state = wellState,
        reason = reason,
        modifier = modifier.dropTarget(
            key = folder,
            state = state,
            canDrop = dragged == null || folder.accepts(dragged, files.size),
            onDrop = { item -> onDrop(item.data) },
        ),
    )
}

/**
 * The folder under the dragged file's center that refuses it. The library
 * skips targets with `canDrop = false` when it picks the hovered one, so this
 * is only the visual reason, never a drop.
 */
private fun rejectingFolder(
    state: DragAndDropState<SampleFile>,
    placement: Map<SampleFile, Folder>,
    fileBounds: Map<SampleFile, Rect>,
    folderBounds: Map<Folder, Rect>,
): Folder? {
    val dragged = state.draggedItem ?: return null
    if (state.hoveredDropTargetKey != null) return null
    val start = fileBounds[dragged.data] ?: return null
    val center: Offset = start.center + dragged.dragAmount
    return folderBounds.entries
        .firstOrNull { (folder, rect) ->
        val count = placement.count { it.value == folder }
        rect.contains(center) && rejectReason(folder, dragged.data, count) != null
    }?.key
}
