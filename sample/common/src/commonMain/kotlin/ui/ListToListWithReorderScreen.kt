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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScreenScaffold
import components.DndItemCard

@Composable
fun ListToListWithReorderScreen(
    onBack: () -> Unit,
) {
    DemoScreenScaffold(
        title = "List to List (Reorder)",
        onBack = onBack,
    ) { paddingValues ->
        ListToListWithReorderContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        )
    }
}

@OptIn(ExperimentalDndApi::class)
@Composable
private fun ListToListWithReorderContent(
    modifier: Modifier = Modifier,
) {
    var listOne by remember {
        mutableStateOf(
            listOf("item1", "item2", "item3", "item4")
        )
    }

    var listTwo by remember {
        mutableStateOf(
            listOf("item5", "item6", "item7", "item8")
        )
    }

    val reorderState = rememberReorderState<String>()

    val lazyListStateOne = rememberLazyListState()
    val lazyListStateTwo = rememberLazyListState()

    ReorderContainer(
        state = reorderState,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = lazyListStateOne,
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .dragAutoScroll(
                        state = reorderState.dndState,
                        lazyListState = lazyListStateOne,
                    ).dropTarget(
                        key = "listOne",
                        state = reorderState.dndState,
                        dropAnimationEnabled = false,
                        onDragEnter = { state ->
                            listTwo = listTwo.toMutableList().apply {
                                val isRemoved = remove(state.data)
                                if (!isRemoved) return@dropTarget
                            }
                            listOne = listOne.toMutableList().apply {
                                add(state.data)
                            }
                        },
                    ),
            ) {
                items(listOne, key = { it }) { item ->
                    ReorderableItem(
                        state = reorderState,
                        key = item,
                        data = item,
                        zIndex = 1f,
                        dropStrategy = DropStrategy.CenterDistance,
                        onDragEnter = { state ->
                            listOne = listOne.toMutableList().apply {
                                val index = indexOf(item)
                                if (index == -1) return@ReorderableItem
                                if (!remove(state.data)) {
                                    listTwo = listTwo.toMutableList().apply {
                                        remove(state.data)
                                    }
                                }
                                add(index, state.data)
                            }
                        },
                        draggableContent = {
                            DndItemCard(
                                label = item,
                                isDragShadow = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                            )
                        },
                        modifier = Modifier.animateItem(),
                    ) {
                        DndItemCard(
                            label = item,
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                }.fillMaxWidth()
                                .height(60.dp),
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                state = lazyListStateTwo,
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .dragAutoScroll(
                        state = reorderState.dndState,
                        lazyListState = lazyListStateTwo,
                    ).dropTarget(
                        key = "listTwo",
                        state = reorderState.dndState,
                        dropAnimationEnabled = false,
                        onDragEnter = { state ->
                            listOne = listOne.toMutableList().apply {
                                val isRemoved = remove(state.data)
                                if (!isRemoved) return@dropTarget
                            }
                            listTwo = listTwo.toMutableList().apply {
                                add(state.data)
                            }
                        },
                    ),
            ) {
                items(listTwo, key = { it }) { item ->
                    ReorderableItem(
                        state = reorderState,
                        key = item,
                        data = item,
                        zIndex = 1f,
                        dropStrategy = DropStrategy.CenterDistance,
                        onDragEnter = { state ->
                            listTwo = listTwo.toMutableList().apply {
                                val index = indexOf(item)
                                if (index == -1) return@ReorderableItem
                                if (!remove(state.data)) {
                                    listOne = listOne.toMutableList().apply {
                                        remove(state.data)
                                    }
                                }
                                add(index, state.data)
                            }
                        },
                        draggableContent = {
                            DndItemCard(
                                label = item,
                                isDragShadow = true,
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp),
                            )
                        },
                        modifier = Modifier.animateItem(),
                    ) {
                        DndItemCard(
                            label = item,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
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
}
