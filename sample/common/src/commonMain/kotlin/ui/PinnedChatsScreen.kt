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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScaffold
import components.SectionLabel
import components.dragPlaceholder
import components.lifted
import theme.SampleTheme

private val PinnedChatsCode = """
    Modifier.reorderableItem(
        key = chat.name,
        data = chat,
        state = dndState,
        enabled = chat.isPinned,
        canDrop = chat.isPinned,
        onDragEnter = { dragged ->
            chats = chats.moved(dragged.data, chat)
        },
        onDragEnd = {
            save(chats.filter { it.isPinned })
        },
        draggableContent = { ChatRow(chat) },
    )
"""

/**
 * Only pinned chats can be reordered, like in most messaging apps. Regular
 * chats are locked: `enabled = false` so they can't be dragged, and
 * `canDrop = false` so a dragged chat never swaps with them.
 *
 * onDragEnd is where the new order gets saved: it fires once per drag, after
 * the drop animation, instead of on every intermediate swap.
 */
@Composable
fun PinnedChatsScreen(
    onBack: () -> Unit,
) {
    DemoScaffold(
        title = "Pinned chats",
        hint = "Long-press a pinned chat and drag it to a new spot.",
        code = PinnedChatsCode,
        onBack = onBack,
    ) { insets ->
        PinnedChatsContent(insets = insets)
    }
}

@Composable
private fun PinnedChatsContent(insets: PaddingValues) {
    val state = rememberDragAndDropState<Chat>(dragAfterLongPress = true)
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    val layoutDirection = LocalLayoutDirection.current
    var chats by remember { mutableStateOf(InitialChats) }
    var savedOrder by remember { mutableStateOf(InitialChats.filter { it.isPinned }) }
    var saveCount by remember { mutableIntStateOf(0) }

    val actions = ChatActions(
        onMove = { dragged, onto ->
            chats = chats.moved(dragged, onto = onto)
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        onDragStart = { haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
        onDragEnd = {
            // Persist once per drag, not on every swap
            savedOrder = chats.filter { it.isPinned }
            saveCount++
        },
    )

    DragAndDropContainer(
        state = state,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = insets.calculateStartPadding(layoutDirection) + 16.dp,
                end = insets.calculateEndPadding(layoutDirection) + 16.dp,
                top = 4.dp,
                bottom = insets.calculateBottomPadding() + 24.dp,
            ),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(state = state, lazyListState = listState),
        ) {
            item(key = "pinned-label") {
                PinnedHeader(
                    savedOrder = savedOrder,
                    saveCount = saveCount,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            chatGroup(chats.filter { it.isPinned }, state, actions)
            item(key = "all-label") {
                SectionLabel("All chats", modifier = Modifier.padding(top = 20.dp, bottom = 12.dp))
            }
            chatGroup(chats.filterNot { it.isPinned }, state, actions)
        }
    }
}

/** What a chat row does when it is dragged. */
private class ChatActions(
    val onMove: (dragged: Chat, onto: Chat) -> Unit,
    val onDragStart: () -> Unit,
    val onDragEnd: () -> Unit,
)

private fun LazyListScope.chatGroup(
    group: List<Chat>,
    state: DragAndDropState<Chat>,
    actions: ChatActions,
) {
    itemsIndexed(group, key = { _, chat -> chat.name }) { index, chat ->
        ReorderableChat(
            chat = chat,
            index = index,
            count = group.size,
            state = state,
            actions = actions,
        )
    }
}

@Composable
private fun LazyItemScope.ReorderableChat(
    chat: Chat,
    index: Int,
    count: Int,
    state: DragAndDropState<Chat>,
    actions: ChatActions,
) {
    val shape = chatGroupShape(index, count)
    ChatRow(
        chat = chat,
        shape = shape,
        showDivider = index > 0,
        modifier = Modifier
            .animateItem()
            .dragPlaceholder(active = state.isDragging(chat.name), shape = shape)
            .reorderableItem(
                key = chat.name,
                data = chat,
                state = state,
                enabled = chat.isPinned,
                canDrop = chat.isPinned,
                onDragStart = { actions.onDragStart() },
                onDragEnter = { dragged -> actions.onMove(dragged.data, chat) },
                onDragEnd = { actions.onDragEnd() },
                draggableContent = {
                    ChatRow(
                        chat = chat,
                        shape = SampleTheme.shapes.item,
                        modifier = Modifier.lifted(),
                    )
                },
            ),
    )
}

private fun List<Chat>.moved(chat: Chat, onto: Chat): List<Chat> {
    val from = indexOf(chat)
    val to = indexOf(onto)
    if (from == -1 || to == -1 || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}
