package ui

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import components.RedBox
import kotlinx.coroutines.launch

object ListToListWithoutReorderScreen: Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "From list to list (without reorder)",
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navigator.pop()
                            }
                        ) {
                            Icon(
                                Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            ListToListWithoutReorderContent(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(paddingValues)
                    .padding(20.dp)
            )
        }
    }
}

@Composable
private fun ListToListWithoutReorderContent(
    modifier: Modifier = Modifier,
) {
    var listOne by remember {
        mutableStateOf(
            listOf(
                "item1",
                "item2",
                "item3",
                "item4",
            )
        )
    }

    var listTwo by remember {
        mutableStateOf(
            listOf(
                "item5",
                "item6",
                "item7",
                "item8",
            )
        )
    }

    val dragAndDropState = rememberDragAndDropState<String>()

    val lazyListState = rememberLazyListState()

    DragAndDropContainer(
        state = dragAndDropState,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                state = lazyListState,
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(
                        width = 1.dp,
                        color = with(MaterialTheme.colorScheme) {
                            if (dragAndDropState.hoveredDropTargetKey == "listOne") primary else onSurface
                        },
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = "listOne",
                        state = dragAndDropState,
                        dropAnimationEnabled = false,
                        onDrop = { state ->
                            listTwo = listTwo.toMutableList().apply {
                                val isRemoved = remove(state.data)
                                if (!isRemoved) return@dropTarget
                            }

                            listOne = listOne.toMutableList().apply {
                                add(state.data)
                            }
                        },
                    )
            ) {
                items(listOne, key = { it }) { item ->
                    DraggableItem(
                        state = dragAndDropState,
                        key = item,
                        data = item,
                        dropTargets = listOf("listTwo"),
                        draggableContent = {
                            RedBox(
                                isDragShadow = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            )
                        },
                        modifier = Modifier
                    ) {
                        RedBox(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                }
                                .fillMaxWidth()
                                .height(60.dp)
                        )
                    }
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                state = lazyListState,
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(
                        width = 1.dp,
                        color = with(MaterialTheme.colorScheme) {
                            if (dragAndDropState.hoveredDropTargetKey == "listTwo") primary else onSurface
                        },
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = "listTwo",
                        state = dragAndDropState,
                        dropAnimationEnabled = false,
                        onDrop = { state ->
                            listOne = listOne.toMutableList().apply {
                                val isRemoved = remove(state.data)
                                if (!isRemoved) return@dropTarget
                            }

                            listTwo = listTwo.toMutableList().apply {
                                add(state.data)
                            }
                        },
                    )
            ) {
                items(listTwo, key = { it }) { item ->
                    DraggableItem(
                        state = dragAndDropState,
                        key = item,
                        data = item,
                        dropTargets = listOf("listOne"),
                        draggableContent = {
                            RedBox(
                                isDragShadow = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                            )
                        },
                        modifier = Modifier
                    ) {
                        RedBox(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                }
                                .fillMaxWidth()
                                .height(60.dp)
                        )
                    }
                }
            }
        }
    }
}
