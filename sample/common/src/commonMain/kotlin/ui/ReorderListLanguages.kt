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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.ItemSurface
import theme.Hue
import theme.Hues
import theme.SampleTheme

private val LanguageRowHeight = 64.dp
private val BadgeSize = 44.dp

@Immutable
internal data class Language(
    val name: String,
    val usedFor: String,
    val year: String,
    val hue: Hue,
)

private val LanguageData = listOf(
    Triple("Kotlin", "Multiplatform apps", "2011"),
    Triple("Swift", "Apple platforms", "2014"),
    Triple("Rust", "Safe systems code", "2015"),
    Triple("TypeScript", "Typed JavaScript", "2012"),
    Triple("Python", "Data and scripting", "1991"),
    Triple("Go", "Cloud services", "2009"),
    Triple("Dart", "Cross-platform UI", "2011"),
    Triple("Java", "Enterprise backends", "1995"),
    Triple("C#", "Desktop apps and games", "2000"),
    Triple("C", "Operating systems", "1972"),
    Triple("C++", "Engines and browsers", "1985"),
    Triple("JavaScript", "The web", "1995"),
    Triple("Ruby", "Web apps", "1995"),
    Triple("Elixir", "Concurrent services", "2012"),
    Triple("Haskell", "Pure functional", "1990"),
    Triple("Scala", "Functional on the JVM", "2004"),
    Triple("Zig", "Simple low-level code", "2016"),
    Triple("Lua", "Embedded scripting", "1993"),
    Triple("Clojure", "A Lisp on the JVM", "2007"),
    Triple("OCaml", "Typed functional", "1996"),
    Triple("Julia", "Scientific computing", "2012"),
    Triple("R", "Statistics", "1993"),
    Triple("PHP", "Server-side web", "1995"),
    Triple("Erlang", "Fault-tolerant systems", "1986"),
)

internal val InitialLanguages: List<Language> = LanguageData.mapIndexed { index, (name, usedFor, year) ->
    Language(name = name, usedFor = usedFor, year = year, hue = Hues.at(index))
}

/** Returns a new list where [moved] takes the slot of [target]. */
internal fun List<Language>.move(moved: Language, target: Language): List<Language> {
    val from = indexOf(moved)
    val to = indexOf(target)
    if (from == -1 || to == -1 || from == to) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

/** One row: a badge, the language and what it is used for, and the year it appeared. */
@Composable
internal fun LanguageRow(
    language: Language,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    ItemSurface(modifier = modifier.fillMaxWidth().height(LanguageRowHeight)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 16.dp),
        ) {
            Badge(language.hue)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = language.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = language.usedFor,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = language.year,
                style = MaterialTheme.typography.labelMedium,
                color = colors.inkMuted,
            )
        }
    }
}

@Composable
private fun Badge(hue: Hue) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(BadgeSize)
            .clip(SampleTheme.shapes.small)
            .background(hue.container),
    ) {
        Icon(
            imageVector = Icons.Rounded.Code,
            contentDescription = null,
            tint = hue.onContainer,
            modifier = Modifier.size(22.dp),
        )
    }
}
