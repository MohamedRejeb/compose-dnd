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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.DragAndDropState
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drag.dragHandle
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import com.mohamedrejeb.compose.dnd.reorder.reorderableItem
import com.mohamedrejeb.compose.dnd.scroll.dragAutoScroll
import components.DemoScaffold
import components.DropWell
import components.ItemSurface
import components.dragPlaceholder
import components.wellState
import navigation.Demo
import theme.SampleTheme

private const val AppendSlotKey = "append-slot"
private val MaxFormWidth = 640.dp
private val EmptySlotHeight = 160.dp
private val AppendSlotHeight = 56.dp

private val FormBuilderCode = """
// Palette chips are copied, never moved
Modifier.draggableItem(
    key = "palette-${'$'}{type.name}",
    data = FormDragData.PaletteItem(type),
    state = dndState,
    draggableContent = { Chip(type) },
)
// The end of the form creates a field
Modifier.dropTarget(
    key = "append-slot",
    state = dndState,
    onDrop = { add(it.data) },
)
"""

@Composable
fun FormBuilderScreen(
    onBack: () -> Unit,
) {
    FormBuilderScreen(onBack = onBack, initialFields = emptyList())
}

/** Starts with [initialFields] already in the form, for previews and snapshots. */
@Composable
internal fun FormBuilderScreen(
    onBack: () -> Unit,
    initialFields: List<FieldType>,
) {
    DemoScaffold(
        title = Demo.FormBuilder.title,
        hint = "Drag a field into the form, then drag its handle to reorder.",
        code = FormBuilderCode,
        onBack = onBack,
    ) { insets ->
        FormBuilderContent(
            initialFields = initialFields,
            contentPadding = insets,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** The form's fields and the id the next new field gets. */
private class FormState(
    initialFields: List<FieldType>
) {
    var fields by mutableStateOf(initialFields.mapIndexed { index, type -> FormField(id = index + 1, type = type) })
        private set
    private var nextId by mutableIntStateOf(initialFields.size + 1)

    fun insert(type: FieldType, index: Int) {
        val field = FormField(id = nextId, type = type)
        nextId += 1
        val slot = index.coerceIn(0, fields.size)
        fields = fields.take(slot) + field + fields.drop(slot)
    }

    fun insertBefore(type: FieldType, target: FormField) {
        val index = fields.indexOfFirst { it.id == target.id }
        if (index != -1) insert(type, index)
    }

    /** Existing fields reorder as soon as they enter another row. */
    fun moveTo(field: FormField, target: FormField) {
        if (field.id == target.id) return
        val targetIndex = fields.indexOfFirst { it.id == target.id }
        if (targetIndex == -1) return
        val rest = fields.filter { it.id != field.id }
        val slot = targetIndex.coerceAtMost(rest.size)
        fields = rest.take(slot) + field + rest.drop(slot)
    }

    fun moveToEnd(field: FormField) {
        fields = fields.filter { it.id != field.id } + field
    }

    fun remove(field: FormField) {
        fields = fields.filter { it.id != field.id }
    }
}

@Composable
private fun FormBuilderContent(
    initialFields: List<FieldType>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val dndState = rememberDragAndDropState<FormDragData>()
    val form = remember { FormState(initialFields) }

    DragAndDropContainer(state = dndState, modifier = modifier) {
        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .widthIn(max = MaxFormWidth)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = contentPadding.calculateBottomPadding() + 16.dp),
            ) {
                FieldPalette(dndState = dndState)
                Spacer(Modifier.height(16.dp))
                FormCard(
                    form = form,
                    dndState = dndState,
                    // Wraps its fields, and scrolls once they outgrow the screen.
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                )
            }
        }
    }
}

@Composable
private fun FormCard(
    form: FormState,
    dndState: DragAndDropState<FormDragData>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    ItemSurface(shape = SampleTheme.shapes.well, modifier = modifier) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 8.dp, end = 12.dp, top = 20.dp, bottom = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .dragAutoScroll(state = dndState, lazyListState = listState),
        ) {
            item(key = "form-header") { FormHeader(fieldCount = form.fields.size) }
            fieldRows(form = form, dndState = dndState)
            item(key = AppendSlotKey) {
                AppendSlot(
                    dndState = dndState,
                    isFormEmpty = form.fields.isEmpty(),
                    onDrop = { data ->
                        when (data) {
                            is FormDragData.PaletteItem -> form.insert(data.type, form.fields.size)
                            is FormDragData.ExistingField -> form.moveToEnd(data.field)
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun FormHeader(fieldCount: Int) {
    Column(modifier = Modifier.padding(start = 8.dp, end = 4.dp, bottom = 12.dp)) {
        Text(
            text = "Workshop sign-up",
            style = MaterialTheme.typography.titleLarge,
            color = SampleTheme.colors.ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = if (fieldCount == 1) "1 field" else "$fieldCount fields",
            style = MaterialTheme.typography.bodySmall,
            color = SampleTheme.colors.inkMuted,
        )
    }
}

private fun LazyListScope.fieldRows(
    form: FormState,
    dndState: DragAndDropState<FormDragData>,
) {
    items(form.fields, key = { it.id }) { field ->
        val paletteHovering = dndState.draggedItem?.data is FormDragData.PaletteItem &&
            dndState.isDropTargetHovered(field.id)
        FormFieldRow(
            field = field,
            onRemove = { form.remove(field) },
            handleModifier = Modifier.dragHandle(key = field.id, state = dndState),
            modifier = Modifier
                .animateItem()
                .insertLineAbove(visible = paletteHovering)
                .dragPlaceholder(active = dndState.isDragging(field.id))
                .reorderableItem(
                    key = field.id,
                    data = FormDragData.ExistingField(field),
                    state = dndState,
                    hasDragHandle = true,
                    dropStrategy = DropStrategy.CenterDistance,
                    onDragEnter = { state ->
                        val entered = state.data
                        // Only existing fields reorder on hover; palette items insert on drop.
                        if (entered is FormDragData.ExistingField) form.moveTo(entered.field, field)
                    },
                    onDrop = { state ->
                        val dropped = state.data
                        if (dropped is FormDragData.PaletteItem) form.insertBefore(dropped.type, field)
                    },
                    draggableContent = { LiftedFormField(field) },
                ),
        )
    }
}

@Composable
private fun AppendSlot(
    dndState: DragAndDropState<FormDragData>,
    isFormEmpty: Boolean,
    onDrop: (FormDragData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = wellState(
        dragging = dndState.draggedItem != null,
        hovered = dndState.isDropTargetHovered(AppendSlotKey),
    )
    DropWell(
        state = state,
        shape = SampleTheme.shapes.item,
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(if (isFormEmpty) EmptySlotHeight else AppendSlotHeight)
            .dropTarget(
                key = AppendSlotKey,
                state = dndState,
                dropAnimationEnabled = false,
                onDrop = { dragged -> onDrop(dragged.data) },
            ),
    ) {
        if (isFormEmpty) EmptyFormPrompt() else AppendPrompt()
    }
}

@Composable
private fun EmptyFormPrompt() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            tint = SampleTheme.colors.inkMuted,
            modifier = Modifier.size(28.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Drag a field here",
            style = MaterialTheme.typography.titleSmall,
            color = SampleTheme.colors.ink,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Your form starts with the first drop.",
            style = MaterialTheme.typography.bodySmall,
            color = SampleTheme.colors.inkMuted,
        )
    }
}

@Composable
private fun AppendPrompt() {
    Text(
        text = "Drop here to add at the end",
        style = MaterialTheme.typography.bodySmall,
        color = SampleTheme.colors.inkMuted,
    )
}
