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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScreenScaffold

@Composable
fun DragHandleReorderScreen(
    onBack: () -> Unit,
) {
    DemoScreenScaffold(
        title = "Drag Handle Reorder",
        onBack = onBack,
    ) { paddingValues ->
        DragHandleReorderContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        )
    }
}

@OptIn(ExperimentalDndApi::class)
@Composable
private fun DragHandleReorderContent(
    modifier: Modifier = Modifier,
) {
    val reorderState = rememberReorderState<String>()
    var items by remember {
        mutableStateOf(
            listOf(
                "Item 1",
                "Item 2",
                "Item 3",
                "Item 4",
                "Item 5",
                "Item 6",
                "Item 7",
                "Item 8",
            )
        )
    }

    val lazyListState = rememberLazyListState()

    ReorderContainer(
        state = reorderState,
        modifier = modifier,
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = reorderState.dndState,
                    lazyListState = lazyListState,
                ),
        ) {
            items(items, key = { it }) { item ->
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
                        DragHandleListItem(
                            text = item,
                            isDragShadow = true,
                        )
                    },
                    modifier = Modifier,
                ) {
                    DragHandleListItem(
                        text = item,
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            },
                        handleModifier = Modifier.dragHandle(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DragHandleListItem(
    text: String,
    isDragShadow: Boolean = false,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(
                if (isDragShadow) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
    ) {
        Icon(
            Icons.Rounded.DragHandle,
            contentDescription = "Drag handle",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = handleModifier.size(24.dp),
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
        )
    }
}
