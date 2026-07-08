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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.annotation.ExperimentalDndApi
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.dragHandle
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import com.mohamedrejeb.compose.dnd.drag.isDragging
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScreenScaffold
import components.GripDots
import theme.ExampleBrandColors

// -- Data model --

private enum class FieldType(
    val label: String,
    val icon: ImageVector,
    val placeholder: String,
) {
    ShortText("Short text", Icons.Rounded.TextFields, "Enter text..."),
    LongText("Paragraph", Icons.Rounded.Subject, "Write something longer..."),
    Email("Email", Icons.Rounded.Email, "name@example.com"),
    Number("Number", Icons.Rounded.Numbers, "0"),
    Checkbox("Checkbox", Icons.Rounded.CheckBox, "Option"),
}

private data class FormField(
    val id: Int,
    val type: FieldType,
)

/**
 * One payload type for both drag sources: palette items create a new field on
 * drop (the palette chip is never consumed), existing fields reorder.
 */
private sealed interface FormDragData {
    data class PaletteItem(
        val type: FieldType
    ) : FormDragData

    data class ExistingField(
        val field: FormField
    ) : FormDragData
}

// -- Screen --

@Composable
fun FormBuilderScreen(
    onBack: () -> Unit,
) {
    DemoScreenScaffold(
        title = "Form Builder",
        onBack = onBack,
    ) { paddingValues ->
        FormBuilderContent(
            paddingValues = paddingValues,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@OptIn(ExperimentalDndApi::class)
@Composable
private fun FormBuilderContent(
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val dndState = rememberDragAndDropState<FormDragData>()
    var fields by remember { mutableStateOf(emptyList<FormField>()) }
    var nextId by remember { mutableIntStateOf(1) }
    val listState = rememberLazyListState()

    fun newField(type: FieldType): FormField = FormField(id = nextId, type = type).also { nextId += 1 }

    fun insertAt(index: Int, type: FieldType) {
        fields = fields.toMutableList().apply { add(index.coerceIn(0, size), newField(type)) }
    }

    fun handleDropOnField(dropped: FormDragData, target: FormField) {
        when (dropped) {
            is FormDragData.PaletteItem -> {
                val index = fields.indexOfFirst { it.id == target.id }
                if (index != -1) insertAt(index, dropped.type)
            }

            // Reorder already happened in onDragEnter
            is FormDragData.ExistingField -> {
                Unit
            }
        }
    }

    fun handleEnterOnField(entered: FormDragData, target: FormField) {
        // Only existing fields reorder on hover; palette items insert on drop
        if (entered !is FormDragData.ExistingField) return
        if (entered.field.id == target.id) return

        fields = fields.toMutableList().apply {
            val targetIndex = indexOfFirst { it.id == target.id }
            if (targetIndex != -1) {
                removeAll { it.id == entered.field.id }
                add(targetIndex.coerceAtMost(size), entered.field)
            }
        }
    }

    DragAndDropContainer(
        state = dndState,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            FieldPalette(dndState = dndState)

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Your form",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .dragAutoScroll(
                        state = dndState,
                        lazyListState = listState,
                    ),
            ) {
                items(fields, key = { it.id }) { field ->
                    val isDragging = dndState.isDragging(field.id)
                    val isPaletteItemHovering = dndState.draggedItem?.data is FormDragData.PaletteItem &&
                        dndState.hoveredDropTargetKey == field.id

                    FormFieldRow(
                        field = field,
                        showInsertIndicator = isPaletteItemHovering,
                        onDelete = { fields = fields.filter { it.id != field.id } },
                        handleModifier = Modifier.dragHandle(
                            key = field.id,
                            state = dndState,
                        ),
                        modifier = Modifier
                            .animateItem()
                            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                            .reorderableItem(
                                key = field.id,
                                data = FormDragData.ExistingField(field),
                                state = dndState,
                                hasDragHandle = true,
                                dropStrategy = DropStrategy.CenterDistance,
                                onDragEnter = { state -> handleEnterOnField(state.data, field) },
                                onDrop = { state -> handleDropOnField(state.data, field) },
                                draggableContent = {
                                    FormFieldRow(
                                        field = field,
                                        onDelete = {},
                                        isDragShadow = true,
                                    )
                                },
                            ).fillMaxWidth(),
                    )
                }

                item(key = "append-slot") {
                    AppendSlot(
                        dndState = dndState,
                        isFormEmpty = fields.isEmpty(),
                        onDropData = { data ->
                            when (data) {
                                is FormDragData.PaletteItem -> {
                                    insertAt(fields.size, data.type)
                                }

                                is FormDragData.ExistingField -> {
                                    fields = fields.filter { it.id != data.field.id } + data.field
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

// -- Components --

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FieldPalette(
    dndState: DragAndDropState<FormDragData>,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp),
    ) {
        Text(
            text = "Drag a field into the form below. The palette stays put.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FieldType.entries.forEach { type ->
                PaletteChip(
                    type = type,
                    dndState = dndState,
                )
            }
        }
    }
}

@Composable
private fun PaletteChip(
    type: FieldType,
    dndState: DragAndDropState<FormDragData>,
) {
    // The palette key is stable and never removed, so the chip stays in place
    // while a copy is created on drop.
    PaletteChipContent(
        type = type,
        modifier = Modifier.draggableItem(
            key = "palette-${type.name}",
            data = FormDragData.PaletteItem(type),
            state = dndState,
            draggableContent = {
                PaletteChipContent(
                    type = type,
                    isDragShadow = true,
                )
            },
        ),
    )
}

@Composable
private fun PaletteChipContent(
    type: FieldType,
    isDragShadow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val accent = ExampleBrandColors.FormBuilder

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .then(
                if (isDragShadow) {
                    Modifier
                        .graphicsLayer { rotationZ = -2f }
                        .shadow(elevation = 8.dp, shape = shape)
                } else {
                    Modifier
                }
            ).clip(shape)
            .background(accent.copy(alpha = if (isDragShadow) 0.9f else 0.14f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Icon(
            imageVector = type.icon,
            contentDescription = null,
            tint = if (isDragShadow) Color.White else accent,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isDragShadow) Color.White else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FormFieldRow(
    field: FormField,
    onDelete: () -> Unit,
    isDragShadow: Boolean = false,
    showInsertIndicator: Boolean = false,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(14.dp)
    val accent = ExampleBrandColors.FormBuilder

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(
                if (isDragShadow) {
                    Modifier
                        .graphicsLayer { rotationZ = -1.5f }
                        .shadow(elevation = 10.dp, shape = shape)
                } else {
                    Modifier
                }
            ).clip(shape)
            .background(
                if (showInsertIndicator) {
                    accent.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            ).drawBehind {
                // The new field is inserted above this row, so mark its top edge
                if (showInsertIndicator) {
                    drawRoundRect(
                        color = accent,
                        size = Size(width = size.width, height = 3.dp.toPx()),
                        cornerRadius = CornerRadius(1.5.dp.toPx()),
                    )
                }
            }.padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Box(modifier = handleModifier.padding(end = 12.dp)) {
            GripDots(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = field.type.icon,
                    contentDescription = null,
                    tint = ExampleBrandColors.FormBuilder,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = field.type.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(6.dp))

            // Mock input preview
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (field.type == FieldType.LongText) 56.dp else 36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp),
                    ).padding(horizontal = 10.dp),
            ) {
                Text(
                    text = field.type.placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Remove field",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun AppendSlot(
    dndState: DragAndDropState<FormDragData>,
    isFormEmpty: Boolean,
    onDropData: (FormDragData) -> Unit,
) {
    val isHovered = dndState.hoveredDropTargetKey == "append-slot"
    val accent = ExampleBrandColors.FormBuilder

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isFormEmpty) 120.dp else 56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isHovered) accent.copy(alpha = 0.1f) else Color.Transparent)
            .drawBehind {
                drawRoundRect(
                    color = if (isHovered) accent else Color.Gray.copy(alpha = 0.5f),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
                    ),
                )
            }.dropTarget(
                key = "append-slot",
                state = dndState,
                dropAnimationEnabled = false,
                onDrop = { state -> onDropData(state.data) },
            ),
    ) {
        Text(
            text = if (isFormEmpty) "Drag a field here to start your form" else "Drop here to add at the end",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
