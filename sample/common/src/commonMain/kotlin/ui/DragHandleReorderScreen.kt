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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.dragHandle
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScaffold
import components.SectionLabel
import components.dragPlaceholder
import components.lifted
import navigation.Demo

private const val DragHandleCode = """
SettingRow(
  setting,
  onToggle = { settings = settings.toggle(id) },
  modifier = Modifier.reorderableItem(
    key = id,
    data = setting,
    state = dnd,
    hasDragHandle = true,
    onDragEnter = {
      settings = settings.move(it.data, setting)
    },
    draggableContent = { SettingRow(setting) },
  ),
  handleModifier = Modifier.dragHandle(id, dnd),
)
"""

@Composable
fun DragHandleReorderScreen(onBack: () -> Unit) {
    DemoScaffold(
        title = Demo.DragHandle.title,
        hint = "Drag a grip to reorder. Tap a row to switch it on or off.",
        code = DragHandleCode,
        onBack = onBack,
    ) { insets ->
        QuickSettingsList(insets)
    }
}

@Composable
private fun QuickSettingsList(insets: PaddingValues) {
    val dndState = rememberDragAndDropState<QuickSetting>()
    val listState = rememberLazyListState()
    var settings by remember { mutableStateOf(InitialSettings) }

    DragAndDropContainer(state = dndState, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 4.dp,
                bottom = insets.calculateBottomPadding() + 16.dp,
            ),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(state = dndState, lazyListState = listState),
        ) {
            item(key = "header") {
                SectionLabel(
                    text = "Quick settings",
                    trailing = "${settings.count { it.on }} on",
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                )
            }
            items(settings, key = { it.label }) { setting ->
                SettingRow(
                    setting = setting,
                    onToggle = { settings = settings.toggle(setting.label) },
                    handleModifier = Modifier.dragHandle(key = setting.label, state = dndState),
                    modifier = Modifier
                        .animateItem()
                        .dragPlaceholder(active = dndState.isDragging(setting.label))
                        .reorderableItem(
                            key = setting.label,
                            data = setting,
                            state = dndState,
                            hasDragHandle = true,
                            onDragEnter = { dragged ->
                                settings = settings.move(dragged.data, setting)
                            },
                            draggableContent = {
                                SettingRow(
                                    setting = setting,
                                    onToggle = {},
                                    modifier = Modifier.lifted(),
                                )
                            },
                        ),
                )
            }
        }
    }
}
