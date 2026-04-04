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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HomeListItem(
                text = "From item to item (one direction)",
                onClick = onNavigateToItemToItemOneDirection,
            )

            HomeListItem(
                text = "From item to item (two directions)",
                onClick = onNavigateToItemToItemTwoDirections,
            )

            HomeListItem(
                text = "Reorderable List",
                onClick = onNavigateToReorderList,
            )

            HomeListItem(
                text = "From list to list (without reorder)",
                onClick = onNavigateToListToListWithoutReorder,
            )

            HomeListItem(
                text = "From list to list (with reorder)",
                onClick = onNavigateToListToListWithReorder,
            )

            HomeListItem(
                text = "Drop Strategies Playground",
                onClick = onNavigateToDropStrategiesPlayground,
            )

            HomeListItem(
                text = "Reorder with Drag Handle",
                onClick = onNavigateToDragHandleReorder,
            )

            HomeListItem(
                text = "Axis-Locked Drag",
                onClick = onNavigateToAxisLockedDrag,
            )

            HomeListItem(
                text = "Conditional Drop (canDrop)",
                onClick = onNavigateToConditionalDrop,
            )

            HomeListItem(
                text = "Auto-Scroll Demo (Row, Grid, Scroll)",
                onClick = onNavigateToAutoScrollDemo,
            )
        }
    }
}

@Composable
private fun HomeListItem(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable(
                onClick = onClick,
            )
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        )
    }
}
