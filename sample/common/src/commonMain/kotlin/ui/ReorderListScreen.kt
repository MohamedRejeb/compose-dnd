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
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScaffold
import components.SectionLabel
import components.dragPlaceholder
import components.lifted
import navigation.Demo

private const val ReorderListCode = """
val dnd = rememberDragAndDropState<Language>(
    dragAfterLongPress = true)

LazyColumn(Modifier.dragAutoScroll(dnd, list)) {
  items(languages, key = { it.name }) { language ->
    LanguageRow(language, Modifier.animateItem()
      .reorderableItem(
        key = language.name,
        data = language,
        state = dnd,
        onDragEnter = {
          languages = languages.move(it.data, language)
        },
        draggableContent = { LanguageRow(language) },
      ))
  }
}
"""

@Composable
fun ReorderListScreen(onBack: () -> Unit) {
    DemoScaffold(
        title = Demo.ReorderList.title,
        hint = "Long-press a language and drag it to a new spot.",
        code = ReorderListCode,
        onBack = onBack,
    ) { insets ->
        LearningQueue(insets)
    }
}

@Composable
private fun LearningQueue(insets: PaddingValues) {
    // Long press so a swipe on a row still scrolls the list on touch screens.
    val dndState = rememberDragAndDropState<Language>(dragAfterLongPress = true)
    val listState = rememberLazyListState()
    var languages by remember { mutableStateOf(InitialLanguages) }

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
                    text = "Learning queue",
                    trailing = "${languages.size} languages",
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                )
            }
            items(languages, key = { it.name }) { language ->
                LanguageRow(
                    language = language,
                    modifier = Modifier
                        .animateItem()
                        .dragPlaceholder(active = dndState.isDragging(language.name))
                        .reorderableItem(
                            key = language.name,
                            data = language,
                            state = dndState,
                            onDragEnter = { dragged -> languages = languages.move(dragged.data, language) },
                            draggableContent = {
                                LanguageRow(language = language, modifier = Modifier.lifted())
                            },
                        ),
                )
            }
        }
    }
}
