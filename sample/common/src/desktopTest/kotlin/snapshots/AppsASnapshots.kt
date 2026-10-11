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
import ui.FieldType
import ui.FormBuilderScreen
import ui.KanbanBoardScreen

class AppsASnapshots {
    private val filledForm = listOf(FieldType.ShortText, FieldType.Email, FieldType.LongText, FieldType.Checkbox)

    @Test fun kanban() {
        shot("kanban") { KanbanBoardScreen {} }
        // Carry the first card down its column. Each of the 12 steps stays under the touch
        // slop, so the long press fires before the lists see a scroll.
        shot("kanban_drag", drag = HeldDrag("Set up CI pipeline", Offset(60f, 200f), longPress = true)) {
            KanbanBoardScreen {}
        }
        // Grab a column by its header.
        shot("kanban_column", drag = HeldDrag("To do", Offset(90f, 20f))) { KanbanBoardScreen {} }
        shot("kanban_wide", w = 1400, h = 860) { KanbanBoardScreen {} }
    }

    @Test fun form() {
        shot("form") { FormBuilderScreen {} }
        shot("form_drag", drag = HeldDrag("Email", Offset(-40f, 315f))) { FormBuilderScreen {} }
        shot("form_filled") { FormBuilderScreen(onBack = {}, initialFields = filledForm) }
        // A palette chip hovering between two fields shows the insertion line.
        shot("form_filled_drag", drag = HeldDrag("Number", Offset(0f, 300f))) {
            FormBuilderScreen(onBack = {}, initialFields = filledForm)
        }
        shot("form_reorder", drag = HeldDrag("Full name", Offset(0f, 110f), touchOffset = Offset(-51f, 25f))) {
            FormBuilderScreen(onBack = {}, initialFields = filledForm)
        }
    }
}
