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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.dragHandle
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.CircleIconButton
import components.CodeBlock
import components.GripDots
import components.SectionLabel
import components.dragPlaceholder
import components.lifted
import navigation.Demo
import theme.LocalThemeController
import theme.SampleTheme

private const val InstallSnippet = "com.mohamedrejeb.dnd:compose-dnd:1.0.0-alpha01"

@Composable
fun HomeScreen(
    onOpen: (Demo) -> Unit,
) {
    val colors = SampleTheme.colors
    val examples = remember { Demo.entries.filter { it.kind == Demo.Kind.Example } }
    // The feature list is itself reorderable: grab a row by its handle.
    var features by remember { mutableStateOf(Demo.entries.filter { it.kind == Demo.Kind.Feature }) }

    val dndState = rememberDragAndDropState<Demo>()
    val listState = rememberLazyListState()
    val insets = WindowInsets.safeDrawing.asPaddingValues()
    val layoutDirection = LocalLayoutDirection.current

    DragAndDropContainer(
        state = dndState,
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = insets.calculateStartPadding(layoutDirection) + 16.dp,
                end = insets.calculateEndPadding(layoutDirection) + 16.dp,
                top = insets.calculateTopPadding() + 8.dp,
                bottom = insets.calculateBottomPadding() + 24.dp,
            ),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(state = dndState, lazyListState = listState),
        ) {
            item(key = "header") { Header() }

            item(key = "examples-label") {
                SectionLabel("Examples", modifier = Modifier.padding(top = 28.dp, bottom = 12.dp))
            }
            item(key = "examples") {
                ExampleGrid(examples = examples, onOpen = onOpen)
            }

            item(key = "features-label") {
                SectionLabel(
                    text = "Features",
                    trailing = "Drag a handle to reorder",
                    modifier = Modifier.padding(top = 28.dp, bottom = 12.dp),
                )
            }
            itemsIndexed(features, key = { _, demo -> demo }) { index, demo ->
                val shape = groupShape(index, features.size)
                val isDragging = dndState.isDragging(demo)
                FeatureRow(
                    demo = demo,
                    showDivider = index > 0,
                    onClick = { onOpen(demo) },
                    handleModifier = Modifier.dragHandle(key = demo, state = dndState),
                    modifier = Modifier
                        .animateItem()
                        .dragPlaceholder(active = isDragging, shape = shape)
                        .reorderableItem(
                            key = demo,
                            data = demo,
                            state = dndState,
                            hasDragHandle = true,
                            onDragEnter = { dragged ->
                                val from = features.indexOf(dragged.data)
                                val to = features.indexOf(demo)
                                if (from != -1 && to != -1) {
                                    features = features.toMutableList().apply { add(to, removeAt(from)) }
                                }
                            },
                            draggableContent = {
                                FeatureRow(
                                    demo = demo,
                                    showDivider = false,
                                    onClick = {},
                                    shape = SampleTheme.shapes.item,
                                    modifier = Modifier.lifted(),
                                )
                            },
                        ),
                    shape = shape,
                )
            }

            item(key = "install") { InstallFooter() }
        }
    }
}

@Composable
private fun Header() {
    val colors = SampleTheme.colors
    val theme = LocalThemeController.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Spacer(Modifier.weight(1f))
            CircleIconButton(
                icon = if (theme.darkTheme) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                contentDescription = if (theme.darkTheme) "Switch to light theme" else "Switch to dark theme",
                onClick = theme.toggle,
            )
        }
        Text(
            text = "Compose DnD",
            style = MaterialTheme.typography.displaySmall,
            color = colors.ink,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Drag and drop, reordering and auto scroll for Compose Multiplatform.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.inkMuted,
        )
    }
}

@Composable
private fun ExampleGrid(
    examples: List<Demo>,
    onOpen: (Demo) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val (featured, rest) = examples.first() to examples.drop(1)
        ExampleTile(
            demo = featured,
            onClick = { onOpen(featured) },
            previewHeight = 132,
            modifier = Modifier.fillMaxWidth(),
        )
        rest.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { demo ->
                    ExampleTile(
                        demo = demo,
                        onClick = { onOpen(demo) },
                        previewHeight = 104,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ExampleTile(
    demo: Demo,
    onClick: () -> Unit,
    previewHeight: Int,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    Column(
        modifier = modifier
            .clip(SampleTheme.shapes.well)
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(previewHeight.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.well),
        ) {
            ExamplePreview(demo)
        }
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 8.dp)) {
            Text(
                text = demo.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = demo.summary,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                minLines = 2,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun FeatureRow(
    demo: Demo,
    showDivider: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier,
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
                        start = Offset(52.dp.toPx(), 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }.clickable(onClick = onClick)
            .padding(end = 12.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = handleModifier
                .size(width = 52.dp, height = 64.dp),
        ) {
            GripDots(color = colors.inkMuted.copy(alpha = 0.6f))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = demo.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
            Text(
                text = demo.summary,
                style = SampleTheme.code,
                color = colors.inkMuted,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.inkMuted,
        )
    }
}

/** Rounds only the outer corners of a row inside a grouped list. */
private fun groupShape(index: Int, count: Int): Shape {
    val radius = 20.dp
    val top = if (index == 0) radius else 0.dp
    val bottom = if (index == count - 1) radius else 0.dp
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

@Composable
private fun InstallFooter() {
    val colors = SampleTheme.colors
    Column(modifier = Modifier.padding(top = 28.dp)) {
        SectionLabel("Add it to your project", modifier = Modifier.padding(bottom = 12.dp))
        CodeBlock(code = InstallSnippet)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Runs on Android, iOS, desktop and the web.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.inkMuted,
        )
    }
}
