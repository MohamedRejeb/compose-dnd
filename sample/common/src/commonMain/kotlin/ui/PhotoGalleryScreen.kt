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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.CircleIconButton
import components.DemoScaffold
import components.dragPlaceholder
import navigation.Demo
import theme.SampleTheme

private val GalleryCode = """
    // Every photo is draggable and a target
    Modifier
      .draggableItem(
        key = photo.id,
        data = photo.id,
        state = state,
        onDragStart = { selectOrMove(it.data) },
        draggableContent = { SelectionStack() },
      )
      .dropTarget(
        key = photo.id,
        state = state,
        onDragEnter = { extendSelectionTo(photo) },
      )
""".trimIndent()

private val TileMinSize = 84.dp
private val TileSpacing = 6.dp
private val GutterWidth = 16.dp

/**
 * A photo library built on one set of draggable items:
 * - Drag to select: long-press a photo and drag across the grid to select the
 *   range. Each photo is also a drop target; entering one extends the
 *   selection. That drag has no visible shadow.
 * - Group drag: long-press a selected photo to carry the whole selection as a
 *   stack onto an album.
 *
 * The mode is picked in onDragStart, from whether the pressed photo is selected.
 */
@Composable
fun PhotoGalleryScreen(
    onBack: () -> Unit,
) {
    val gallery = remember { GalleryState() }

    DemoScaffold(
        title = Demo.Photos.title,
        hint = "Long-press and drag across photos to select them, then drop them on an album.",
        code = GalleryCode,
        onBack = onBack,
        actions = {
            CircleIconButton(
                icon = Icons.Rounded.Refresh,
                contentDescription = "Reset gallery",
                onClick = gallery::reset,
            )
        },
    ) { insets ->
        PhotoGalleryContent(
            gallery = gallery,
            bottomPadding = insets,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PhotoGalleryContent(
    gallery: GalleryState,
    bottomPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val state = rememberDragAndDropState<Int>(dragAfterLongPress = true)
    val gridState = rememberLazyGridState()
    val isMoving = gallery.dragMode == GalleryDragMode.Move

    DragAndDropContainer(
        state = state,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.padding(horizontal = GutterWidth)) {
            AlbumRow(
                albums = gallery.albums,
                state = state,
                isMoving = isMoving,
                selectionSize = gallery.selection.size,
                onDropOnAlbum = gallery::moveSelectionTo,
            )
            SelectionBar(
                photoCount = gallery.photos.size,
                selectionCount = gallery.selection.size,
                onClear = gallery::clearSelection,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = TileMinSize),
                state = gridState,
                horizontalArrangement = Arrangement.spacedBy(TileSpacing),
                verticalArrangement = Arrangement.spacedBy(TileSpacing),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = bottomPadding.calculateBottomPadding() + GutterWidth,
                ),
                modifier = Modifier
                    .weight(1f)
                    // Scroll while drag-selecting, not while carrying photos to an album
                    .then(if (isMoving) Modifier else Modifier.dragAutoScroll(state, gridState)),
            ) {
                items(gallery.photos, key = { it.id }) { photo ->
                    GalleryGridItem(photo = photo, gallery = gallery, state = state)
                }
            }
        }
    }
}

@Composable
private fun LazyGridItemScope.GalleryGridItem(
    photo: GalleryPhoto,
    gallery: GalleryState,
    state: DragAndDropState<Int>,
) {
    val isSelected = photo.id in gallery.selection
    val isMoving = gallery.dragMode == GalleryDragMode.Move

    PhotoTile(
        photo = photo,
        isSelected = isSelected,
        showBadge = gallery.selection.isNotEmpty(),
        modifier = Modifier
            .animateItem()
            // The selection leaves the grid while it is carried to an album
            .dragPlaceholder(active = isMoving && isSelected, shape = SampleTheme.shapes.small)
            .semantics {
                contentDescription = "Photo ${photo.id + 1}"
                selected = isSelected
            }.clickable { gallery.toggle(photo.id) }
            .draggableItem(
                key = photo.id,
                data = photo.id,
                state = state,
                dropStrategy = DropStrategy.CenterDistance,
                onDragStart = { gallery.onDragStart(it.data) },
                onDragEnd = { gallery.onDragEnd() },
                draggableContent = {
                    // Drag-select has no shadow, only a group drag shows the stack
                    if (gallery.dragMode == GalleryDragMode.Move) {
                        SelectionStack(photos = gallery.photos.filter { it.id in gallery.selection })
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

@Composable
private fun AlbumRow(
    albums: List<PhotoAlbum>,
    state: DragAndDropState<Int>,
    isMoving: Boolean,
    selectionSize: Int,
    onDropOnAlbum: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        albums.forEach { album ->
            val key = "album-${album.key}"
            AlbumWell(
                album = album,
                isMoving = isMoving,
                isHovered = isMoving && state.isDropTargetHovered(key),
                incomingCount = selectionSize,
                modifier = Modifier
                    .weight(1f)
                    .dropTarget(
                        key = key,
                        state = state,
                        canDrop = isMoving,
                        onDrop = { onDropOnAlbum(album.key) },
                    ),
            )
        }
    }
}
