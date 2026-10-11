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
package snapshots

import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import snapshots.Snapshots.HeldDrag
import snapshots.Snapshots.shot
import ui.AutoScrollDemoScreen
import ui.AutoScrollLayout
import ui.AxisLockedDragScreen
import ui.BetweenListsMode
import ui.BetweenListsScreen

class ListsSnapshots {
    @Test fun betweenLists() {
        shot("between") { BetweenListsScreen {} }
        shot("between_drag", drag = HeldDrag("Maya Chen", Offset(200f, 210f))) { BetweenListsScreen {} }
        shot("between_narrow", w = 360) { BetweenListsScreen {} }
        shot("between_insert", drag = HeldDrag("Maya Chen", Offset(200f, 96f))) {
            BetweenListsScreen(onBack = {}, initialMode = BetweenListsMode.Insert)
        }
    }

    @Test fun autoScroll() {
        AutoScrollLayout.entries.forEach { layout ->
            val name = "autoscroll_" + layout.name.lowercase()
            shot(name) { AutoScrollDemoScreen(onBack = {}, initialLayout = layout) }
        }
        shot("autoscroll_drag", drag = HeldDrag("Desk lamp", Offset(60f, 24f))) { AutoScrollDemoScreen {} }
        shot("autoscroll_griddrag", drag = HeldDrag("Maps", Offset(90f, 100f))) {
            AutoScrollDemoScreen(onBack = {}, initialLayout = AutoScrollLayout.Grid)
        }
        shot("autoscroll_masonrydrag", drag = HeldDrag("Kyoto", Offset(-120f, 120f))) {
            AutoScrollDemoScreen(onBack = {}, initialLayout = AutoScrollLayout.Masonry)
        }
    }

    @Test fun axisLock() {
        shot("axis") { AxisLockedDragScreen {} }
        shot("axis_drag", drag = HeldDrag("1x", Offset(110f, 60f))) { AxisLockedDragScreen {} }
        shot("axis_vertical", drag = HeldDrag("Login crash", Offset(-80f, 70f))) { AxisLockedDragScreen {} }
        shot("axis_free", drag = HeldDrag("Maya", Offset(120f, 80f))) { AxisLockedDragScreen {} }
    }
}
