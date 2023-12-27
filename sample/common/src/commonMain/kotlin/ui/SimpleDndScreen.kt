package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import cafe.adriel.voyager.core.screen.Screen
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.DraggableItemUi

object SimpleDndScreen: Screen {

    @Composable
    override fun Content() {
        val dragAndDropState = rememberDragAndDropState<Int>()
        val itemPosition = remember { mutableStateOf(1) }

        DragAndDropContainer(
            state = dragAndDropState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Green)
                        .dropTarget(
                            key = "1",
                            state = dragAndDropState,
                            onDrop = {
                                itemPosition.value = 1
                            }
                        )
                ) {
                    if (itemPosition.value == 1) {
                        DraggableItem(
                            state = dragAndDropState,
                            key = 1,
                            data = 1,
                            dropTargets = listOf("2"),
                        ) {
                            DraggableItemUi(
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = if (isDragging) 0.5f else 1f
                                    }
                            )
                        }
                    }
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Red)
                        .dropTarget(
                            key = "2",
                            state = dragAndDropState,
                            onDrop = {
                                itemPosition.value = 2
                            }
                        )
                ) {
                    if (itemPosition.value == 2) {
                        DraggableItem(
                            state = dragAndDropState,
                            key = 2,
                            data = 2,
                            dropTargets = listOf("1"),
                        ) {
                            DraggableItemUi(
                                modifier = Modifier
                                    .graphicsLayer {
                                        alpha = if (isDragging) 0.5f else 1f
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}