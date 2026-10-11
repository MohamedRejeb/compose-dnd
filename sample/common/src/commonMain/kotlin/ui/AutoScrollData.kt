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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Backpack
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Chair
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.Contacts
import androidx.compose.material.icons.rounded.DocumentScanner
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.Light
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Monitor
import androidx.compose.material.icons.rounded.Mouse
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.PedalBike
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TabletMac
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Umbrella
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import theme.Hue
import theme.Hues

internal val ScrollProductWidth = 232.dp
internal val ScrollAppTileHeight = 92.dp

@Immutable
internal data class ScrollProduct(
    val name: String,
    val price: String,
    val icon: ImageVector,
    val hue: Hue
)

@Immutable
internal data class ScrollApp(
    val name: String,
    val icon: ImageVector,
    val hue: Hue
)

@Immutable
internal data class ScrollPhoto(
    val place: String,
    val height: Dp,
    val hue: Hue
)

@Immutable
internal data class ScrollTask(
    val title: String,
    val due: String,
    val tag: String,
    val hue: Hue
)

private val ProductList = listOf(
    Triple("Desk lamp", "$39", Icons.Rounded.Light), Triple("Backpack", "$64", Icons.Rounded.Backpack),
    Triple("Coffee mug", "$12", Icons.Rounded.Coffee), Triple("Wrist watch", "$129", Icons.Rounded.Watch),
    Triple("Keyboard", "$89", Icons.Rounded.Keyboard), Triple("City bike", "$420", Icons.Rounded.PedalBike),
    Triple("Notebook", "$9", Icons.Rounded.MenuBook), Triple("Umbrella", "$24", Icons.Rounded.Umbrella),
    Triple("Armchair", "$310", Icons.Rounded.Chair), Triple("Laptop", "$1,199", Icons.Rounded.Laptop),
    Triple("House plant", "$18", Icons.Rounded.LocalFlorist), Triple("Mouse", "$35", Icons.Rounded.Mouse),
    Triple("Monitor", "$279", Icons.Rounded.Monitor), Triple("Suitcase", "$150", Icons.Rounded.Luggage),
    Triple("Rain jacket", "$75", Icons.Rounded.Checkroom), Triple("Printer", "$99", Icons.Rounded.Print),
    Triple("Router", "$59", Icons.Rounded.Router), Triple("Tablet", "$449", Icons.Rounded.TabletMac),
    Triple("Bed frame", "$380", Icons.Rounded.Bed), Triple("Toolbox", "$45", Icons.Rounded.Handyman),
)

internal val InitialScrollProducts: List<ScrollProduct> =
    ProductList.mapIndexed { i, (name, price, icon) -> ScrollProduct(name, price, icon, Hues.at(i)) }

private val AppList = listOf(
    "Camera" to Icons.Rounded.CameraAlt, "Photos" to Icons.Rounded.Photo, "Maps" to Icons.Rounded.Map,
    "Mail" to Icons.Rounded.Mail, "Tasks" to Icons.Rounded.Checklist, "Messages" to Icons.Rounded.ChatBubble,
    "Calendar" to Icons.Rounded.CalendarMonth, "Clock" to Icons.Rounded.Schedule,
    "Weather" to Icons.Rounded.WbSunny, "Notes" to Icons.Rounded.EditNote, "Files" to Icons.Rounded.Folder,
    "Settings" to Icons.Rounded.Settings, "Wallet" to Icons.Rounded.AccountBalanceWallet,
    "Health" to Icons.Rounded.Favorite, "Bank" to Icons.Rounded.AccountBalance,
    "Books" to Icons.Rounded.AutoStories, "News" to Icons.Rounded.Newspaper,
    "Store" to Icons.Rounded.ShoppingBag, "Phone" to Icons.Rounded.Call, "Contacts" to Icons.Rounded.Contacts,
    "Calculator" to Icons.Rounded.Calculate, "Translate" to Icons.Rounded.Translate,
    "Fitness" to Icons.Rounded.FitnessCenter, "Home" to Icons.Rounded.Home, "Scanner" to Icons.Rounded.DocumentScanner,
    "Compass" to Icons.Rounded.Explore, "Recorder" to Icons.Rounded.Mic, "Browser" to Icons.Rounded.Public,
    "Drive" to Icons.Rounded.Cloud, "Games" to Icons.Rounded.SportsEsports, "Travel" to Icons.Rounded.Flight,
    "Food" to Icons.Rounded.Restaurant,
)

internal val InitialScrollApps: List<ScrollApp> =
    AppList.mapIndexed { i, (name, icon) -> ScrollApp(name, icon, Hues.at(i)) }

private val Places = listOf(
    "Lisbon", "Kyoto", "Reykjavik", "Marrakesh", "Cape Town", "Oslo", "Havana", "Seoul",
    "Porto", "Tunis", "Bergen", "Hanoi", "Sintra", "Nara", "Tromso", "Chefchaouen",
    "Valparaiso", "Busan", "Hvar", "Sidi Bou Said", "Lofoten", "Hoi An", "Evora", "Kotor",
)

/** ScrollPhoto heights cycle through a few steps so the columns never line up. */
private val PhotoHeights = listOf(150.dp, 210.dp, 120.dp, 180.dp, 240.dp, 140.dp, 200.dp)

internal val InitialScrollPhotos: List<ScrollPhoto> =
    Places.mapIndexed { i, place -> ScrollPhoto(place, PhotoHeights[i % PhotoHeights.size], Hues.at(i + 2)) }

private val TaskTitles = listOf(
    "Draft the release notes", "Review the login flow", "Book the team offsite", "Fix the crash on resume",
    "Update the privacy page", "Plan the sprint demo", "Answer support tickets", "Record the onboarding video",
    "Clean up old branches", "Write the migration guide", "Pick the new app icon", "Test on a small phone",
    "Order new test devices", "Sync with the design team", "Prepare the board update", "Refresh the store screenshots",
    "Translate the settings", "Measure the cold start", "Archive last quarter", "Renew the domain",
    "Send the invoices", "Rotate the API keys", "Share the roadmap", "Close the beta survey",
)

private val TaskDays = listOf("Today", "Tomorrow", "Mon", "Tue", "Wed", "Thu", "Fri", "Next week")
private val TaskTags = listOf("Work" to Hues.Sky, "Team" to Hues.Mint, "Admin" to Hues.Butter, "Bug" to Hues.Rose)

internal val InitialScrollTasks: List<ScrollTask> =
    TaskTitles.mapIndexed { i, title ->
        val (tag, hue) = TaskTags[i % TaskTags.size]
        ScrollTask(title, TaskDays[i % TaskDays.size], tag, hue)
    }

internal const val AutoScrollRowCode = """
val listState = rememberLazyListState()

LazyRow(
    state = listState,
    modifier = Modifier.dragAutoScroll(
        state = dndState,
        lazyListState = listState,
    ),
) {
    items(products, key = { it.name }) {
        // Each card uses reorderableItem
    }
}
"""

internal const val AutoScrollGridCode = """
val gridState = rememberLazyGridState()

LazyVerticalGrid(
    columns = GridCells.Fixed(4),
    state = gridState,
    modifier = Modifier.dragAutoScroll(
        state = dndState,
        lazyGridState = gridState,
    ),
) {
    items(apps, key = { it.name }) {
        // Each tile uses reorderableItem
    }
}
"""

internal const val AutoScrollMasonryCode = """
val grid = rememberLazyStaggeredGridState()

LazyVerticalStaggeredGrid(
    columns = StaggeredGridCells.Fixed(2),
    state = grid,
    modifier = Modifier.dragAutoScroll(
        state = dndState,
        lazyStaggeredGridState = grid,
    ),
) {
    items(photos, key = { it.place }) {
        // Each photo uses reorderableItem
    }
}
"""

internal const val AutoScrollColumnCode = """
val scroll = rememberScrollState()

Column(
    modifier = Modifier
        .verticalScroll(scroll)
        .dragAutoScroll(
            state = dndState,
            scrollState = scroll,
            orientation = Orientation.Vertical,
        ),
) {
    // Each task row uses reorderableItem
}
"""
