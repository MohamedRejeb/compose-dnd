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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.FilterNone
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Height
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Reorder
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
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
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(text = "Compose DnD")
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
        ) {
            // -- Drag & Drop Basics --
            SectionHeader(title = "Drag & Drop Basics")

            DemoListItem(
                title = "Item to Item (One Direction)",
                description = "Drag an item from one zone to another",
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                onClick = onNavigateToItemToItemOneDirection,
            )
            DemoListItem(
                title = "Item to Item (Two Directions)",
                description = "Move items freely between two zones",
                icon = Icons.Rounded.SwapVert,
                onClick = onNavigateToItemToItemTwoDirections,
            )
            DemoListItem(
                title = "Drop Strategies Playground",
                description = "Compare Surface, Surface%, and Center Distance strategies",
                icon = Icons.Rounded.Tune,
                onClick = onNavigateToDropStrategiesPlayground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // -- Reorder Lists --
            SectionHeader(title = "Reorder Lists")

            DemoListItem(
                title = "Reorderable List",
                description = "Drag items to reorder within a single list",
                icon = Icons.Rounded.Reorder,
                onClick = onNavigateToReorderList,
            )
            DemoListItem(
                title = "Reorder with Drag Handle",
                description = "Use a dedicated handle to initiate drag",
                icon = Icons.Rounded.DragHandle,
                onClick = onNavigateToDragHandleReorder,
            )
            DemoListItem(
                title = "List to List (without reorder)",
                description = "Move items between two lists on drop",
                icon = Icons.Rounded.SwapHoriz,
                onClick = onNavigateToListToListWithoutReorder,
            )
            DemoListItem(
                title = "List to List (with reorder)",
                description = "Move and reorder items across two lists",
                icon = Icons.Rounded.FilterNone,
                onClick = onNavigateToListToListWithReorder,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // -- Advanced Features --
            SectionHeader(title = "Advanced Features")

            DemoListItem(
                title = "Auto-Scroll Demo",
                description = "Row, Grid, and ScrollState auto-scroll while dragging",
                icon = Icons.Rounded.GridView,
                onClick = onNavigateToAutoScrollDemo,
            )
            DemoListItem(
                title = "Axis-Locked Drag",
                description = "Constrain drag movement to horizontal, vertical, or free",
                icon = Icons.Rounded.Height,
                onClick = onNavigateToAxisLockedDrag,
            )
            DemoListItem(
                title = "Conditional Drop (canDrop)",
                description = "Only accept drops when conditions are met",
                icon = Icons.Rounded.Lock,
                onClick = onNavigateToConditionalDrop,
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 4.dp,
        ),
    )
}

@Composable
private fun DemoListItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        modifier = Modifier.clickable(onClick = onClick),
    )
}
