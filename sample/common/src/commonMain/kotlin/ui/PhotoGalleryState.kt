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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import theme.Hue
import theme.Hues

private const val PhotoCount = 60
internal const val TrashAlbumKey = "trash"

internal enum class GalleryDragMode { None, Select, Move }

/** The landscape drawn on a photo tile. */
internal enum class PhotoScene { Hills, Peaks, Sea }

/**
 * A photo in the library, drawn as a small one-hue landscape.
 *
 * @param sun where the sun sits, as fractions of the photo's width and height.
 * @param mirrored whether the landscape is flipped horizontally.
 */
@Immutable
internal data class GalleryPhoto(
    val id: Int,
    val hue: Hue,
    val scene: PhotoScene,
    val sun: Offset,
    val mirrored: Boolean,
)

@Immutable
internal data class PhotoAlbum(
    val key: String,
    val name: String,
    val icon: ImageVector,
    val photos: List<GalleryPhoto> = emptyList(),
)

/** A cheap, stable scramble so neighbouring photos differ without looking patterned. */
private fun scramble(id: Int): Int = (id * 2654435761L ushr 7).toInt() and Int.MAX_VALUE

/**
 * Picks a hue for each photo that differs from the photos to its left and
 * above it, whether the grid shows four or five columns.
 */
private fun spreadHues(count: Int): List<Hue> =
    (0 until count).fold(emptyList()) { picked, id ->
        val neighbours = NeighbourOffsets.mapNotNull { picked.getOrNull(id - it) }
        val start = scramble(id)
        val hue = (0 until Hues.all.size)
            .map { Hues.at(start + it) }
            .firstOrNull { it !in neighbours } ?: Hues.at(start)
        picked + hue
    }

/** Photos that sit next to or above a photo in a four or five column grid. */
private val NeighbourOffsets = listOf(1, 3, 4, 5)

private fun galleryPhoto(id: Int, hue: Hue): GalleryPhoto {
    val seed = scramble(id + PhotoCount)
    return GalleryPhoto(
        id = id,
        hue = hue,
        scene = PhotoScene.entries[seed % PhotoScene.entries.size],
        // Keep the sun clear of the selection badge in the top-left corner
        sun = Offset(
            x = 0.45f + (seed % 36) / 100f,
            y = 0.2f + (seed / 36 % 16) / 100f,
        ),
        mirrored = seed / 7 % 2 == 1,
    )
}

private fun initialPhotos(): List<GalleryPhoto> =
    spreadHues(PhotoCount).mapIndexed { id, hue -> galleryPhoto(id, hue) }

private fun initialAlbums(): List<PhotoAlbum> =
    listOf(
        PhotoAlbum(key = "favorites", name = "Favorites", icon = Icons.Rounded.Favorite),
        PhotoAlbum(key = "travel", name = "Travel", icon = Icons.Rounded.Flight),
        PhotoAlbum(key = "pets", name = "Pets", icon = Icons.Rounded.Pets),
        PhotoAlbum(key = TrashAlbumKey, name = "Trash", icon = Icons.Rounded.Delete),
    )

@Stable
internal class GalleryState {
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
