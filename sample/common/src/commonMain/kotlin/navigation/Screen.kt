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
package navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object ItemToItemOneDirectionRoute

@Serializable
data object ItemToItemTwoDirectionsRoute

@Serializable
data object ReorderListRoute

@Serializable
data object ListToListWithoutReorderRoute

@Serializable
data object ListToListWithReorderRoute

@Serializable
data object DropStrategiesPlaygroundRoute

@Serializable
data object DragHandleReorderRoute

@Serializable
data object AxisLockedDragRoute

@Serializable
data object ConditionalDropRoute

@Serializable
data object AutoScrollDemoRoute
