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
import ui.DragAndDropScreen
import ui.DragHandleReorderScreen
import ui.ReorderListScreen

class BasicsSnapshots {
    @Test fun screens() {
        shot("dnd") { DragAndDropScreen {} }
        // Invoice.pdf carried from Downloads over Documents.
        shot("dnd_drag", drag = HeldDrag("Invoice.pdf", Offset(0f, 430f))) { DragAndDropScreen {} }

        shot("reorder") { ReorderListScreen {} }
        shot("reorder_drag", drag = HeldDrag("Kotlin", Offset(0f, 180f), longPress = true)) {
            ReorderListScreen {}
        }

        shot("handle") { DragHandleReorderScreen {} }
        // The label node is the whole toggle area; shift left onto the grip.
        val onGrip = Offset(-193f, 0f)
        shot("handle_drag", drag = HeldDrag("Wi-Fi", Offset(0f, 150f), touchOffset = onGrip)) {
            DragHandleReorderScreen {}
        }
    }
}
