# Form Builder (Copy on Drop)

Most drag and drop moves an item from one place to another. A form builder needs something different: dragging a field type from a **palette** should *create* a new field in the form while the palette chip stays put. This guide walks through the pattern used by the Form Builder sample.

<video src="../../videos/form-builder.mp4" autoplay loop muted playsinline width="720"></video>

The building blocks:

- A **sealed drag payload** so palette items and existing form fields share one `DragAndDropState`.
- Palette chips as `draggableItem`s whose source is **never hidden or removed** — a copy is created on drop.
- Form rows as `reorderableItem`s that insert palette drops at their position and reorder existing fields on hover.
- An append slot at the end of the form, doubling as the empty-form target.

## One State, Two Payloads

A `DragAndDropState<T>` carries one data type, but palette chips and form fields are different things. Model both as a sealed hierarchy:

```kotlin
sealed interface FormDragData {
    data class PaletteItem(val type: FieldType) : FormDragData

    data class ExistingField(val field: FormField) : FormDragData
}

val dndState = rememberDragAndDropState<FormDragData>()
```

Every drop target can now branch on what was dropped.

## The Palette: Sources That Stay Put

Each palette chip is a plain `draggableItem`. Two things distinguish it from a move:

- The key is stable (`"palette-Email"`) and the chip is never removed from the palette.
- There is **no** `graphicsLayer { alpha = ... }` trick — the source stays visible while its drag shadow flies.

```kotlin
PaletteChipContent(
    type = type,
    modifier = Modifier.draggableItem(
        key = "palette-${type.name}",
        data = FormDragData.PaletteItem(type),
        state = dndState,
        draggableContent = {
            PaletteChipContent(type = type, isDragShadow = true)
        },
    ),
)
```

## Insert on Drop, Reorder on Enter

The two payloads want different timing:

- **Existing fields** reorder in `onDragEnter`, like any sortable list — the live displacement is the feedback.
- **Palette items** must wait for `onDrop`. Creating the field in `onDragEnter` would leave phantom copies behind every row the drag passes over.

Each form row's `reorderableItem` handles both:

```kotlin
Modifier.reorderableItem(
    key = field.id,
    data = FormDragData.ExistingField(field),
    state = dndState,
    hasDragHandle = true,
    dropStrategy = DropStrategy.CenterDistance,
    onDragEnter = { state ->
        val entered = state.data
        if (entered is FormDragData.ExistingField && entered.field.id != field.id) {
            fields = fields.moveBefore(entered.field, field)
        }
    },
    onDrop = { state ->
        val dropped = state.data
        if (dropped is FormDragData.PaletteItem) {
            val index = fields.indexOfFirst { it.id == field.id }
            if (index != -1) fields = fields.insertAt(index, newField(dropped.type))
        }
    },
    draggableContent = { FormFieldRow(field, isDragShadow = true) },
)
```

Rows drag by a grip handle (`hasDragHandle = true` plus `Modifier.dragHandle(key, state)` on the grip icon), so the delete button and future inputs stay clickable.

## Showing Where the Field Will Land

Palette drops insert *above* the hovered row, so mark that edge while a palette item is in the air. `hoveredDropTargetKey` plus a type check on `draggedItem` is all it takes:

```kotlin
val isPaletteItemHovering = dndState.draggedItem?.data is FormDragData.PaletteItem &&
    dndState.hoveredDropTargetKey == field.id
```

The sample tints the hovered row and draws an accent bar across its top edge:

```kotlin
Modifier.drawBehind {
    if (isPaletteItemHovering) {
        drawRoundRect(
            color = accent,
            size = Size(width = size.width, height = 3.dp.toPx()),
            cornerRadius = CornerRadius(1.5.dp.toPx()),
        )
    }
}
```

## The Append Slot

A dashed `dropTarget` after the last row accepts both payloads — palette items append a new field, existing fields move to the end. When the form is empty it grows into the main call-to-action:

```kotlin
Box(
    modifier = Modifier
        .dropTarget(
            key = "append-slot",
            state = dndState,
            dropAnimationEnabled = false,
            onDrop = { state ->
                when (val data = state.data) {
                    is FormDragData.PaletteItem -> fields = fields + newField(data.type)
                    is FormDragData.ExistingField ->
                        fields = fields.filter { it.id != data.field.id } + data.field
                }
            },
        ),
) {
    Text(if (fields.isEmpty()) "Drag a field here to start your form" else "Drop here to add at the end")
}
```

The complete implementation is in the sample app: [FormBuilderScreen.kt](https://github.com/MohamedRejeb/compose-dnd/blob/main/sample/common/src/commonMain/kotlin/ui/FormBuilderScreen.kt).
