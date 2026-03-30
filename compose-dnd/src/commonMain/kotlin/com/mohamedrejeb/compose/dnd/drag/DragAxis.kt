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

import androidx.compose.ui.geometry.Offset

/**
 * Axis constraint for drag movement.
 */
enum class DragAxis {
    /**
     * Drag freely on both axes.
     */
    Free,

    /**
     * Drag only on horizontal axis. Vertical movement is zeroed.
     */
    Horizontal,

    /**
     * Drag only on vertical axis. Horizontal movement is zeroed.
     */
    Vertical,
}

/**
 * Apply axis constraint to a drag offset.
 */
internal fun DragAxis.applyConstraint(offset: Offset): Offset =
    when (this) {
        DragAxis.Free -> offset
        DragAxis.Horizontal -> Offset(offset.x, 0f)
        DragAxis.Vertical -> Offset(0f, offset.y)
    }
