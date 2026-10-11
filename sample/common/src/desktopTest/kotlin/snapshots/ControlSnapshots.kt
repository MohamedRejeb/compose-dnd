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
import ui.ConditionalDrop
import ui.ConditionalDropScreen
import ui.DropStrategiesPlayground
import ui.DropStrategiesPlaygroundScreen
import ui.FileKind
import ui.Folder
import ui.FolderCapacity
import ui.PinnedChatsScreen
import ui.SampleFiles
import ui.StrategyOption

/** Conditional drop, drop strategies and pinned chats, at rest and mid-drag. */
class ControlSnapshots {
    @Test fun screens() {
        shot("conditional") { ConditionalDropScreen {} }
        shot("conditional_narrow", w = 360, h = 780) { ConditionalDropScreen {} }
        shot("conditional_reject", drag = HeldDrag("invoice.pdf", Offset(-200f, 360f))) {
            ConditionalDropScreen {}
        }
        shot("conditional_accept", drag = HeldDrag("beach.jpg", Offset(0f, 320f))) {
            ConditionalDropScreen {}
        }
        // Photos already holds three images: the fourth is refused for capacity.
        val threeImages = SampleFiles.filter { it.kind == FileKind.Image }.take(FolderCapacity)
        shot("conditional_full", drag = HeldDrag("sunset.jpg", Offset(-190f, 330f))) {
            ConditionalDrop(initialPlacement = threeImages.associateWith { Folder.Photos }, onBack = {})
        }

        shot("strategies") { DropStrategiesPlaygroundScreen {} }
        shot("strategies_narrow", w = 360, h = 780) { DropStrategiesPlaygroundScreen {} }
        // Straddle Medium and Large: the overlap favors Large, the centers favor Medium.
        val straddle = HeldDrag("Targets of different sizes", Offset(25f, 122f), touchOffset = Offset(109f, 62f))
        shot("strategies_drag", drag = straddle) { DropStrategiesPlaygroundScreen {} }
        shot("strategies_center", drag = straddle) {
            DropStrategiesPlayground(initialOption = StrategyOption.Center, onBack = {})
        }

        shot("pinned") { PinnedChatsScreen {} }
        shot("pinned_narrow", w = 360, h = 780) { PinnedChatsScreen {} }
        shot("pinned_drag", drag = HeldDrag("Alex", Offset(0f, -150f), longPress = true)) {
            PinnedChatsScreen {}
        }
    }
}
