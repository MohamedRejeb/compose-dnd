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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScreenScaffold
import components.DndItemCard

@Composable
fun ReorderListScreen(
    onBack: () -> Unit,
) {
    DemoScreenScaffold(
        title = "Reorder List",
        onBack = onBack,
    ) { paddingValues ->
        ReorderScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        )
    }
}

@OptIn(ExperimentalDndApi::class)
@Composable
private fun ReorderScreenContent(
    modifier: Modifier = Modifier,
) {
    val reorderState = rememberReorderState<String>()
    var items by remember {
        mutableStateOf(
            (1..50).map { "item$it" }
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
                        DndItemCard(
                            label = "#$number",
                            isDragShadow = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                        )
                    },
                    modifier = Modifier,
                ) {
                    DndItemCard(
                        label = "#$number",
                        modifier = Modifier
                            .graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }.fillMaxWidth()
                            .height(60.dp),
                    )
                }
            }
        }
    }
}
