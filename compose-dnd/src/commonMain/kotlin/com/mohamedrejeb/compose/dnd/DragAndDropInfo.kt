package com.mohamedrejeb.compose.dnd

import androidx.compose.runtime.staticCompositionLocalOf

internal interface DragAndDropInfo {
    /**
     * Whether the current composition is a shadow composition or not.
     */
    val isShadow: Boolean
}

internal class DragAndDropInfoImpl(
    override val isShadow: Boolean,
) : DragAndDropInfo

internal val LocalDragAndDropInfo = staticCompositionLocalOf<DragAndDropInfo> {
    DragAndDropInfoImpl(isShadow = false)
}
