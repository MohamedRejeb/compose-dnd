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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScreenScaffold

/**
 * A photo grid showing two gestures built on the same draggable items:
 * - Drag to select: long-press a photo and drag across the grid to select the
 *   range, like Google Photos. Each photo is also a drop target; entering one
 *   extends the selection. The drag has no visible shadow.
 * - Group drag: long-press a selected photo to drag the whole selection as a
 *   stack onto an album.
 *
 * The mode is picked in onDragStart, from whether the pressed photo is selected.
 */
@Composable
fun PhotoGalleryScreen(
    onBack: () -> Unit,
) {
    val gallery = remember { GalleryState() }

    DemoScreenScaffold(
        title = if (gallery.selection.isEmpty()) "Photo Gallery" else "${gallery.selection.size} selected",
        onBack = onBack,
        actions = {
            if (gallery.selection.isNotEmpty()) {
                TextButton(onClick = gallery::clearSelection) { Text("Clear") }
            }
            IconButton(onClick = gallery::reset) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset gallery")
            }
        },
    ) { paddingValues ->
        PhotoGalleryContent(
            gallery = gallery,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        )
    }
}

private const val PHOTO_COUNT = 60
private const val TRASH_KEY = "trash"

private enum class GalleryDragMode { None, Select, Move }

@Immutable
private data class Photo(
    val id: Int,
    val color: Color,
)

@Immutable
private data class Album(
    val key: String,
    val name: String,
    val icon: ImageVector,
    val photos: List<Photo> = emptyList(),
)

private fun initialPhotos(): List<Photo> =
    List(PHOTO_COUNT) { id ->
        Photo(id = id, color = Color.hsv(hue = (id * 37f) % 360f, saturation = 0.5f, value = 0.85f))
    }

private fun initialAlbums(): List<Album> =
    listOf(
        Album(key = "favorites", name = "Favorites", icon = Icons.Rounded.Favorite),
        Album(key = "travel", name = "Travel", icon = Icons.Rounded.Flight),
        Album(key = "pets", name = "Pets", icon = Icons.Rounded.Pets),
        Album(key = TRASH_KEY, name = "Trash", icon = Icons.Rounded.Delete),
    )

@Stable
private class GalleryState {
    var photos by mutableStateOf(initialPhotos())
        private set
    var albums by mutableStateOf(initialAlbums())
        private set
    var selection by mutableStateOf(emptySet<Int>())
        private set
    var dragMode by mutableStateOf(GalleryDragMode.None)
        private set

    /** Selection before the current drag-select started, and the photo it started on. */
    private var baseSelection = emptySet<Int>()
    private var anchorId: Int? = null

    fun toggle(id: Int) {
        selection = if (id in selection) selection - id else selection + id
    }

    fun clearSelection() {
        selection = emptySet()
    }

    fun reset() {
        photos = initialPhotos()
        albums = initialAlbums()
        selection = emptySet()
    }

    fun onDragStart(id: Int) {
        anchorId = id
        if (id in selection) {
            dragMode = GalleryDragMode.Move
        } else {
            dragMode = GalleryDragMode.Select
            baseSelection = selection
            selection = selection + id
        }
    }

    fun onDragEnd() {
        dragMode = GalleryDragMode.None
        anchorId = null
    }

    /** Drag-select: select every photo between the anchor and [id]. */
    fun extendSelectionTo(id: Int) {
        if (dragMode != GalleryDragMode.Select) return
        val anchorIndex = photos.indexOfFirst { it.id == anchorId }
        val index = photos.indexOfFirst { it.id == id }
        if (anchorIndex == -1 || index == -1) return

        val range = photos.subList(minOf(anchorIndex, index), maxOf(anchorIndex, index) + 1)
        selection = baseSelection + range.map { it.id }
    }

    fun moveSelectionTo(albumKey: String) {
        val moved = photos.filter { it.id in selection }
        photos = photos.filterNot { it.id in selection }
        albums = albums.map { album ->
            if (album.key == albumKey) album.copy(photos = album.photos + moved) else album
        }
        selection = emptySet()
    }
}

@Composable
private fun PhotoGalleryContent(
    gallery: GalleryState,
    modifier: Modifier = Modifier,
) {
    val state = rememberDragAndDropState<Int>(dragAfterLongPress = true)
    val gridState = rememberLazyGridState()
    val isMoving = gallery.dragMode == GalleryDragMode.Move

    DragAndDropContainer(
        state = state,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Long-press a photo and drag to select a range. Tap to toggle. " +
                    "Drag a selected photo onto an album to move the whole selection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )

            AlbumRow(
                albums = gallery.albums,
                state = state,
                acceptsDrops = isMoving,
                onDropOnAlbum = gallery::moveSelectionTo,
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 96.dp),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .weight(1f)
                    // Scroll while drag-selecting, not while carrying photos to an album
                    .then(if (isMoving) Modifier else Modifier.dragAutoScroll(state, gridState)),
            ) {
                items(gallery.photos, key = { it.id }) { photo ->
                    val isSelected = photo.id in gallery.selection

                    PhotoTile(
                        photo = photo,
                        isSelected = isSelected,
                        modifier = Modifier
                            .animateItem()
                            .graphicsLayer { alpha = if (isMoving && isSelected) 0.35f else 1f }
                            .clickable { gallery.toggle(photo.id) }
                            .draggableItem(
                                key = photo.id,
                                data = photo.id,
                                state = state,
                                dropStrategy = DropStrategy.CenterDistance,
                                onDragStart = { gallery.onDragStart(it.data) },
                                onDragEnd = { gallery.onDragEnd() },
                                draggableContent = {
                                    // Drag-select has no shadow, only a group drag shows one
                                    if (gallery.dragMode == GalleryDragMode.Move) {
                                        SelectionStack(
                                            photos = gallery.photos.filter { it.id in gallery.selection },
                                        )
                                    }
                                },
                            ).dropTarget(
                                key = photo.id,
                                state = state,
                                canDrop = gallery.dragMode == GalleryDragMode.Select,
                                dropAnimationEnabled = false,
                                onDragEnter = { gallery.extendSelectionTo(photo.id) },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumRow(
    albums: List<Album>,
    state: DragAndDropState<Int>,
    acceptsDrops: Boolean,
    onDropOnAlbum: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        albums.forEach { album ->
            val key = "album-${album.key}"
            AlbumChip(
                album = album,
                isHighlighted = acceptsDrops,
                isHovered = state.isDropTargetHovered(key),
                modifier = Modifier
                    .weight(1f)
                    .dropTarget(
                        key = key,
                        state = state,
                        canDrop = acceptsDrops,
                        onDrop = { onDropOnAlbum(album.key) },
                    ),
            )
        }
    }
}

@Composable
private fun AlbumChip(
    album: Album,
    isHighlighted: Boolean,
    isHovered: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = if (album.key == TRASH_KEY) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val scale by animateFloatAsState(if (isHovered) 1.08f else 1f)
    val shape = RoundedCornerShape(16.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }.clip(shape)
            .background(if (isHovered) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isHighlighted) 2.dp else 1.dp,
                color = if (isHighlighted) accent else MaterialTheme.colorScheme.outlineVariant,
                shape = shape,
            ).padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            val cover = album.photos.lastOrNull()
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cover?.color ?: accent.copy(alpha = 0.12f)),
            )
            Icon(
                imageVector = album.icon,
                contentDescription = null,
                tint = if (cover != null) Color.White else accent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
        Text(
            text = "${album.photos.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PhotoTile(
    photo: Photo,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    // Selected photos shrink inside their cell, like Google Photos
    val inset by animateFloatAsState(if (isSelected) 0.86f else 1f)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
            ),
    ) {
        PhotoArt(
            photo = photo,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = inset
                    scaleY = inset
                },
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
            )
        }
    }
}

@Composable
private fun PhotoArt(
    photo: Photo,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    listOf(photo.color, photo.color.copy(alpha = 0.55f)),
                ),
            ),
    ) {
        Text(
            text = "${photo.id + 1}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            modifier = Modifier.padding(6.dp),
        )
    }
}

/**
 * Drag shadow of a group drag: up to three photos fanned out, with a badge
 * counting the whole selection.
 */
@Composable
private fun SelectionStack(
    photos: List<Photo>,
) {
    Box(modifier = Modifier.size(96.dp)) {
        photos.takeLast(3).forEachIndexed { index, photo ->
            PhotoArt(
                photo = photo,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .graphicsLayer { rotationZ = (index - 1) * 8f }
                    .shadow(8.dp, RoundedCornerShape(8.dp)),
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(28.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        ) {
            Text(
                text = "${photos.size}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}
