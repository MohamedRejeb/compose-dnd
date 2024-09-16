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
package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

internal class DraggableItemState<T>(
    var key: Any,
    var data: T,
    var dropTargets: List<Any> = emptyList(),
    var dropStrategy: DropStrategy,

    var dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),
    var sizeDropAnimationSpec: AnimationSpec<Size> = SpringSpec(),

    var positionInRoot: Offset,
    var size: Size,

    var content: @Composable () -> Unit,
) {
    fun copy(): DraggableItemState<T> {
        return DraggableItemState(
            key = key,
            data = data,
            dropTargets = dropTargets,
            dropStrategy = dropStrategy,
            dropAnimationSpec = dropAnimationSpec,
            sizeDropAnimationSpec = sizeDropAnimationSpec,
            positionInRoot = positionInRoot,
            size = size,
            content = content
        )
    }
}
