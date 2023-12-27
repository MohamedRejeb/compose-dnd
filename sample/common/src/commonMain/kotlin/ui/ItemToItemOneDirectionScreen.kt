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

object ItemToItemOneDirectionScreen: Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Item to Item (one direction)",
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
            ItemToItemOneDirectionScreenContent(
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
private fun ItemToItemOneDirectionScreenContent(
    modifier: Modifier = Modifier,
) {
    val dragAndDropState = rememberDragAndDropState<Int>()

    var isDropped by remember {
        mutableStateOf(false)
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
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = RoundedCornerShape(24.dp),
                    )
            ) {
                if (!isDropped) {
                    DraggableItem(
                        state = dragAndDropState,
                        key = 1,
                        data = 1,
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

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = with(MaterialTheme.colorScheme) {
                            if (dragAndDropState.hoveredDropTargetKey == "targetKey") primary else onSurface
                        },
                        shape = RoundedCornerShape(24.dp),
                    )
                    .dropTarget(
                        key = "targetKey",
                        state = dragAndDropState,
                        onDrop = {
                            isDropped = true
                        }
                    )
            ) {
                if (isDropped)
                    RedBox(
                        modifier = Modifier
                            .size(200.dp)
                    )
            }
        }
    }
}
