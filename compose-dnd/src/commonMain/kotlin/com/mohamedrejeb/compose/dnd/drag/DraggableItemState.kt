package com.mohamedrejeb.compose.dnd.drag

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

internal class DraggableItemState<T>(
    var key: Any,
    var data: T,
    var dropTargets: List<Any> = emptyList(),

    var dropAnimationSpec: AnimationSpec<Offset> = SpringSpec(),

    var positionInRoot: Offset,
    var size: Size,

    val content: @Composable () -> Unit,
) {
//    var dragStartPositionInRoot: Offset = positionInRoot
//    var dragStartOffset: Offset = positionInRoot
//
//    val dragPosition: MutableState<Offset> = mutableStateOf(positionInRoot)
//    val dragPositionAnimatable: Animatable<Offset, AnimationVector2D> = Animatable(Offset.Zero, Offset.VectorConverter)
}
