# Compose DND

A library that allows you to easily add drag and drop functionality to your Jetpack Compose or Compose Multiplatform projects.

[![Kotlin](https://img.shields.io/badge/kotlin-2.3.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.10.3-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
[![Apache-2.0](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)
[![BuildPassing](https://shields.io/badge/build-passing-brightgreen)](https://github.com/MohamedRejeb/compose-dnd/actions)
[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.dnd/compose-dnd)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.dnd%22%20AND%20a:%22compose-dnd%22)

![Compose DND thumbnail](docs/images/thumbnail.png)

## Features

- **Drag and Drop** -- Drag items from one location and drop them onto designated targets
- **Reorder Lists** -- Reorder items within a list using drag and drop gestures
- **Auto Scroll** -- Automatically scroll containers when dragging items near edges
- **Drop Strategies** -- Choose from multiple built-in strategies to determine which drop target receives the dragged item
- **Drag Handle** -- Restrict the drag gesture to a specific handle area within the item
- **Axis Lock** -- Constrain dragging to the horizontal or vertical axis
- **Conditional Drop** -- Control which drop targets accept which dragged items
- **Drop Animation** -- Smooth spring-based animations when items are dropped
- **Custom Drag Shadow** -- Provide a custom composable to display while an item is being dragged

## Platform Support

| Platform   | Supported |
|------------|-----------|
| Android    | Yes       |
| iOS        | Yes       |
| Desktop    | Yes       |
| Web (JS)   | Yes       |
| Web (WASM) | Yes       |

## Installation

[![Maven Central](https://img.shields.io/maven-central/v/com.mohamedrejeb.dnd/compose-dnd)](https://search.maven.org/search?q=g:%22com.mohamedrejeb.dnd%22%20AND%20a:%22compose-dnd%22)

### Version Compatibility

| Kotlin version | Compose version | Compose DND version |
|----------------|-----------------|---------------------|
| 2.3.20         | 1.10.3          | 0.5.0               |

Add the following dependency to your module `build.gradle.kts` file:

```kotlin
implementation("com.mohamedrejeb.dnd:compose-dnd:0.5.0")
```

For Kotlin Multiplatform projects, add the dependency to your `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.mohamedrejeb.dnd:compose-dnd:0.5.0")
        }
    }
}
```

## Usage

### Drag and Drop

Create a `DragAndDropState`, wrap your content with `DragAndDropContainer`, and use the `draggableItem` and `dropTarget` modifiers:

```kotlin
val dragAndDropState = rememberDragAndDropState<String>()

DragAndDropContainer(
    state = dragAndDropState,
) {
    val isDragging = dragAndDropState.isDragging("item-1")

    Text(
        text = "Drag me",
        modifier = Modifier
            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
            .draggableItem(
                key = "item-1",
                data = "Hello",
                state = dragAndDropState,
                draggableContent = {
                    Text("Drag me") // Shown as the drag shadow
                },
            ),
    )

    Box(
        modifier = Modifier
            .dropTarget(
                key = "target-1",
                state = dragAndDropState,
                onDrop = { state ->
                    println("Dropped: ${state.data}")
                },
            )
    ) {
        Text("Drop here")
    }
}
```

### Reorder List

The `reorderableItem` modifier makes an item both draggable and a drop target, which is all a sortable list needs:

```kotlin
val dndState = rememberDragAndDropState<String>()

DragAndDropContainer(
    state = dndState,
) {
    LazyColumn {
        items(items, key = { it }) { item ->
            Text(
                text = item,
                modifier = Modifier
                    .graphicsLayer { alpha = if (dndState.isDragging(item)) 0f else 1f }
                    .reorderableItem(
                        key = item,
                        data = item,
                        state = dndState,
                        onDragEnter = { state ->
                            items = items.toMutableList().apply {
                                val index = indexOf(item)
                                if (index != -1) {
                                    remove(state.data)
                                    add(index, state.data)
                                }
                            }
                        },
                        draggableContent = {
                            Text(item)
                        },
                    ),
            )
        }
    }
}
```

### Enable/Disable Drag and Drop

Toggle drag and drop at the container level:

```kotlin
DragAndDropContainer(
    state = dragAndDropState,
    enabled = false,
) { }
```

Or for a specific item by passing `enabled = false` to the `draggableItem` or `reorderableItem` modifier.

> Prefer wrapping composables instead of modifiers? The `DraggableItem` and `ReorderableItem` wrapper composables offer the same features - see the [documentation](https://mohamedrejeb.github.io/compose-dnd/).

> For more details and advanced features (auto scroll, drop strategies, drag handles, axis lock), check out the [documentation](https://mohamedrejeb.github.io/compose-dnd/) and the [sample project](https://github.com/MohamedRejeb/compose-dnd/tree/main/sample/common/src/commonMain/kotlin).

## Contribution

If you've found an error in this library, please file an [issue](https://github.com/MohamedRejeb/compose-dnd/issues).

Feel free to help out by sending a pull request.

[Code of Conduct](https://github.com/MohamedRejeb/compose-dnd/blob/main/CODE_OF_CONDUCT.md)

## Find this library useful?

Support it by joining [stargazers](https://github.com/MohamedRejeb/compose-dnd/stargazers) for this repository.

Also, [follow me](https://github.com/MohamedRejeb) on GitHub for more libraries!

You can always <a href="https://www.buymeacoffee.com/MohamedRejeb"><img src="https://img.buymeacoffee.com/button-api/?text=Buy me a coffee&emoji=&slug=MohamedRejeb&button_colour=FFDD00&font_colour=000000&font_family=Cookie&outline_colour=000000&coffee_colour=ffffff"></a>

## License

```
Copyright 2023 Mohamed Rejeb

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
