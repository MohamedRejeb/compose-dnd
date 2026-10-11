# Multiple Lists (Kanban)

A Kanban board combines several features into a single screen: multiple columns, cross-column card transfer, positional insertion, auto-scroll on both axes, and reordering the columns themselves. This guide walks through the pattern used by the Kanban Board sample.

<video class="demo-wide" src="../../videos/kanban.mp4" autoplay loop muted playsinline></video>

The building blocks:

- One shared `DragAndDropState` for cards, so they can move between any columns.
- A second `DragAndDropState` for the columns, dragged by their header. Two states let card drags and column drags coexist on one screen.
- An outer `LazyRow` of columns and an inner `LazyColumn` per column, each with its own `dragAutoScroll`.
- `Modifier.reorderableItem` on each card, handling both same-column reorder and cross-column insertion in a single callback.
- A `dropTarget` placeholder so cards can be dropped into empty columns.

On a narrow screen the board shows one column at a time. Holding a card or a column at the edge scrolls the board to the next ones:

<video class="demo-phone" src="../../videos/kanban-phone.mp4" autoplay loop muted playsinline></video>

## Data Model and State

Keep columns and cards in immutable state. A single state instance is shared across the whole board:

```kotlin
data class Card(val id: String, val title: String)

data class Column(val id: String, val title: String, val cards: List<Card>)

val dndState = rememberDragAndDropState<Card>()
var columns by remember { mutableStateOf(initialColumns()) }
```

## Board Layout

Wrap the board in one `DragAndDropContainer`. The outer `LazyRow` scrolls horizontally between columns while a card is dragged toward the screen edge; each column's `LazyColumn` scrolls vertically:

```kotlin
@Composable
fun KanbanBoard() {
    val dndState = rememberDragAndDropState<Card>()
    var columns by remember { mutableStateOf(initialColumns()) }
    val rowState = rememberLazyListState()

    DragAndDropContainer(state = dndState) {
        LazyRow(
            state = rowState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .dragAutoScroll(
                    state = dndState,
                    lazyListState = rowState,
                ),
        ) {
            items(columns, key = { it.id }) { column ->
                ColumnUi(
                    column = column,
                    dndState = dndState,
                    onReorder = { dragged, target ->
                        columns = columns.moveCard(dragged, target)
                    },
                    onDropInEmpty = { dragged ->
                        columns = columns.moveCardToColumn(dragged, column.id)
                    },
                )
            }
        }
    }
}
```

## Cards with reorderableItem

Each card uses the `reorderableItem` modifier. Because every card in every column shares the same state, dragging a card over a card in *another* column fires `onDragEnter` there — cross-column transfer works with the same callback as same-column reorder:

```kotlin
@Composable
fun ColumnUi(
    column: Column,
    dndState: DragAndDropState<Card>,
    onReorder: (dragged: Card, target: Card) -> Unit,
    onDropInEmpty: (dragged: Card) -> Unit,
) {
    val colState = rememberLazyListState()

    LazyColumn(
        state = colState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .width(280.dp)
            .dragAutoScroll(
                state = dndState,
                lazyListState = colState,
            ),
    ) {
        items(column.cards, key = { it.id }) { card ->
            val isDragging = dndState.isDragging(card.id)

            CardUi(
                card = card,
                modifier = Modifier
                    .animateItem()
                    .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                    .reorderableItem(
                        key = card.id,
                        data = card,
                        state = dndState,
                        dropStrategy = DropStrategy.CenterDistance,
                        dragAfterLongPress = true,
                        onDragEnter = { state -> onReorder(state.data, card) },
                        draggableContent = {
                            CardUi(card = card, isDragShadow = true)
                        },
                    )
                    .fillMaxWidth(),
            )
        }
    }
}
```

Two choices matter here:

- `dropStrategy = DropStrategy.CenterDistance` picks the closest card when the dragged card reaches several at once, e.g. on its way into another column. See [Drop Strategies](drop-strategies.md).
- `dragAfterLongPress = true` keeps normal touch scrolling working — a plain swipe scrolls the column, a long press picks up the card.

## Cross-Column Move Logic

The reorder callback removes the dragged card from whichever column holds it and inserts it before the target card. One immutable transformation handles both same-column and cross-column moves:

```kotlin
fun List<Column>.moveCard(dragged: Card, target: Card): List<Column> {
    if (dragged.id == target.id) return this

    return map { col ->
        val targetIndex = col.cards.indexOfFirst { it.id == target.id }
        if (targetIndex == -1) {
            // Not the target column: just remove the dragged card if present
            col.copy(cards = col.cards.filter { it.id != dragged.id })
        } else {
            // Target column: remove then insert at the target position
            col.copy(
                cards = col.cards
                    .filter { it.id != dragged.id }
                    .toMutableList()
                    .apply { add(targetIndex.coerceAtMost(size), dragged) },
            )
        }
    }
}
```

Two lists are the smallest version of the same idea. Here a person either joins the end of the other team, or lands on the row they are dropped on:

<video class="demo-phone" src="../../videos/between-lists.mp4" autoplay loop muted playsinline></video>

## Dropping into Empty Columns

An empty column has no cards to hover over, so `onDragEnter` never fires. Add a placeholder `dropTarget` that appears when the column is empty:

```kotlin
if (column.cards.isEmpty()) {
    item(key = "empty-${column.id}") {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .dropTarget(
                    key = "empty-${column.id}",
                    state = dndState,
                    dropAnimationEnabled = false,
                    onDrop = { state -> onDropInEmpty(state.data) },
                ),
        ) {
            Text("Drop here")
        }
    }
}
```

`dropAnimationEnabled = false` commits the move immediately on drop — the card is re-parented into the column, so animating toward the placeholder's old position would fight the layout change.

## Reordering the Columns Themselves

Columns are draggable too, by their header. The trick is a **second** `DragAndDropState` typed to columns, with its own container wrapped around the card container — each drag layer tracks its own items and renders its own shadow:

```kotlin
val cardDndState = rememberDragAndDropState<Card>()
val columnDndState = rememberDragAndDropState<Column>()

DragAndDropContainer(state = columnDndState) {
    DragAndDropContainer(state = cardDndState) {
        LazyRow(
            modifier = Modifier
                .dragAutoScroll(state = cardDndState, lazyListState = rowState)
                .dragAutoScroll(state = columnDndState, lazyListState = rowState),
        ) {
            // columns...
        }
    }
}
```

Each column becomes a `reorderableItem` in the column state, with `hasDragHandle = true` so that only the header initiates the drag — otherwise every card gesture would also grab the column:

```kotlin
Column(
    modifier = Modifier
        .graphicsLayer { alpha = if (isColumnDragging) 0f else 1f }
        .reorderableItem(
            key = "col-${column.id}",
            data = column,
            state = columnDndState,
            hasDragHandle = true,
            dragAxis = DragAxis.Horizontal,
            dropStrategy = DropStrategy.CenterDistance,
            onDragEnter = { state -> onColumnEnter(state.data, column) },
            draggableContent = { ColumnDragPreview(column) },
        ),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dragHandle(
                key = "col-${column.id}",
                state = columnDndState,
            ),
    ) {
        // header content: colored dot, title, count, grip dots
    }

    // cards LazyColumn...
}
```

Useful details:

- `dragAxis = DragAxis.Horizontal` keeps the lifted column aligned with the row.
- The column reorder callback moves the column by id, mirroring the card logic.
- `draggableContent` renders a simplified preview (header plus the first few cards) instead of the full live column, keeping the drag shadow cheap.

!!! tip
    Cards of different heights swap without bouncing: a card takes the slot of another one when its leading edge passes the middle of that card, whatever their sizes — see [Reorder List](reorder.md#how-a-swap-is-decided).

The complete implementation, including priorities, tags, and assignees on the cards, is in the sample app: [KanbanBoardScreen.kt](https://github.com/MohamedRejeb/compose-dnd/blob/main/sample/common/src/commonMain/kotlin/ui/KanbanBoardScreen.kt).
