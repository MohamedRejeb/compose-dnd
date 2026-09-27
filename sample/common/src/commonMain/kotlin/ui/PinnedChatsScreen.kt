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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScreenScaffold

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
    DemoScreenScaffold(
        title = "Pinned Chats",
        onBack = onBack,
    ) { paddingValues ->
        PinnedChatsContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        )
    }
}

@Immutable
private data class Chat(
    val name: String,
    val preview: String,
    val color: Color,
    val isPinned: Boolean,
)

private val InitialChats = listOf(
    Chat("Design team", "New mockups are up", Color(0xFF6366F1), isPinned = true),
    Chat("Mom", "Call me when you land", Color(0xFFEC4899), isPinned = true),
    Chat("Release crew", "1.0 alpha is ready for review", Color(0xFF10B981), isPinned = true),
    Chat("Alex", "Sounds good!", Color(0xFFF59E0B), isPinned = true),
    Chat("Sam", "See you tomorrow", Color(0xFF0EA5E9), isPinned = false),
    Chat("Book club", "Next pick: Dune", Color(0xFF8B5CF6), isPinned = false),
    Chat("Jordan", "Sent a photo", Color(0xFF14B8A6), isPinned = false),
    Chat("Gym buddies", "Leg day?", Color(0xFFEF4444), isPinned = false),
    Chat("Taylor", "Thanks!", Color(0xFF64748B), isPinned = false),
    Chat("Family", "Dinner on Sunday", Color(0xFFD97706), isPinned = false),
)

@Composable
private fun PinnedChatsContent(
    modifier: Modifier = Modifier,
) {
    val state = rememberDragAndDropState<Chat>(dragAfterLongPress = true)
    val listState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current
    var chats by remember { mutableStateOf(InitialChats) }
    var savedOrder by remember { mutableStateOf(InitialChats.filter { it.isPinned }) }
    var saveCount by remember { mutableIntStateOf(0) }

    DragAndDropContainer(
        state = state,
        modifier = modifier,
    ) {
        Column {
            Text(
                text = "Long-press a pinned chat to reorder it. Other chats are locked in place.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            SavedOrderBanner(savedOrder = savedOrder, saveCount = saveCount)

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .dragAutoScroll(state = state, lazyListState = listState),
            ) {
                items(chats, key = { it.name }) { chat ->
                    ChatRow(
                        chat = chat,
                        modifier = Modifier
                            .animateItem()
                            .graphicsLayer { alpha = if (state.isDragging(chat.name)) 0f else 1f }
                            .reorderableItem(
                                key = chat.name,
                                data = chat,
                                state = state,
                                enabled = chat.isPinned,
                                canDrop = chat.isPinned,
                                onDragStart = { haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                                onDragEnter = { dragged ->
                                    chats = chats.moved(dragged.data, onto = chat)
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                },
                                onDragEnd = {
                                    // Persist once per drag, not on every swap
                                    savedOrder = chats.filter { it.isPinned }
                                    saveCount++
                                },
                                draggableContent = {
                                    ChatRow(chat = chat, isLifted = true)
                                },
                            ),
                    )
                }
            }
        }
    }
}

private fun List<Chat>.moved(chat: Chat, onto: Chat): List<Chat> {
    val from = indexOf(chat)
    val to = indexOf(onto)
    if (from == -1 || to == -1 || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

@Composable
private fun SavedOrderBanner(
    savedOrder: List<Chat>,
    saveCount: Int,
) {
    Text(
        text = "Saved order ($saveCount): " + savedOrder.joinToString(" · ") { it.name },
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
private fun ChatRow(
    chat: Chat,
    modifier: Modifier = Modifier,
    isLifted: Boolean = false,
) {
    val shape = RoundedCornerShape(12.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .then(if (isLifted) Modifier.shadow(12.dp, shape) else Modifier)
            .clip(shape)
            .background(
                if (isLifted) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surface,
            ).padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .background(chat.color, CircleShape),
        ) {
            Text(
                text = chat.name.take(1),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = chat.preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (chat.isPinned) {
            Icon(
                imageVector = Icons.Rounded.PushPin,
                contentDescription = "Pinned",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
