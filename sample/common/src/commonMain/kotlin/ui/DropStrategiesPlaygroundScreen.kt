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
package ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import components.DemoScaffold
import components.SegmentedControl
import components.dragPlaceholder
import components.lifted
import components.wellState
import theme.SampleTheme

private const val TrayName = "Tray"
private val TrayHeight = 76.dp
private const val HoveredZ = 1f

private val DropStrategiesCode = """
    Modifier.draggableItem(
        key = "beach",
        data = photo,
        state = dndState,
        dropStrategy =
            DropStrategy.CenterDistance,
        draggableContent = { PhotoToken() },
    )

    Modifier.dropTarget(
        key = "Shared",
        state = dndState,
        onDrop = { location = "Shared" },
    )
"""

/**
 * `DropStrategy` decides which target wins when the dragged item overlaps
 * several. Each setup has its own photo; drag it across the targets and switch
 * strategies to see which one lights up.
 */
@Composable
fun DropStrategiesPlaygroundScreen(
    onBack: () -> Unit,
) {
    DropStrategiesPlayground(initialOption = StrategyOption.Surface, onBack = onBack)
}

/** The screen with a chosen starting strategy, for previews and snapshots. */
@Composable
internal fun DropStrategiesPlayground(
    initialOption: StrategyOption,
    onBack: () -> Unit,
) {
    var option by remember { mutableStateOf(initialOption) }

    DemoScaffold(
        title = "Drop strategies",
        hint = "Pick a strategy, then drag a photo across the targets.",
        code = DropStrategiesCode,
        onBack = onBack,
    ) { insets ->
        DropStrategiesContent(
            option = option,
            onOptionChange = { option = it },
            insets = insets,
        )
    }
}

@Composable
private fun DropStrategiesContent(
    option: StrategyOption,
    onOptionChange: (StrategyOption) -> Unit,
    insets: PaddingValues,
) {
    val state = rememberDragAndDropState<String>()

    DragAndDropContainer(
        state = state,
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(insets)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            SegmentedControl(
                options = StrategyOption.entries,
                selected = option,
                onSelect = onOptionChange,
                label = { it.label },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = option.explanation,
                style = MaterialTheme.typography.bodyMedium,
                color = SampleTheme.colors.inkMuted,
                minLines = 2,
                modifier = Modifier.padding(top = 12.dp),
            )
            Spacer(Modifier.height(20.dp))
            StrategySetupView(OverlappingSetup, option.strategy, state)
            Spacer(Modifier.height(20.dp))
            StrategySetupView(SizesSetup, option.strategy, state)
        }
    }
}

@Composable
private fun StrategySetupView(
    setup: StrategySetup,
    strategy: DropStrategy,
    state: DragAndDropState<String>,
) {
    // Where the photo sits: a target name, or the tray.
    var location by remember { mutableStateOf(TrayName) }
    val targetKeys = remember(setup) { setup.targets.map { it.name } + trayKey(setup) }
    val hovered = setup.targets.firstOrNull { state.isDropTargetHovered(it.name) }?.name
    val photo: @Composable (Modifier) -> Unit = { modifier ->
        DraggablePhoto(setup, strategy, targetKeys, state, modifier)
    }

    Column {
        SetupHeader(
            title = setup.title,
            hovered = hovered,
            droppedIn = location.takeIf { it != TrayName },
        )
        PhotoTray(
            state = wellState(
                dragging = state.isDragging(setup.photoKey) && location != TrayName,
                hovered = state.isDropTargetHovered(trayKey(setup)),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(TrayHeight)
                .dropTarget(key = trayKey(setup), state = state, onDrop = { location = TrayName }),
        ) {
            if (location == TrayName) photo(Modifier)
        }
        Spacer(Modifier.height(8.dp))
        TargetsArea(
            setup = setup,
            state = state,
            location = location,
            onDrop = { location = it },
            photo = photo,
        )
    }
}

@Composable
private fun TargetsArea(
    setup: StrategySetup,
    state: DragAndDropState<String>,
    location: String,
    onDrop: (String) -> Unit,
    photo: @Composable (Modifier) -> Unit,
) {
    val dragging = state.isDragging(setup.photoKey)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = setup.targetsHeight),
    ) {
        val width = maxWidth
        setup.targets.forEach { target ->
            val isHovered = state.isDropTargetHovered(target.name)
            TargetWell(
                name = target.name,
                state = wellState(dragging = dragging, hovered = isHovered),
                labelAlignment = target.labelAlignment,
                modifier = Modifier
                    .zIndex(if (isHovered) HoveredZ else 0f)
                    .placed(this, target, width)
                    .dropTarget(key = target.name, state = state, onDrop = { onDrop(target.name) }),
            )
        }
        // The photo sits on top of the target it was dropped in.
        setup.targets.firstOrNull { it.name == location }?.let { target ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.zIndex(HoveredZ + 1f).placed(this, target, width),
            ) {
                photo(Modifier)
            }
        }
    }
}

@Composable
private fun DraggablePhoto(
    setup: StrategySetup,
    strategy: DropStrategy,
    targetKeys: List<String>,
    state: DragAndDropState<String>,
    modifier: Modifier = Modifier,
) {
    PhotoToken(
        hue = setup.photoHue,
        modifier = modifier
            .dragPlaceholder(active = state.isDragging(setup.photoKey))
            .draggableItem(
                key = setup.photoKey,
                data = setup.photoKey,
                state = state,
                dropTargets = targetKeys,
                dropStrategy = strategy,
                draggableContent = { PhotoToken(hue = setup.photoHue, modifier = Modifier.lifted()) },
            ),
    )
}

private fun trayKey(setup: StrategySetup): String = "${setup.photoKey}-tray"

private fun Modifier.placed(
    scope: BoxScope,
    target: TargetSpec,
    width: Dp,
): Modifier = with(scope) { align(Alignment.CenterStart) }
    .offset(x = width * target.startFraction)
    .width(width * target.widthFraction)
    .height(target.height)
