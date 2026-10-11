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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Image
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import components.DropWell
import components.ItemSurface
import components.WellState
import theme.Hue
import theme.Hues
import theme.SampleTheme

/** How many files a folder holds before it rejects more. */
internal const val FolderCapacity = 3

internal enum class FileKind(
    val label: String,
    val plural: String,
    val icon: ImageVector,
    val hue: Hue,
) {
    Image("Image", "images", Icons.Rounded.Image, Hues.Sky),
    Document("Document", "documents", Icons.Rounded.Description, Hues.Peach),
}

@Immutable
internal data class SampleFile(
    val name: String,
    val kind: FileKind,
)

internal enum class Folder(
    val title: String,
    val accepts: FileKind,
) {
    Photos("Photos", FileKind.Image),
    Documents("Documents", FileKind.Document),
}

internal val SampleFiles = listOf(
    SampleFile("beach.jpg", FileKind.Image),
    SampleFile("invoice.pdf", FileKind.Document),
    SampleFile("skyline.png", FileKind.Image),
    SampleFile("resume.pdf", FileKind.Document),
    SampleFile("portrait.jpg", FileKind.Image),
    SampleFile("notes.txt", FileKind.Document),
    SampleFile("sunset.jpg", FileKind.Image),
)

/** Why [folder] refuses [file] when it already holds [count] files, or null when it accepts it. */
internal fun rejectReason(folder: Folder, file: SampleFile, count: Int): String? = when {
    file.kind != folder.accepts -> "Only ${folder.accepts.plural} go here"
    count >= FolderCapacity -> "${folder.title} is full"
    else -> null
}

internal fun Folder.accepts(file: SampleFile, count: Int): Boolean = rejectReason(this, file, count) == null

private val FileIconSize = 36.dp
private val FolderFileRowHeight = 40.dp
private val FolderFileIconSize = 24.dp
private const val IconGlyphRatio = 0.55f
private val FolderIconSize = 28.dp
private val StatusHeight = 32.dp
private val RejectOutline = 2.dp

@Composable
internal fun FileCard(
    file: SampleFile,
    modifier: Modifier = Modifier,
    rejected: Boolean = false,
) {
    val shape = SampleTheme.shapes.item
    val outline = if (rejected) {
        Modifier.border(RejectOutline, SampleTheme.colors.danger, shape)
    } else {
        Modifier
    }
    ItemSurface(shape = shape, modifier = modifier.then(outline)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(10.dp),
        ) {
            HueTile(icon = file.kind.icon, hue = file.kind.hue, size = FileIconSize)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = SampleTheme.colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = file.kind.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = SampleTheme.colors.inkMuted,
                )
            }
        }
    }
}

@Composable
private fun HueTile(
    icon: ImageVector,
    hue: Hue,
    size: Dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(SampleTheme.shapes.small)
            .background(hue.container),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = hue.onContainer,
            modifier = Modifier.size(size * IconGlyphRatio),
        )
    }
}

/**
 * A folder drawn as a [DropWell]: title, rule and files at the top, and at the
 * bottom, while a file hovers it, "Release to add" or the reason it says no.
 */
@Composable
internal fun FolderWell(
    folder: Folder,
    files: List<SampleFile>,
    state: WellState,
    reason: String?,
    modifier: Modifier = Modifier,
) {
    DropWell(state = state, modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(12.dp),
        ) {
            FolderHeader(folder = folder)
            FolderRule(folder = folder, count = files.size)
            files.forEach { file -> FolderFileRow(file) }
            Spacer(Modifier.weight(1f))
            FolderStatus(state = state, reason = reason)
        }
    }
}

@Composable
private fun FolderHeader(folder: Folder) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HueTile(icon = Icons.Rounded.Folder, hue = folder.accepts.hue, size = FolderIconSize)
        Spacer(Modifier.width(8.dp))
        Text(
            text = folder.title,
            style = MaterialTheme.typography.titleMedium,
            color = SampleTheme.colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** What the folder takes, and how full it is. */
@Composable
private fun FolderRule(
    folder: Folder,
    count: Int,
) {
    val colors = SampleTheme.colors
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text(
            text = "${folder.accepts.label}s only",
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$count of $FolderCapacity",
            style = MaterialTheme.typography.labelMedium,
            color = if (count >= FolderCapacity) colors.ink else colors.inkMuted,
        )
    }
}

@Composable
private fun FolderStatus(
    state: WellState,
    reason: String?,
) {
    val colors = SampleTheme.colors
    val (text, color) = when (state) {
        WellState.Reject -> (reason ?: "Not allowed here") to colors.danger
        WellState.Accept -> "Release to add" to colors.accent
        else -> "" to colors.inkMuted
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth().height(StatusHeight),
    ) {
        if (state == WellState.Reject) {
            Icon(
                imageVector = Icons.Rounded.Block,
                contentDescription = null,
                tint = colors.danger,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FolderFileRow(file: SampleFile) {
    ItemSurface(
        shape = SampleTheme.shapes.small,
        modifier = Modifier
            .fillMaxWidth()
            .height(FolderFileRowHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp).align(Alignment.CenterStart),
        ) {
            HueTile(icon = file.kind.icon, hue = file.kind.hue, size = FolderFileIconSize)
            Spacer(Modifier.width(8.dp))
            Text(
                text = file.name,
                style = MaterialTheme.typography.labelMedium,
                color = SampleTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
