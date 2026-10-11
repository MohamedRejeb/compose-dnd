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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import components.ItemSurface
import theme.Hue
import theme.SampleTheme

private const val ArtAlpha = 0.22f
private val ProductIconSize = 72.dp

/** A product in the "Row" mode: a square picture with its name and price below. */
@Composable
internal fun ScrollProductCard(
    product: ScrollProduct,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    ItemSurface(modifier = modifier.width(ScrollProductWidth)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(SampleTheme.shapes.small)
                    .background(product.hue.container),
            ) {
                Icon(
                    imageVector = product.icon,
                    contentDescription = null,
                    tint = product.hue.onContainer,
                    modifier = Modifier.size(ProductIconSize),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.labelLarge,
                color = colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
            Text(
                text = product.price,
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 2.dp),
            )
        }
    }
}

/** An app in the "Grid" mode: an icon on a hue tile with its name. */
@Composable
internal fun ScrollAppTile(
    app: ScrollApp,
    modifier: Modifier = Modifier,
) {
    ItemSurface(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(ScrollAppTileHeight),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(SampleTheme.shapes.small)
                    .background(app.hue.container),
            ) {
                Icon(
                    imageVector = app.icon,
                    contentDescription = null,
                    tint = app.hue.onContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Text(
                text = app.name,
                style = MaterialTheme.typography.labelMedium,
                color = SampleTheme.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

/** A photo in the "Masonry" mode: a hue print with a simple landscape and its place. */
@Composable
internal fun ScrollPhotoTile(
    photo: ScrollPhoto,
    modifier: Modifier = Modifier,
) {
    ItemSurface(
        color = photo.hue.container,
        bordered = false,
        modifier = modifier
            .fillMaxWidth()
            .height(photo.height),
    ) {
        Canvas(Modifier.fillMaxSize()) { drawLandscape(photo.hue) }
        Text(
            text = photo.place,
            style = MaterialTheme.typography.labelMedium,
            color = photo.hue.onContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
        )
    }
}

/** A sun and two hills, drawn in the photo's own hue. */
private fun DrawScope.drawLandscape(hue: Hue) {
    val ink = hue.onContainer.copy(alpha = ArtAlpha)
    val w = size.width
    val h = size.height
    drawCircle(color = ink, radius = w * 0.1f, center = Offset(w * 0.72f, h * 0.3f))
    val far = Path().apply {
        moveTo(0f, h * 0.72f)
        quadraticTo(w * 0.3f, h * 0.48f, w * 0.62f, h * 0.7f)
        lineTo(w, h * 0.62f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    val near = Path().apply {
        moveTo(0f, h * 0.84f)
        quadraticTo(w * 0.55f, h * 0.66f, w, h * 0.86f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(far, ink)
    drawPath(near, ink)
}

/** A task in the "Column" mode: a compact row with a due date and a tag. */
@Composable
internal fun ScrollTaskRow(
    task: ScrollTask,
    modifier: Modifier = Modifier,
) {
    val colors = SampleTheme.colors
    ItemSurface(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .border(2.dp, colors.line, SampleTheme.shapes.pill),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = task.due,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkMuted,
                )
            }
            Text(
                text = task.tag,
                style = MaterialTheme.typography.labelMedium,
                color = task.hue.onContainer,
                modifier = Modifier
                    .clip(SampleTheme.shapes.pill)
                    .background(task.hue.container)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
