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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScaffold
import components.SectionLabel
import components.SegmentedControl
import components.dragPlaceholder
import components.lifted
import navigation.Demo
import theme.SampleTheme

/** The four scroll containers `dragAutoScroll` supports. */
internal enum class AutoScrollLayout(
    val label: String,
    val code: String,
) {
    Row("Row", AutoScrollRowCode),
    Grid("Grid", AutoScrollGridCode),
    Masonry("Masonry", AutoScrollMasonryCode),
    Column("Column", AutoScrollColumnCode),
}

private val Gutter = 16.dp
private val Gap = 8.dp
private const val PhotoTilt = 3f
private const val CardTilt = 3f

@Composable
fun AutoScrollDemoScreen(
    onBack: () -> Unit,
) {
    AutoScrollDemoScreen(onBack = onBack, initialLayout = AutoScrollLayout.Row)
}

/** Opens the screen on a given layout, for previews and snapshots. */
@Composable
internal fun AutoScrollDemoScreen(
    onBack: () -> Unit,
    initialLayout: AutoScrollLayout,
) {
    var layout by remember { mutableStateOf(initialLayout) }

    DemoScaffold(
        title = Demo.AutoScroll.title,
        hint = "Drag an item to the edge and the list scrolls.",
        code = layout.code,
        onBack = onBack,
    ) { insets ->
        Column(modifier = Modifier.fillMaxSize()) {
            SegmentedControl(
                options = AutoScrollLayout.entries,
                selected = layout,
                onSelect = { layout = it },
                label = { it.label },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Gutter),
            )
            val contentPadding = PaddingValues(
                start = Gutter,
                end = Gutter,
                top = 12.dp,
                bottom = insets.calculateBottomPadding() + Gutter,
            )
            Box(modifier = Modifier.weight(1f)) {
                when (layout) {
                    AutoScrollLayout.Row -> ProductRow(contentPadding)
                    AutoScrollLayout.Grid -> AppGrid(contentPadding)
                    AutoScrollLayout.Masonry -> PhotoMasonry(contentPadding)
                    AutoScrollLayout.Column -> TaskColumn(contentPadding)
                }
            }
        }
    }
}

/** Moves [item] to where [anchor] is, the way every mode on this screen reorders. */
private fun <T> List<T>.withItemMovedTo(anchor: T, item: T): List<T> {
    val index = indexOf(anchor)
    if (index == -1) return this
    val without = this - item
    return without.take(index) + item + without.drop(index)
}

/**
 * Makes an item reorderable in its list: it leaves a placeholder behind and
 * takes the spot of whatever item it is dragged over.
 */
@Composable
private fun <T> Modifier.reorderTile(
    item: T,
    key: Any,
    state: DragAndDropState<T>,
    onMove: (dragged: T) -> Unit,
    shape: Shape = SampleTheme.shapes.item,
    draggableContent: @Composable () -> Unit,
): Modifier =
    dragPlaceholder(active = state.isDragging(key), shape = shape)
        .reorderableItem(
            key = key,
            data = item,
            state = state,
            onDragEnter = { dragged -> onMove(dragged.data) },
            draggableContent = draggableContent,
        )

@Composable
private fun ProductRow(contentPadding: PaddingValues) {
    val dndState = rememberDragAndDropState<ScrollProduct>()
    var products by remember { mutableStateOf(InitialScrollProducts) }
    val listState = rememberLazyListState()

    DragAndDropContainer(state = dndState, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding()),
        ) {
            SectionLabel(
                text = "New arrivals",
                trailing = "${products.size} products",
                modifier = Modifier.padding(start = Gutter, bottom = 12.dp),
            )
            // A single row of large cards, centered in the space left, reads as a carousel.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = contentPadding.calculateBottomPadding() + 48.dp),
            ) {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(Gap),
                    contentPadding = PaddingValues(horizontal = Gutter),
                    modifier = Modifier
                        .fillMaxWidth()
                        .dragAutoScroll(state = dndState, lazyListState = listState),
                ) {
                    listItems(products, key = { it.name }) { product ->
                        ScrollProductCard(
                            product = product,
                            modifier = Modifier
                                .animateItem()
                                .reorderTile(
                                    item = product,
                                    key = product.name,
                                    state = dndState,
                                    onMove = { dragged -> products = products.withItemMovedTo(product, dragged) },
                                    draggableContent = {
                                        ScrollProductCard(product, Modifier.lifted(tilt = CardTilt))
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGrid(contentPadding: PaddingValues) {
    val dndState = rememberDragAndDropState<ScrollApp>()
    var apps by remember { mutableStateOf(InitialScrollApps) }
    val gridState = rememberLazyGridState()

    DragAndDropContainer(state = dndState, modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            state = gridState,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(Gap),
            horizontalArrangement = Arrangement.spacedBy(Gap),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(state = dndState, lazyGridState = gridState),
        ) {
            gridItems(apps, key = { it.name }) { app ->
                ScrollAppTile(
                    app = app,
                    modifier = Modifier
                        .animateItem()
                        .reorderTile(
                            item = app,
                            key = app.name,
                            state = dndState,
                            onMove = { dragged -> apps = apps.withItemMovedTo(app, dragged) },
                            draggableContent = { ScrollAppTile(app, Modifier.lifted()) },
                        ),
                )
            }
        }
    }
}

@Composable
private fun PhotoMasonry(contentPadding: PaddingValues) {
    val dndState = rememberDragAndDropState<ScrollPhoto>()
    var photos by remember { mutableStateOf(InitialScrollPhotos) }
    val gridState = rememberLazyStaggeredGridState()

    DragAndDropContainer(state = dndState, modifier = Modifier.fillMaxSize()) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            state = gridState,
            contentPadding = contentPadding,
            verticalItemSpacing = Gap,
            horizontalArrangement = Arrangement.spacedBy(Gap),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(state = dndState, lazyStaggeredGridState = gridState),
        ) {
            staggeredItems(photos, key = { it.place }) { photo ->
                ScrollPhotoTile(
                    photo = photo,
                    modifier = Modifier
                        .animateItem()
                        .reorderTile(
                            item = photo,
                            key = photo.place,
                            state = dndState,
                            onMove = { dragged -> photos = photos.withItemMovedTo(photo, dragged) },
                            draggableContent = { ScrollPhotoTile(photo, Modifier.lifted(tilt = PhotoTilt)) },
                        ),
                )
            }
        }
    }
}

@Composable
private fun TaskColumn(contentPadding: PaddingValues) {
    val dndState = rememberDragAndDropState<ScrollTask>()
    var tasks by remember { mutableStateOf(InitialScrollTasks) }
    val scrollState = rememberScrollState()

    DragAndDropContainer(state = dndState, modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Gap),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .dragAutoScroll(
                    state = dndState,
                    scrollState = scrollState,
                    orientation = Orientation.Vertical,
                ).padding(contentPadding),
        ) {
            tasks.forEach { task ->
                key(task.title) {
                    ScrollTaskRow(
                        task = task,
                        modifier = Modifier.reorderTile(
                            item = task,
                            key = task.title,
                            state = dndState,
                            onMove = { dragged -> tasks = tasks.withItemMovedTo(task, dragged) },
                            draggableContent = { ScrollTaskRow(task, Modifier.lifted()) },
                        ),
                    )
                }
            }
        }
    }
}
