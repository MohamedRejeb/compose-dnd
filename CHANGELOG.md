# Changelog

All notable changes to Compose DND are documented in this file.

## Unreleased

### Fixed

- Reordering in a `LazyVerticalStaggeredGrid` or `LazyHorizontalStaggeredGrid` with `dragAutoScroll` or `dragScrollPin` no longer throws the scroll position back on every swap. Before, an item held at the bottom edge kept swapping with the same two items and the grid never scrolled further. The scroll position is now only pinned when the swap moves the first visible item.

## 1.0.0-alpha01

The first 1.0 alpha. It unifies the API around `DragAndDropState`, removes the last experimental opt-ins, and fixes several drag lifecycle races. See the [migration guide](https://mohamedrejeb.github.io/compose-dnd/migration-1.0/) if you're upgrading from `0.5.0`.

### Added

- `DragAndDropState.isDropTargetHovered(key)`, equivalent to `hoveredDropTargetKey == key`.
- `DragAndDropState.cancelDrag()` to abort the active drag programmatically: the ghost animates back, no drop target receives `onDrop`, and the item's `onDragEnd` fires.
- `onDragStart` and `onDragEnd` callbacks, both `(DraggedItemState<T>) -> Unit`, on `Modifier.draggableItem`, `Modifier.reorderableItem`, `DraggableItem`, and `ReorderableItem`. `onDragEnd` fires once per drag, after the drop target's `onDrop` and the drop animation (or after the return animation on cancel/release outside a target) -- the recommended place to persist a reordered list.
- `canDrop: Boolean = true` on `Modifier.reorderableItem` and `ReorderableItem`, so a pinned or locked item's slot can opt out of reorder swaps. Combine with `enabled = false` to also stop the item itself from being dragged.
- `dropAlignment`, `dropOffset`, and `dropAnimationEnabled` on the `ReorderableItem` composable, for parity with the `reorderableItem` modifier.
- A new `ReorderableItem(state: DragAndDropState<T>, ...)` overload.
- `dragAutoScroll` and `dragScrollPin` overloads for `LazyStaggeredGridState`.
- `DragAutoScrollConfig.DefaultMinScrollThreshold` (`48.dp`), `DefaultMaxScrollThreshold` (`160.dp`), and `DefaultMaxScrollSpeed` (`600.dp`).
- New sample screens: drag-to-select and multi-select group drag (`PhotoGalleryScreen`), free-form placement with grid snapping (`StickyNotesBoardScreen`), pinned-item reordering (`PinnedChatsScreen`), and a staggered-grid ("Masonry") auto-scroll demo.
- New docs pages: [Drag to Select and Multi-Select](https://mohamedrejeb.github.io/compose-dnd/drag-and-drop/multi-select/), [Free-Form Placement](https://mohamedrejeb.github.io/compose-dnd/drag-and-drop/free-form/), and a [migration guide](https://mohamedrejeb.github.io/compose-dnd/migration-1.0/).
- The public API is now tracked with Kotlin's binary compatibility validator (`api/` dumps, `./gradlew :compose-dnd:checkKotlinAbi`).

### Changed

- `dragAutoScroll` and `dragScrollPin` are no longer annotated `@ExperimentalDndApi` and no longer require `@OptIn`. Axis lock (`dragAxis`) and `canDrop` were already stable; their docs incorrectly asked for opt-in and have been corrected. Nothing in the public API currently requires `@OptIn(ExperimentalDndApi::class)`.
- `DragAutoScrollConfig.maxScrollSpeed` is now a `Dp` distance scrolled per second instead of a raw pixel-per-second `Float`, so auto-scroll feels the same on every screen density. The default is `600.dp`.
- `DragAutoScrollConfig` is a regular class instead of a `data class`. It still exposes `copy()`, but no longer supports `componentN()` destructuring.
- Auto-scroll edge zones are capped to a quarter of the viewport, and an item too large to fit between them is followed around the pointer instead of by its edges. Before, a wide item on a small screen (a Kanban card on a phone) sat in both zones at once: it scrolled at full speed after a short move, or not at all when it started next to the list's first edge.
- An item picked up next to an edge no longer scrolls the list right away: auto-scroll waits for the pointer to move `8.dp` toward that edge, or for the item to leave the edge zone and come back.
- Auto-scroll now stays idle once the container can't scroll further in a direction, instead of spinning frames against the end of the list.
- A draggable item's configuration (`dropTargets`, `dropStrategy`, `dragAxis`, `dropAnimationSpec`, `draggableContent`) is now captured when a drag starts and applies for the whole drag.
- `reorderHysteresisDistance` is now the play around the point where two items swap: after a swap, the drag has to come back by that distance, plus the spacing between the items, to undo it. `0.dp` no longer disables anything, it leaves the spacing as the only play. In a reorder, the `dropStrategy` picks among the slots the dragged item has reached instead of among every target under it.
- `DragAndDropContainer` wraps its content in a `LookaheadScope`, which is how it knows where animated items settle.
- **Breaking:** `DropStrategy.SurfacePercentage` now measures the overlap as a share of each **drop target's** area, as its docs described: a small target that is fully covered wins over a large one that is partly covered. Before, it divided by the dragged item's area, which made it pick exactly like `DropStrategy.Surface`.
- The default `dropStrategy` is now `DropStrategy.Surface`. The default hover behavior is unchanged, since the old default picked the same target.
- **Breaking:** the `DraggableItem` and `ReorderableItem` composables now take `key, data, state, modifier` first, in that order, like the `draggableItem` and `reorderableItem` modifiers. `ReorderableItem` also takes `zIndex` after `dragAxis`. Calls with named arguments are not affected; calls passing these positionally need to be reordered.
- The drag is anchored at the point where the pointer went down instead of where the touch slop was crossed, so the grabbed point stays under the pointer and `DraggedItemState.dragAmount` matches the pointer movement exactly.

### Deprecated

- `ReorderState`, `rememberReorderState`, `ReorderContainer`, and the `ReorderableItem(state: ReorderState, ...)` overload. Use `rememberDragAndDropState`, `DragAndDropContainer`, and `ReorderableItem(state: DragAndDropState, ...)` instead -- one state type for both plain drag and drop and reordering.
- The top-level `com.mohamedrejeb.compose.dnd.drag.isDragging(key)` extension function. `DragAndDropState.isDragging(key)` is now a member function and needs no import.
- The `DragAutoScrollConfig(minScrollThreshold, maxScrollThreshold, maxScrollSpeed: Float)` factory. Use the `Dp`-based constructor instead; the deprecated factory is kept only for migration.

### Fixed

- The drop animation after a fast reorder no longer lands the ghost on the neighboring slot before jumping to the final target; scrolling during the settle animation no longer stalls it (#73).
- Starting a new drag while the previous drop animation is still running no longer clears that drop's state -- the previous drop now completes instantly, including its `onDrop` callback, before the new drag starts.
- A second pointer can no longer start a concurrent drag.
- A drag started by a long press inside a scrollable is no longer taken over by the scrollable when the finger drifted during the press. Once a drag is on, the container follows the pointer before its children see the event.
- Releasing a long-press drag without moving no longer also clicks the item.
- The drop animation of an item locked to an axis (`dragAxis`) now keeps to that axis. Before, the item slid sideways to the middle of a target wider than itself, then jumped back to its place.
- A scrollable under a drag no longer scrolls itself when the finger reports an event without movement. During auto-scroll this made the list jump back and stop scrolling.
- Reordering items of different sizes no longer bounces. A dragged item now takes the slot of another one when its leading edge passes the middle of that item, a rule that stays true after the swap whatever the two sizes. Before, the target was picked by the drop strategy alone and could change with the very swap it caused: a card carried over another list could swap back and forth several times and end one slot away from where it was held.
- An item pulled more than half out of its list sideways, on its way to another one, no longer swaps with the items it leaves behind.
- Hovering is resolved against where items settle, not where they are while `animateItem()` moves them, so the reorder animation no longer changes the hovered item.
- A card inserted into another list above a card it is mostly below now moves on past that card right away, instead of waiting for the drag to move again.
- Disabling a `DragAndDropContainer` (`enabled = false`) mid-drag now cancels the drag instead of leaving it stuck.
- A gesture canceled by the system (an incoming call, a parent taking over the touch, the container leaving the composition) now cancels the drag. Before, it was handled as a release and dropped the item on the hovered target.
- A `DragAndDropContainer` that leaves the composition mid-drag now ends the drag, so a state hoisted above it is ready for the next one.
- A drag that ends over a target without dropping on it (canceled, or the target turned `canDrop = false`) now calls that target's `onDragExit`, so a highlight set in `onDragEnter` no longer stays on.
- A target whose `canDrop` turned `false` while it was hovered no longer receives the drop.
- `cancelDrag()` called right after the item was released no longer runs the drop and the cancel together, which called `onDragEnd` twice.
- `enabled = false` on an item now also stops its drag handle.
- An item whose scoped `dragHandle()` is removed from its content can be dragged as a whole again.
- Items and drop targets now follow their position when an ancestor moves them, such as a list scrolling under the drag. Before, hover detection could use positions from before the scroll, so on Android a Kanban column dragged to the edge scrolled the board without ever swapping with the columns passing under it.
- `dragAutoScroll` on lazy layouts keeps a steady pace while the pointer keeps moving inside an edge zone. Before, every pointer move restarted its frame loop.
- `dragAutoScroll` now scrolls the right way in lists and grids with `reverseLayout = true` and in horizontal layouts in right-to-left locales. Before, it did not scroll at all toward the far end of such a list.
- Changing an item's key (for example on lazy layout node reuse) no longer leaves a stale registration behind.
- Drag handles no longer poll for their item every millisecond while waiting for it to register.
- The drag start position now uses live coordinates, so it is never stale after a reorder.
- The container no longer misses the start of a drag when several pointer events are dispatched before the next recomposition (seen on Android as drags that never hovered any target).
- `dragAutoScroll` for `ScrollState` no longer polls every frame during a drag when there is nothing to scroll.

### Documentation

- `onDrop` timing is now documented: it is called once the drop animation has landed the drag shadow on the target, right before the dragged item's `onDragEnd`, or on release with `dropAnimationEnabled = false`.
- The conditional drop docs showed `canDrop` as a lambda; it is a `Boolean`.
