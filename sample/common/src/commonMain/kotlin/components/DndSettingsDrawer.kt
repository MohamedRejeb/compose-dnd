/*
 * Copyright 2025, Mohamed Ben Rejeb and the Compose Dnd project contributors
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
package components

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DndSettingsDrawer(
    drawerState: DrawerState,
    dragAfterLongPress: Boolean,
    onDragAfterLongPressChange: (Boolean) -> Unit,
    requireFirstDownUnconsumed: Boolean,
    onRequireFirstDownUnconsumedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true,
        drawerContent = {
            ModalDrawerSheet {
                DndSettingsPanel(
                    dragAfterLongPress = dragAfterLongPress,
                    onDragAfterLongPressChange = onDragAfterLongPressChange,
                    requireFirstDownUnconsumed = requireFirstDownUnconsumed,
                    onRequireFirstDownUnconsumedChange = onRequireFirstDownUnconsumedChange,
                )
            }
        },
        content = content,
    )
}
