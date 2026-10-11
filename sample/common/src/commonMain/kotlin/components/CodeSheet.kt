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
package components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import theme.Hues
import theme.SampleColors
import theme.SampleTheme

/**
 * A bottom sheet that shows the code behind the current demo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeSheet(
    title: String,
    code: String,
    onDismiss: () -> Unit,
) {
    val colors = SampleTheme.colors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
        contentColor = colors.ink,
        scrimColor = Color.Black.copy(alpha = 0.32f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.ink,
                )
            }
            Spacer(Modifier.height(12.dp))
            CodeBlock(code = code)
        }
    }
}

/** A recessed, horizontally scrollable block of highlighted Kotlin. */
@Composable
fun CodeBlock(
    code: String,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    val highlighted = remember(code, colors) { highlightKotlin(code.trimIndent(), colors) }
    Text(
        text = highlighted,
        style = SampleTheme.code,
        color = colors.ink,
        softWrap = false,
        modifier = modifier
            .fillMaxWidth()
            .clip(SampleTheme.shapes.small)
            .background(colors.well)
            .horizontalScroll(rememberScrollState())
            .padding(16.dp),
    )
}

private val keywords = setOf(
    "val", "var", "fun", "if", "else", "return", "when", "true", "false", "null",
    "by", "in", "is", "it", "object", "class", "data", "private", "for",
)

private val tokenPattern = Regex(
    """(//[^\n]*)|("(?:[^"\\]|\\.)*")|(\.[a-z][A-Za-z0-9]*(?=\s*[({]))|([A-Za-z_][A-Za-z0-9_]*)|(\s+)|(.)""",
)

/** A small highlighter for the handful of Kotlin shapes our snippets use. */
internal fun highlightKotlin(code: String, colors: SampleColors): AnnotatedString {
    val comment = SpanStyle(color = colors.inkMuted)
    val string = SpanStyle(color = if (colors.isDark) Hues.Mint.container else Hues.Mint.onContainer)
    val keyword = SpanStyle(color = if (colors.isDark) Hues.Lilac.container else Hues.Lilac.onContainer)
    val call = SpanStyle(color = colors.accent, fontWeight = FontWeight.Medium)
    val type = SpanStyle(color = if (colors.isDark) Hues.Peach.container else Hues.Peach.onContainer)

    return buildAnnotatedString {
        tokenPattern.findAll(code).forEach { match ->
            val text = match.value
            val style = when {
                match.groups[1] != null -> comment
                match.groups[2] != null -> string
                match.groups[3] != null -> call
                match.groups[4] != null && text in keywords -> keyword
                match.groups[4] != null && text.first().isUpperCase() -> type
                else -> null
            }
            if (style == null) append(text) else withStyle(style) { append(text) }
        }
    }
}
