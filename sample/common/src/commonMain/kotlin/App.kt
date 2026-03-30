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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import navigation.DropStrategiesPlaygroundRoute
import navigation.HomeRoute
import navigation.ItemToItemOneDirectionRoute
import navigation.ItemToItemTwoDirectionsRoute
import navigation.ListToListWithReorderRoute
import navigation.ListToListWithoutReorderRoute
import navigation.ReorderListRoute
import theme.AppTheme
import ui.DropStrategiesPlaygroundScreen
import ui.HomeScreen
import ui.ItemToItemOneDirectionScreen
import ui.ItemToItemTwoDirectionsScreen
import ui.ListToListWithReorderScreen
import ui.ListToListWithoutReorderScreen
import ui.ReorderListScreen

@Composable
fun App() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = HomeRoute,
            ) {
                composable<HomeRoute> {
                    HomeScreen(
                        onNavigateToItemToItemOneDirection = {
                            navController.navigate(ItemToItemOneDirectionRoute)
                        },
                        onNavigateToItemToItemTwoDirections = {
                            navController.navigate(ItemToItemTwoDirectionsRoute)
                        },
                        onNavigateToReorderList = {
                            navController.navigate(ReorderListRoute)
                        },
                        onNavigateToListToListWithoutReorder = {
                            navController.navigate(ListToListWithoutReorderRoute)
                        },
                        onNavigateToListToListWithReorder = {
                            navController.navigate(ListToListWithReorderRoute)
                        },
                        onNavigateToDropStrategiesPlayground = {
                            navController.navigate(DropStrategiesPlaygroundRoute)
                        },
                    )
                }
                composable<ItemToItemOneDirectionRoute> {
                    ItemToItemOneDirectionScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<ItemToItemTwoDirectionsRoute> {
                    ItemToItemTwoDirectionsScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<ReorderListRoute> {
                    ReorderListScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<ListToListWithoutReorderRoute> {
                    ListToListWithoutReorderScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<ListToListWithReorderRoute> {
                    ListToListWithReorderScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
                composable<DropStrategiesPlaygroundRoute> {
                    DropStrategiesPlaygroundScreen(
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}
