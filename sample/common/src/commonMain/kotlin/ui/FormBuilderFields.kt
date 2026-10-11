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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Subject
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.draggableItem
import components.GripDots
import components.ItemSurface
import components.SectionLabel
import components.lifted
import theme.SampleTheme

// -- Data model --

internal enum class FieldType(
    val paletteLabel: String,
    val icon: ImageVector,
    val fieldLabel: String,
    val placeholder: String,
) {
    ShortText("Short text", Icons.Rounded.TextFields, "Full name", "Ada Lovelace"),
    LongText("Paragraph", Icons.Rounded.Subject, "Anything we should know?", "Dietary needs, access, questions"),
    Email("Email", Icons.Rounded.Email, "Work email", "ada@example.com"),
    Number("Number", Icons.Rounded.Numbers, "Number of guests", "1"),
    Checkbox("Checkbox", Icons.Rounded.CheckBox, "Send me the agenda", ""),
}

internal data class FormField(
    val id: Int,
    val type: FieldType,
)

/**
 * One payload type for both drag sources: palette items create a new field on
 * drop (the palette chip is never consumed), existing fields reorder.
 */
internal sealed interface FormDragData {
    data class PaletteItem(
        val type: FieldType,
    ) : FormDragData

    data class ExistingField(
        val field: FormField,
    ) : FormDragData
}

// -- Palette --

private const val ChipTilt = 2f

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FieldPalette(
    dndState: DragAndDropState<FormDragData>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(SampleTheme.shapes.well)
            .background(SampleTheme.colors.well)
            .padding(16.dp),
    ) {
        SectionLabel(text = "Fields", trailing = "Drag one into the form")
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FieldType.entries.forEach { type ->
                // The key never leaves the palette, so the chip stays put and
                // each drop creates a new field.
                PaletteChip(
                    type = type,
                    modifier = Modifier.draggableItem(
                        key = "palette-${type.name}",
                        data = FormDragData.PaletteItem(type),
                        state = dndState,
                        draggableContent = {
                            PaletteChip(
                                type = type,
                                modifier = Modifier.lifted(shape = SampleTheme.shapes.pill, tilt = ChipTilt),
                            )
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun PaletteChip(
    type: FieldType,
    modifier: Modifier = Modifier,
) {
    ItemSurface(
        shape = SampleTheme.shapes.pill,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
        ) {
            Icon(
                imageVector = type.icon,
                contentDescription = null,
                tint = SampleTheme.colors.inkMuted,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = type.paletteLabel,
                style = MaterialTheme.typography.labelLarge,
                color = SampleTheme.colors.ink,
            )
        }
    }
}

// -- Form fields --

private val HandleWidth = 40.dp
private val InputHeight = 40.dp
private val ParagraphHeight = 72.dp
private val CheckboxSize = 20.dp
private val InsertLineHeight = 3.dp

@Composable
internal fun FormFieldRow(
    field: FormField,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = handleModifier.size(width = HandleWidth, height = InputHeight + 24.dp),
        ) {
            GripDots(color = SampleTheme.colors.inkMuted.copy(alpha = 0.6f))
        }
        Box(modifier = Modifier.weight(1f)) {
            FieldPreview(field.type)
        }
        RemoveButton(onClick = onRemove)
    }
}

/** A field the way it looks in the finished form: a label over an input. */
@Composable
private fun FieldPreview(type: FieldType) {
    if (type == FieldType.Checkbox) {
        CheckboxPreview(type)
        return
    }
    Column {
        Text(
            text = type.fieldLabel,
            style = MaterialTheme.typography.labelLarge,
            color = SampleTheme.colors.ink,
        )
        Spacer(Modifier.height(6.dp))
        InputBox(
            placeholder = type.placeholder,
            height = if (type == FieldType.LongText) ParagraphHeight else InputHeight,
        )
    }
}

@Composable
private fun InputBox(
    placeholder: String,
    height: Dp,
) {
    val colors = SampleTheme.colors
    val shape = SampleTheme.shapes.small
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(colors.canvas)
            .border(1.dp, colors.line, shape)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.inkMuted,
            maxLines = 1,
        )
    }
}

@Composable
private fun CheckboxPreview(type: FieldType) {
    val colors = SampleTheme.colors
    val shape = SampleTheme.shapes.small
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(CheckboxSize)
                .clip(shape)
                .background(colors.canvas)
                .border(1.5.dp, colors.line, shape),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = type.fieldLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
        )
    }
}

@Composable
private fun RemoveButton(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(start = 4.dp)
            .size(40.dp)
            .clip(SampleTheme.shapes.pill)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = "Remove field",
            tint = SampleTheme.colors.inkMuted,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** The field as it follows the pointer: the same row on its own lifted card. */
@Composable
internal fun LiftedFormField(field: FormField) {
    ItemSurface(
        modifier = Modifier
            .fillMaxWidth()
            .lifted(),
    ) {
        FormFieldRow(field = field, onRemove = {}, modifier = Modifier.padding(end = 4.dp))
    }
}

/**
 * Marks where a new field will land: an accent line along the top of this row,
 * starting after the handle so it lines up with the field labels.
 */
@Composable
internal fun Modifier.insertLineAbove(visible: Boolean): Modifier {
    val accent = SampleTheme.colors.accent
    return drawBehind {
        if (!visible) return@drawBehind
        val height = InsertLineHeight.toPx()
        val start = HandleWidth.toPx()
        drawRoundRect(
            color = accent,
            topLeft = Offset(start, -height / 2f),
            size = Size(size.width - start, height),
            cornerRadius = CornerRadius(height / 2f),
        )
    }
}
