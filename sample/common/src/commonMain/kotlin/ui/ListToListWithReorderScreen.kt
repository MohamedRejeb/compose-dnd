package ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState
import components.RedBox
import kotlinx.coroutines.launch
import utils.handleLazyListScroll

object ListToListWithReorderScreen: Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "From list to list (with reorder)",
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
            ListToListWithReorderContent(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(paddingValues)
                    .padding(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalDndApi::class)
@Composable
private fun ListToListWithReorderContent(
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

    val scope = rememberCoroutineScope()

    val reorderState = rememberReorderState<String>()

    val lazyListStateOne = rememberLazyListState()
    val lazyListStateTwo = rememberLazyListState()

    ReorderContainer(
        state = reorderState,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                state = lazyListStateOne,
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = "listOne",
                        state = reorderState.dndState,
                        dropAnimationEnabled = false,
                        onDragEnter = { state ->
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
                    ReorderableItem(
                        state = reorderState,
                        key = item,
                        data = item,
                        zIndex = 1f,
                        onDragEnter = { state ->
                            listOne = listOne.toMutableList().apply {
                                val index = indexOf(item)
                                if (index == -1) return@ReorderableItem
                                if (!remove(state.data))
                                    // If the item is not in listOne, it means it's coming from the listTwo
                                    listTwo = listTwo.toMutableList().apply {
                                        remove(state.data)
                                    }

                                add(index, state.data)

                                scope.launch {
                                    handleLazyListScroll(
                                        lazyListState = lazyListStateOne,
                                        dropIndex = index,
                                    )
                                }
                            }
                        },
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
                state = lazyListStateTwo,
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = "listTwo",
                        state = reorderState.dndState,
                        dropAnimationEnabled = false,
                        onDragEnter = { state ->
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
                    ReorderableItem(
                        state = reorderState,
                        key = item,
                        data = item,
                        zIndex = 1f,
                        onDragEnter = { state ->
                            listTwo = listTwo.toMutableList().apply {
                                val index = indexOf(item)
                                if (index == -1) return@ReorderableItem
                                if (!remove(state.data))
                                    // If the item is not in listTwo, it means it's coming from the listOne
                                    listOne = listOne.toMutableList().apply {
                                        remove(state.data)
                                    }

                                add(index, state.data)

                                scope.launch {
                                    handleLazyListScroll(
                                        lazyListState = lazyListStateTwo,
                                        dropIndex = index,
                                    )
                                }
                            }
                        },
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
