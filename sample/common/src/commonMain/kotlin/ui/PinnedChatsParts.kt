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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.Avatar
import components.SectionLabel
import theme.Hue
import theme.Hues
import theme.SampleTheme

@Immutable
internal data class Chat(
    val name: String,
    val preview: String,
    val time: String,
    val hue: Hue,
    val isPinned: Boolean,
)

internal val InitialChats = listOf(
    Chat("Design team", "New mockups are up", "9:41", Hues.Lilac, isPinned = true),
    Chat("Mom", "Call me when you land", "9:12", Hues.Rose, isPinned = true),
    Chat("Release crew", "1.0 alpha is ready for review", "8:30", Hues.Mint, isPinned = true),
    Chat("Alex", "Sounds good, see you there", "Yesterday", Hues.Butter, isPinned = true),
    Chat("Sam", "See you tomorrow", "Yesterday", Hues.Sky, isPinned = false),
    Chat("Book club", "Next book is picked", "Mon", Hues.Peach, isPinned = false),
    Chat("Jordan", "Sent a photo", "Mon", Hues.Mint, isPinned = false),
    Chat("Gym buddies", "Leg day tomorrow?", "Sun", Hues.Rose, isPinned = false),
    Chat("Taylor", "Thanks for the notes", "Sat", Hues.Lilac, isPinned = false),
    Chat("Family", "Dinner on Sunday", "Fri", Hues.Butter, isPinned = false),
)

private val AvatarSize = 44.dp
private val RowPadding = 14.dp
private val DividerInset = RowPadding + AvatarSize + 12.dp
private val GroupRadius = 20.dp

@Composable
internal fun ChatRow(
    chat: Chat,
    shape: Shape,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .drawBehind {
                if (showDivider) {
                    drawLine(
                        color = colors.line,
                        start = Offset(DividerInset.toPx(), 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }.padding(horizontal = RowPadding, vertical = 12.dp),
    ) {
        Avatar(name = chat.name, hue = chat.hue, size = AvatarSize)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
                maxLines = 1,
            )
            Text(
                text = chat.preview,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        ChatMeta(chat)
    }
}

@Composable
private fun ChatMeta(chat: Chat) {
    val colors = SampleTheme.colors
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = chat.time,
            style = MaterialTheme.typography.labelMedium,
            color = colors.inkMuted,
        )
        if (chat.isPinned) {
            Icon(
                imageVector = Icons.Rounded.PushPin,
                contentDescription = "Pinned",
                tint = colors.inkMuted,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * The "Pinned" heading with a quiet note on the right that changes each time
 * `onDragEnd` saves the order. The saved order itself is in the semantics.
 */
@Composable
internal fun PinnedHeader(
    savedOrder: List<Chat>,
    saveCount: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        SectionLabel(text = "Pinned")
        Spacer(Modifier.weight(1f))
        if (saveCount > 0) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = colors.inkMuted,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = saveLabel(saveCount),
            style = MaterialTheme.typography.labelMedium,
            color = colors.inkMuted,
            modifier = Modifier.semantics {
                contentDescription = "Saved order: " + savedOrder.joinToString(", ") { it.name }
            },
        )
    }
}

private fun saveLabel(saveCount: Int): String = when (saveCount) {
    0 -> "Saves on drop"
    1 -> "Saved once"
    else -> "Saved $saveCount times"
}

/** Rounds only the outer corners of a row inside a grouped list. */
internal fun chatGroupShape(index: Int, count: Int): Shape {
    val top = if (index == 0) GroupRadius else 0.dp
    val bottom = if (index == count - 1) GroupRadius else 0.dp
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}
