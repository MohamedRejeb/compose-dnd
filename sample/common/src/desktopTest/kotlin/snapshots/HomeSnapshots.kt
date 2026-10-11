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
import ui.HomeScreen

class HomeSnapshots {
    @Test fun home() {
        shot("home") { HomeScreen {} }
        shot("home_tall", h = 1900) { HomeScreen {} }
        shot("home_drag", h = 1900, drag = HeldDrag("Reorder a list", Offset(0f, 150f), touchOffset = Offset(-190f, 0f))) {
            HomeScreen {}
        }
    }
}
