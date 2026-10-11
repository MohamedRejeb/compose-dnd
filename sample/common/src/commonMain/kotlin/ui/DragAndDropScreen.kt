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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.DemoScaffold
import components.OptionToggle
import components.OptionsRow
import components.dragPlaceholder
import components.lifted
import components.wellState
import navigation.Demo

private const val DragAndDropCode = """
val dnd = rememberDragAndDropState<FileItem>()

FileRow(file, Modifier.draggableItem(
    key = file.name,
    data = file,
    state = dnd,
    draggableContent = { FileRow(file) },
))

val dragged = dnd.draggedItem?.data
TransferFolderWell(folder, Modifier.dropTarget(
    key = folder,
    state = dnd,
    canDrop = dragged?.folder != folder,
    onDrop = { moveFile(it.data, folder) },
))
"""

@Composable
fun DragAndDropScreen(onBack: () -> Unit) {
    var dragAfterLongPress by remember { mutableStateOf(false) }
    var dropAnimationEnabled by remember { mutableStateOf(true) }

    DemoScaffold(
        title = Demo.DragAndDrop.title,
        hint = "Drag a file into the other folder, then bring it back.",
        code = DragAndDropCode,
        onBack = onBack,
    ) { insets ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            OptionsRow {
                OptionToggle(
                    label = "Long press to drag",
                    checked = dragAfterLongPress,
                    onCheckedChange = { dragAfterLongPress = it },
                )
                OptionToggle(
                    label = "Drop animation",
                    checked = dropAnimationEnabled,
                    onCheckedChange = { dropAnimationEnabled = it },
                )
            }
            FolderBoard(
                dragAfterLongPress = dragAfterLongPress,
                dropAnimationEnabled = dropAnimationEnabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FolderBoard(
    dragAfterLongPress: Boolean,
    dropAnimationEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val dndState = rememberDragAndDropState<FileItem>(dragAfterLongPress = dragAfterLongPress)
    var files by remember { mutableStateOf(InitialFiles) }

    DragAndDropContainer(state = dndState, modifier = modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            TransferFolder.entries.forEach { folder ->
                FolderDropTarget(
                    folder = folder,
                    files = files.filter { it.folder == folder },
                    dndState = dndState,
                    dropAnimationEnabled = dropAnimationEnabled,
                    onDrop = { file -> files = files.moveTo(file, folder) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FolderDropTarget(
    folder: TransferFolder,
    files: List<FileItem>,
    dndState: DragAndDropState<FileItem>,
    dropAnimationEnabled: Boolean,
    onDrop: (FileItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dragged = dndState.draggedItem?.data
    // A file can only go to a folder it is not in yet.
    val canDrop = dragged != null && dragged.folder != folder

    TransferFolderWell(
        folder = folder,
        fileCount = files.size,
        state = wellState(
            dragging = dragged != null,
            hovered = dndState.isDropTargetHovered(folder),
            canDrop = canDrop,
        ),
        modifier = modifier
            .fillMaxWidth()
            .dropTarget(
                key = folder,
                state = dndState,
                canDrop = canDrop,
                dropAnimationEnabled = dropAnimationEnabled,
                // Land the file where it will appear: the next free row.
                dropAlignment = Alignment.TopCenter,
                dropOffset = Offset(0f, nextRowTopPx(files.size)),
                onDrop = { onDrop(it.data) },
            ),
    ) {
        files.forEach { file ->
            key(file.name) {
                DraggableFile(file = file, dndState = dndState)
            }
        }
    }
}

@Composable
private fun DraggableFile(
    file: FileItem,
    dndState: DragAndDropState<FileItem>,
) {
    FileRow(
        file = file,
        modifier = Modifier
            .dragPlaceholder(active = dndState.isDragging(file.name))
            .draggableItem(
                key = file.name,
                data = file,
                state = dndState,
                draggableContent = {
                    FileRow(file = file, modifier = Modifier.lifted())
                },
            ),
    )
}

/** Distance from the top of a folder well to the row a new file lands in. */
@Composable
private fun nextRowTopPx(fileCount: Int): Float {
    val firstRowTop = FolderPadding + FolderHeaderHeight + FileRowGap
    val top = firstRowTop + (FileRowHeight + FileRowGap) * fileCount
    return with(LocalDensity.current) { top.toPx() }
}
