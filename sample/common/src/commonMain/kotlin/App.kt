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
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import navigation.Demo
import navigation.DemoRoute
import navigation.HomeRoute
import theme.AppTheme
import ui.AutoScrollDemoScreen
import ui.AxisLockedDragScreen
import ui.BetweenListsScreen
import ui.ConditionalDropScreen
import ui.DragAndDropScreen
import ui.DragHandleReorderScreen
import ui.DropStrategiesPlaygroundScreen
import ui.FormBuilderScreen
import ui.HomeScreen
import ui.KanbanBoardScreen
import ui.PhotoGalleryScreen
import ui.PinnedChatsScreen
import ui.ReorderListScreen
import ui.StickyNotesBoardScreen

@Composable
fun App() {
    AppTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = HomeRoute,
        ) {
            composable<HomeRoute> {
                HomeScreen(
                    onOpen = { demo -> navController.navigate(DemoRoute(demo)) },
                )
            }
            composable<DemoRoute> { entry ->
                DemoScreen(
                    demo = entry.toRoute<DemoRoute>().demo,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

@Composable
private fun DemoScreen(
    demo: Demo,
    onBack: () -> Unit,
) {
    when (demo) {
        Demo.Kanban -> KanbanBoardScreen(onBack)
        Demo.Photos -> PhotoGalleryScreen(onBack)
        Demo.StickyNotes -> StickyNotesBoardScreen(onBack)
        Demo.FormBuilder -> FormBuilderScreen(onBack)
        Demo.PinnedChats -> PinnedChatsScreen(onBack)
        Demo.DragAndDrop -> DragAndDropScreen(onBack)
        Demo.ReorderList -> ReorderListScreen(onBack)
        Demo.DragHandle -> DragHandleReorderScreen(onBack)
        Demo.BetweenLists -> BetweenListsScreen(onBack)
        Demo.AutoScroll -> AutoScrollDemoScreen(onBack)
        Demo.AxisLock -> AxisLockedDragScreen(onBack)
        Demo.ConditionalDrop -> ConditionalDropScreen(onBack)
        Demo.DropStrategies -> DropStrategiesPlaygroundScreen(onBack)
    }
}
