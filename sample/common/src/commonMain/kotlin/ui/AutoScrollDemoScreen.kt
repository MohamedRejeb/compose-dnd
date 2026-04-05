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
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScreenScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoScrollDemoScreen(
    onBack: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("LazyRow", "Grid", "Scroll")

    DemoScreenScaffold(
        title = "Auto-Scroll Demo",
        onBack = onBack,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            when (selectedTab) {
                0 -> LazyRowReorderContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                )

                1 -> LazyGridReorderContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                )

                2 -> ScrollStateReorderContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                )
            }
        }
    }
}

// -- LazyRow --

@OptIn(ExperimentalDndApi::class)
@Composable
private fun LazyRowReorderContent(modifier: Modifier = Modifier) {
    val reorderState = rememberReorderState<String>()
    var items by remember { mutableStateOf((1..30).map { "item$it" }) }
    val lazyListState = rememberLazyListState()

    ReorderContainer(
        state = reorderState,
        modifier = modifier,
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .dragAutoScroll(
                    state = reorderState.dndState,
                    lazyListState = lazyListState,
                ),
        ) {
            items(items, key = { it }) { item ->
                val number = item.removePrefix("item")
                ReorderableItem(
                    state = reorderState,
                    key = item,
                    data = item,
                    onDrop = {},
                    onDragEnter = { state ->
                        items = items.toMutableList().apply {
                            val index = indexOf(item)
                            if (index == -1) return@ReorderableItem
                            remove(state.data)
                            add(index, state.data)
                        }
                    },
                    draggableContent = {
                        AutoScrollItem(
                            number = number,
                            isDragShadow = true,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .width(80.dp)
                                .fillMaxHeight(),
                        )
                    },
                    modifier = Modifier.animateItem(),
                ) {
                    AutoScrollItem(
                        number = number,
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                            .width(80.dp)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

// -- LazyGrid --

@OptIn(ExperimentalDndApi::class)
@Composable
private fun LazyGridReorderContent(modifier: Modifier = Modifier) {
    val reorderState = rememberReorderState<String>()
    var items by remember { mutableStateOf((1..40).map { "item$it" }) }
    val lazyGridState = rememberLazyGridState()

    ReorderContainer(
        state = reorderState,
        modifier = modifier,
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            state = lazyGridState,
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = reorderState.dndState,
                    lazyGridState = lazyGridState,
                ),
        ) {
            items(items, key = { it }) { item ->
                val number = item.removePrefix("item")
                ReorderableItem(
                    state = reorderState,
                    key = item,
                    data = item,
                    onDrop = {},
                    onDragEnter = { state ->
                        items = items.toMutableList().apply {
                            val index = indexOf(item)
                            if (index == -1) return@ReorderableItem
                            remove(state.data)
                            add(index, state.data)
                        }
                    },
                    draggableContent = {
                        AutoScrollItem(
                            number = number,
                            isDragShadow = true,
                            color = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                        )
                    },
                    modifier = Modifier.animateItem(),
                ) {
                    AutoScrollItem(
                        number = number,
                        color = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                            .fillMaxWidth()
                            .height(80.dp),
                    )
                }
            }
        }
    }
}

// -- ScrollState --

@OptIn(ExperimentalDndApi::class)
@Composable
private fun ScrollStateReorderContent(modifier: Modifier = Modifier) {
    val reorderState = rememberReorderState<String>()
    var items by remember { mutableStateOf((1..30).map { "item$it" }) }
    val scrollState = rememberScrollState()

    ReorderContainer(
        state = reorderState,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .dragAutoScroll(
                    state = reorderState.dndState,
                    scrollState = scrollState,
                    orientation = Orientation.Vertical,
                ),
        ) {
            items.forEach { item ->
                key(item) {
                    val number = item.removePrefix("item")
                    ReorderableItem(
                        state = reorderState,
                        key = item,
                        data = item,
                        onDrop = {},
                        onDragEnter = { state ->
                            items = items.toMutableList().apply {
                                val index = indexOf(item)
                                if (index == -1) return@ReorderableItem
                                remove(state.data)
                                add(index, state.data)
                            }
                        },
                        draggableContent = {
                            ScrollListItem(
                                number = number,
                                isDragShadow = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                            )
                        },
                        modifier = Modifier,
                    ) {
                        ScrollListItem(
                            number = number,
                            modifier = Modifier
                                .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                                .fillMaxWidth()
                                .height(56.dp),
                        )
                    }
                }
            }
        }
    }
}

// -- Item composables --

@Composable
private fun AutoScrollItem(
    number: String,
    color: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    isDragShadow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(
                if (isDragShadow) {
                    Modifier.shadow(16.dp, shape)
                } else {
                    Modifier
                }
            ).clip(shape)
            .background(color),
    ) {
        Text(
            text = "#$number",
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ScrollListItem(
    number: String,
    isDragShadow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(
                if (isDragShadow) {
                    Modifier.shadow(16.dp, shape)
                } else {
                    Modifier
                }
            ).clip(shape)
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "#$number",
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Scroll item $number",
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
