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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import components.DropWell
import components.WellState
import components.lifted
import components.wellState
import theme.SampleTheme

/** Scale of a selected photo inside its cell, leaving room for the ring. */
private const val SelectedInset = 0.84f
private const val StackTilt = -3f
private const val StackFanDegrees = 7f
private const val MaxStackLayers = 3
private const val EmptyBadgeAlpha = 0.2f

/** The carried stack is smaller than a cell, so the album under it stays readable. */
private const val StackScale = 0.78f

private val RingWidth = 2.dp
private val BadgeSize = 22.dp
private val CoverSize = 44.dp

/**
 * A photo cell. Selected photos shrink inside an accent ring and show a
 * check; while anything is selected, the others show an empty badge.
 */
@Composable
internal fun PhotoTile(
    photo: GalleryPhoto,
    isSelected: Boolean,
    showBadge: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = SampleTheme.shapes.small
    val inset by animateFloatAsState(if (isSelected) SelectedInset else 1f)

    Box(modifier = modifier.aspectRatio(1f)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = inset
                    scaleY = inset
                }.clip(shape)
                .photoArt(photo),
        )
        if (isSelected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(RingWidth, SampleTheme.colors.accent, shape),
            )
        }
        if (showBadge) {
            SelectBadge(
                isSelected = isSelected,
                ring = photo.hue.container,
                fill = photo.hue.onContainer,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp),
            )
        }
    }
}

@Composable
private fun SelectBadge(
    isSelected: Boolean,
    ring: Color,
    fill: Color,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    val shape = SampleTheme.shapes.pill
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(BadgeSize)
            .clip(shape)
            .background(if (isSelected) colors.accent else fill.copy(alpha = EmptyBadgeAlpha))
            .border(1.5.dp, if (isSelected) colors.surface else ring, shape),
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

/**
 * An album that takes the carried selection. It shows a dashed outline while
 * photos are in flight, and lights up with the incoming count under the stack.
 * The trash lights up in danger instead.
 */
@Composable
internal fun AlbumWell(
    album: PhotoAlbum,
    isMoving: Boolean,
    isHovered: Boolean,
    incomingCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    val isTrash = album.key == TrashAlbumKey
    val state = if (isHovered && isTrash) WellState.Reject else wellState(isMoving, isHovered)
    val highlight = if (isTrash) colors.danger else colors.accent

    DropWell(
        state = state,
        shape = SampleTheme.shapes.item,
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
        ) {
            AlbumCover(album)
            Spacer(Modifier.height(8.dp))
            Text(
                text = album.name,
                style = MaterialTheme.typography.labelLarge,
                color = colors.ink,
                maxLines = 1,
            )
            Text(
                text = if (isHovered) "+$incomingCount" else photoCountLabel(album.photos.size),
                style = MaterialTheme.typography.labelMedium,
                color = if (isHovered) highlight else colors.inkMuted,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun AlbumCover(album: PhotoAlbum) {
    val colors = SampleTheme.colors
    val shape = SampleTheme.shapes.small
    val cover = album.photos.lastOrNull()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(CoverSize)
            .clip(shape)
            .background(colors.surface)
            .then(if (cover != null) Modifier.photoArt(cover) else Modifier.border(1.dp, colors.line, shape)),
    ) {
        if (cover == null) {
            Icon(
                imageVector = album.icon,
                contentDescription = null,
                tint = colors.inkMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun photoCountLabel(count: Int): String = if (count == 1) "1 photo" else "$count photos"

/** The line above the grid: the library size, or the selection and a way to clear it. */
@Composable
internal fun SelectionBar(
    photoCount: Int,
    selectionCount: Int,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp),
    ) {
        if (selectionCount == 0) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
            )
            Text(
                text = photoCountLabel(photoCount),
                style = MaterialTheme.typography.labelMedium,
                color = colors.inkMuted,
            )
        } else {
            Text(
                text = "$selectionCount selected",
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink,
            )
            ClearButton(onClick = onClear)
        }
    }
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    val colors = SampleTheme.colors
    val shape = SampleTheme.shapes.pill
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(
            text = "Clear",
            style = MaterialTheme.typography.labelLarge,
            color = colors.ink,
        )
    }
}

/**
 * The drag shadow of a group drag: the last few selected photos fanned out
 * like prints, with a badge counting the whole selection.
 */
@Composable
internal fun SelectionStack(photos: List<GalleryPhoto>) {
    val colors = SampleTheme.colors
    val shape = SampleTheme.shapes.small
    val layers = photos.takeLast(MaxStackLayers)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = StackScale
                scaleY = StackScale
            }.lifted(shape = shape, tilt = StackTilt),
    ) {
        layers.forEachIndexed { index, photo ->
            val depth = layers.lastIndex - index
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = depth * StackFanDegrees }
                    .clip(shape)
                    .background(colors.surface)
                    .padding(3.dp)
                    .clip(SampleTheme.shapes.small)
                    .photoArt(photo),
            )
        }
        CountBadge(
            count = photos.size,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp),
        )
    }
}

@Composable
private fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = 28.dp, minHeight = 28.dp)
            .clip(SampleTheme.shapes.pill)
            .background(colors.accent)
            .border(2.dp, colors.surface, SampleTheme.shapes.pill)
            .padding(horizontal = 6.dp),
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelLarge,
            color = colors.onAccent,
        )
    }
}
