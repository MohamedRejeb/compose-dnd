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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.DropWell
import components.ItemSurface
import components.WellState
import theme.Hue
import theme.Hues
import theme.SampleTheme

/** Inner padding of a folder well. */
internal val FolderPadding = 12.dp

/** Height of the folder title row. */
internal val FolderHeaderHeight = 32.dp

/** Height of one file row. */
internal val FileRowHeight = 60.dp

/** Vertical gap between the rows of a folder. */
internal val FileRowGap = 8.dp

private val FileIconSize = 40.dp

internal enum class TransferFolder(
    val title: String
) {
    Downloads("Downloads"),
    Documents("Documents"),
}

internal enum class TransferFileKind(
    val icon: ImageVector,
    val hue: Hue
) {
    Pdf(Icons.Rounded.PictureAsPdf, Hues.Rose),
    Image(Icons.Rounded.Image, Hues.Sky),
    Text(Icons.Rounded.Description, Hues.Butter),
    Sheet(Icons.Rounded.TableChart, Hues.Mint),
}

@Immutable
internal data class FileItem(
    val name: String,
    val size: String,
    val kind: TransferFileKind,
    val folder: TransferFolder,
)

internal val InitialFiles = listOf(
    FileItem("Invoice.pdf", "248 KB", TransferFileKind.Pdf, TransferFolder.Downloads),
    FileItem("Beach.jpg", "3.1 MB", TransferFileKind.Image, TransferFolder.Downloads),
    FileItem("Notes.txt", "4 KB", TransferFileKind.Text, TransferFolder.Downloads),
    FileItem("Budget.xlsx", "36 KB", TransferFileKind.Sheet, TransferFolder.Documents),
)

/** Returns a new list where [file] sits at the end of [folder]. */
internal fun List<FileItem>.moveTo(file: FileItem, folder: TransferFolder): List<FileItem> =
    filterNot { it.name == file.name } + file.copy(folder = folder)

/** A folder drawn as a drop well: a title row, then its files. */
@Composable
internal fun TransferFolderWell(
    folder: TransferFolder,
    fileCount: Int,
    state: WellState,
    modifier: Modifier = Modifier,
    files: @Composable ColumnScope.() -> Unit,
) {
    DropWell(state = state, modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FileRowGap),
            modifier = Modifier
                .fillMaxSize()
                .padding(FolderPadding),
        ) {
            FolderHeader(folder = folder, fileCount = fileCount)
            if (fileCount == 0) {
                EmptyFolder(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                files()
            }
        }
    }
}

@Composable
private fun FolderHeader(
    folder: TransferFolder,
    fileCount: Int,
) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(FolderHeaderHeight)
            .padding(horizontal = 4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = null,
            tint = colors.inkMuted,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = folder.title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = if (fileCount == 1) "1 file" else "$fileCount files",
            style = MaterialTheme.typography.labelMedium,
            color = colors.inkMuted,
        )
    }
}

@Composable
private fun EmptyFolder(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Text(
            text = "Drop files here",
            style = MaterialTheme.typography.bodyMedium,
            color = SampleTheme.colors.inkMuted,
        )
    }
}

/** One file: a hue tile with the file type icon, the name and the size. */
@Composable
internal fun FileRow(
    file: FileItem,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    ItemSurface(modifier = modifier.fillMaxWidth().height(FileRowHeight)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
        ) {
            FileIcon(file.kind)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = file.size,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
        }
    }
}

@Composable
private fun FileIcon(kind: TransferFileKind) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(FileIconSize)
            .clip(SampleTheme.shapes.small)
            .background(kind.hue.container),
    ) {
        Icon(
            imageVector = kind.icon,
            contentDescription = null,
            tint = kind.hue.onContainer,
            modifier = Modifier.size(22.dp),
        )
    }
}
