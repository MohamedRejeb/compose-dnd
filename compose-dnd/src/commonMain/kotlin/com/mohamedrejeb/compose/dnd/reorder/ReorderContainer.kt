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
package com.mohamedrejeb.compose.dnd.reorder

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi

/**
 * Container for reorder,
 * All reorderable items should be inside this container
 *
 * @param state The state of the reorder
 * @param modifier [Modifier]
 * @param content content of the container
 */
@OptIn(ExperimentalDndApi::class)
@Composable
fun <T> ReorderContainer(
    state: ReorderState<T>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    DragAndDropContainer(
        state = state.dndState,
        modifier = modifier,
        content = content,
    )
}
