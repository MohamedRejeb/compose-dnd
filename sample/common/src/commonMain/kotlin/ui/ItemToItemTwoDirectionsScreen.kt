package ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import components.RedBox

object ItemToItemTwoDirectionsScreen: Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Item to Item (two directions)",
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
            ItemToItemTwoDirectionsScreenContent(
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
private fun ItemToItemTwoDirectionsScreenContent(
    modifier: Modifier = Modifier,
) {
    val dragAndDropState = rememberDragAndDropState<Int>()

    var itemIndex by remember {
        mutableStateOf(0)
    }

    DragAndDropContainer(
        state = dragAndDropState,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = with(MaterialTheme.colorScheme) {
                            if (dragAndDropState.hoveredDropTargetKey == 0) primary else onSurface
                        },
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = 0,
                        state = dragAndDropState,
                        onDrop = { state ->
                            if (state.data == 1)
                                itemIndex = 0
                        }
                    )
            ) {
                if (itemIndex == 0)
                    DraggableItem(
                        state = dragAndDropState,
                        key = 0,
                        data = 0,
                        dropTargets = listOf(1),
                    ) {
                        RedBox(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                }
                                .size(200.dp)
                        )
                    }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = with(MaterialTheme.colorScheme) {
                            if (dragAndDropState.hoveredDropTargetKey == 1) primary else onSurface
                        },
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = 1,
                        state = dragAndDropState,
                        onDrop = { state ->
                            if (state.data == 0)
                                itemIndex = 1
                        }
                    )
            ) {
                if (itemIndex == 1)
                    DraggableItem(
                        state = dragAndDropState,
                        key = 1,
                        data = 1,
                        dropTargets = listOf(0),
                    ) {
                        RedBox(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (isDragging) 0f else 1f
                                }
                                .size(200.dp)
                        )
                    }
            }
        }
    }
}