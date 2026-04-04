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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.DemoScreenScaffold
import components.DndDropZone
import components.DndSettingsDrawer
import components.RedBox
import kotlinx.coroutines.launch

@Composable
fun ItemToItemTwoDirectionsScreen(
    onBack: () -> Unit,
) {
    var dragAfterLongPress by remember { mutableStateOf(false) }
    var requireFirstDownUnconsumed by remember { mutableStateOf(false) }
    val drawerState = androidx.compose.material3.rememberDrawerState(
        initialValue = androidx.compose.material3.DrawerValue.Closed,
    )
    val scope = rememberCoroutineScope()

    DndSettingsDrawer(
        drawerState = drawerState,
        dragAfterLongPress = dragAfterLongPress,
        onDragAfterLongPressChange = { dragAfterLongPress = it },
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
        onRequireFirstDownUnconsumedChange = { requireFirstDownUnconsumed = it },
    ) {
        DemoScreenScaffold(
            title = "Item to Item (Two Way)",
            onBack = onBack,
            actions = {
                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                    Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                }
            },
        ) { paddingValues ->
            ItemToItemTwoDirectionsScreenContent(
                dragAfterLongPress = dragAfterLongPress,
                requireFirstDownUnconsumed = requireFirstDownUnconsumed,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun ItemToItemTwoDirectionsScreenContent(
    dragAfterLongPress: Boolean,
    requireFirstDownUnconsumed: Boolean,
    modifier: Modifier = Modifier,
) {
    val dragAndDropState = rememberDragAndDropState<Int>(
        dragAfterLongPress = dragAfterLongPress,
        requireFirstDownUnconsumed = requireFirstDownUnconsumed,
    )

    var itemIndex by remember { mutableStateOf(0) }

    DragAndDropContainer(
        state = dragAndDropState,
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            val isTopHovered = dragAndDropState.hoveredDropTargetKey == 0

            DndDropZone(
                label = if (isTopHovered) "Hovering" else "Zone A",
                isHovered = isTopHovered,
                accentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .dropTarget(
                        key = 0,
                        state = dragAndDropState,
                        onDragEnter = { state ->
                            if (state.data == 1) {
                                itemIndex = 0
                            }
                        },
                        onDragExit = { state ->
                            if (state.data == 1) {
                                itemIndex = 1
                            }
                        },
                    ),
            ) {
                if (itemIndex == 0) {
                    DraggableItem(
                        state = dragAndDropState,
                        key = 0,
                        data = 0,
                        dropTargets = listOf(1),
                        modifier = Modifier.size(200.dp),
                    ) {
                        RedBox(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                }
                                .fillMaxSize(),
                        )
                    }
                }
            }

            val isBottomHovered = dragAndDropState.hoveredDropTargetKey == 1

            DndDropZone(
                label = if (isBottomHovered) "Hovering" else "Zone B",
                isHovered = isBottomHovered,
                accentColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .dropTarget(
                        key = 1,
                        state = dragAndDropState,
                        onDragEnter = { state ->
                            if (state.data == 0) {
                                itemIndex = 1
                            }
                        },
                        onDragExit = { state ->
                            if (state.data == 0) {
                                itemIndex = 0
                            }
                        },
                    ),
            ) {
                if (itemIndex == 1) {
                    DraggableItem(
                        state = dragAndDropState,
                        key = 0,
                        data = 1,
                        dropTargets = listOf(0),
                        modifier = Modifier.size(100.dp),
                    ) {
                        RedBox(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                }
                                .fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}
