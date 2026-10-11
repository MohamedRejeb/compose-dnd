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
package navigation

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

/**
 * Opens one demo. Carries the [Demo] name rather than the enum itself, because
 * enum route arguments are only supported on Android in navigation 2.9.
 */
@Serializable
data class DemoRoute(
    val name: String,
) {
    constructor(demo: Demo) : this(demo.name)

    val demo: Demo get() = Demo.valueOf(name)
}

/** Every screen reachable from home, in the order home lists them. */
enum class Demo(
    val title: String,
    val summary: String,
    val kind: Kind,
) {
    // -- Examples: small apps built with the library --
    Kanban("Kanban board", "Move cards across columns and reorder the columns.", Kind.Example),
    Photos("Photo gallery", "Drag to select photos, then drop them on an album.", Kind.Example),
    StickyNotes("Sticky notes", "Pin notes anywhere and snap them to a grid.", Kind.Example),
    FormBuilder("Form builder", "Drag fields from a palette into a form.", Kind.Example),
    PinnedChats("Pinned chats", "Reorder chats while pinned ones stay put.", Kind.Example),

    // -- Features: one API each --
    DragAndDrop("Drag and drop", "draggableItem + dropTarget", Kind.Feature),
    ReorderList("Reorder a list", "reorderableItem", Kind.Feature),
    DragHandle("Drag handle", "dragHandle", Kind.Feature),
    BetweenLists("Move between lists", "one state, many lists", Kind.Feature),
    AutoScroll("Auto scroll", "dragAutoScroll", Kind.Feature),
    AxisLock("Axis lock", "dragAxis", Kind.Feature),
    ConditionalDrop("Conditional drop", "canDrop", Kind.Feature),
    DropStrategies("Drop strategies", "DropStrategy", Kind.Feature),
    ;

    enum class Kind { Example, Feature }
}
