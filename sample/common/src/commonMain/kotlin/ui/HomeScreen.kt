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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.FilterNone
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import components.DemoScreenScaffold
import components.ExampleStatus
import components.FeatureCard
import components.FilesMiniScene
import components.GradientHero
import components.KanbanMiniScene
import components.PlaylistMiniScene
import components.RealExampleCard
import components.SectionHeader
import theme.SampleAccents

@Composable
fun HomeScreen(
    onNavigateToItemToItemOneDirection: () -> Unit,
    onNavigateToItemToItemTwoDirections: () -> Unit,
    onNavigateToReorderList: () -> Unit,
    onNavigateToListToListWithoutReorder: () -> Unit,
    onNavigateToListToListWithReorder: () -> Unit,
    onNavigateToDropStrategiesPlayground: () -> Unit,
    onNavigateToDragHandleReorder: () -> Unit,
    onNavigateToAxisLockedDrag: () -> Unit,
    onNavigateToConditionalDrop: () -> Unit,
    onNavigateToAutoScrollDemo: () -> Unit,
    onNavigateToKanbanBoard: () -> Unit,
) {
    DemoScreenScaffold(
        title = "Compose DND",
        onBack = null,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                GradientHero()
            }

            item { SectionSpacer() }

            item {
                SectionHeader(
                    title = "Start here",
                    count = 2,
                    subtitle = "One state, one container, two modifiers - draggableItem and dropTarget.",
                    accent = SampleAccents.Sky,
                )
            }

            item {
                FeatureCard(
                    title = "Drag to a Target",
                    description = "Drag an item from one zone and drop it onto another.",
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    accent = SampleAccents.Sky,
                    onClick = onNavigateToItemToItemOneDirection,
                )
            }

            item {
                FeatureCard(
                    title = "Two-Way Drag",
                    description = "Move items freely between two zones, in both directions.",
                    icon = Icons.Rounded.SwapVert,
                    accent = SampleAccents.Sky,
                    onClick = onNavigateToItemToItemTwoDirections,
                )
            }

            item { SectionSpacer() }

            item {
                SectionHeader(
                    title = "Reorder",
                    count = 3,
                    subtitle = "The reorderableItem modifier turns any lazy list into a sortable one.",
                    accent = SampleAccents.Indigo,
                )
            }

            item {
                FeatureCard(
                    title = "Reorderable List",
                    description = "Drag items to reorder within a single list with smooth displacement.",
                    icon = Icons.Rounded.Reorder,
                    accent = SampleAccents.Indigo,
                    onClick = onNavigateToReorderList,
                )
            }

            item {
                FeatureCard(
                    title = "Drag Handle",
                    description = "Only a grip icon starts the drag - the rest of the row stays interactive.",
                    icon = Icons.Rounded.DragHandle,
                    accent = SampleAccents.Indigo,
                    onClick = onNavigateToDragHandleReorder,
                )
            }

            item {
                FeatureCard(
                    title = "Auto Scroll",
                    description = "Lists, grids, and scrollable columns scroll on their own near the edges.",
                    icon = Icons.Rounded.Height,
                    accent = SampleAccents.Teal,
                    onClick = onNavigateToAutoScrollDemo,
                )
            }

            item { SectionSpacer() }

            item {
                SectionHeader(
                    title = "Multiple lists",
                    count = 2,
                    subtitle = "Shared drag state lets items travel between any number of lists.",
                    accent = SampleAccents.Violet,
                )
            }

            item {
                FeatureCard(
                    title = "List to List",
                    description = "Move items between two lists - dropped items append to the target.",
                    icon = Icons.Rounded.SwapHoriz,
                    accent = SampleAccents.Violet,
                    onClick = onNavigateToListToListWithoutReorder,
                )
            }

            item {
                FeatureCard(
                    title = "List to List with Reorder",
                    description = "Insert items at any position while moving them across lists.",
                    icon = Icons.Rounded.FilterNone,
                    accent = SampleAccents.Violet,
                    onClick = onNavigateToListToListWithReorder,
                )
            }

            item { SectionSpacer() }

            item {
                SectionHeader(
                    title = "Control",
                    count = 3,
                    subtitle = "Axis locks, drop validation, and strategies for picking the hovered target.",
                    accent = SampleAccents.Amber,
                )
            }

            item {
                FeatureCard(
                    title = "Axis-Locked Drag",
                    description = "Constrain movement to horizontal, vertical, or leave it free.",
                    icon = Icons.Rounded.Height,
                    accent = SampleAccents.Amber,
                    onClick = onNavigateToAxisLockedDrag,
                )
            }

            item {
                FeatureCard(
                    title = "Conditional Drop",
                    description = "Targets accept or reject drops dynamically with canDrop.",
                    icon = Icons.Rounded.Lock,
                    accent = SampleAccents.Amber,
                    onClick = onNavigateToConditionalDrop,
                )
            }

            item {
                FeatureCard(
                    title = "Drop Strategies Playground",
                    description = "Compare Surface, Surface Percentage, and Center Distance live.",
                    icon = Icons.Rounded.Tune,
                    accent = SampleAccents.Magenta,
                    onClick = onNavigateToDropStrategiesPlayground,
                )
            }

            item { SectionSpacer() }

            item {
                SectionHeader(
                    title = "Real examples",
                    count = 3,
                    subtitle = "Production-style screens built entirely with Compose DND.",
                    accent = SampleAccents.Emerald,
                )
            }

            item {
                RealExampleCard(
                    name = "Kanban Board",
                    tagline = "Cross-column card transfer with nested auto-scroll and positional insert.",
                    status = ExampleStatus.Live,
                    onClick = onNavigateToKanbanBoard,
                    preview = { KanbanMiniScene() },
                )
            }

            item {
                RealExampleCard(
                    name = "Playlist",
                    tagline = "Reorder tracks with drag handles and queue-to-queue moves.",
                    status = ExampleStatus.ComingSoon,
                    onClick = {},
                    preview = { PlaylistMiniScene() },
                )
            }

            item {
                RealExampleCard(
                    name = "File Manager",
                    tagline = "Drag files between folders with conditional drops and spring-loaded targets.",
                    status = ExampleStatus.ComingSoon,
                    onClick = {},
                    preview = { FilesMiniScene() },
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                Footer()
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SectionSpacer() {
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun Footer() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "com.mohamedrejeb.dnd",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Apache-2.0 licensed · Built with Compose Multiplatform",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
