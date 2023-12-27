package components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun RedBox(
    isDragShadow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .then(
                if (isDragShadow)
                    Modifier
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(24.dp),
                        )
                else
                    Modifier
            )
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.tertiary)
    ) {
        Icon(
            Icons.Rounded.DragIndicator,
            contentDescription = "Drag indicator",
            tint = MaterialTheme.colorScheme.onTertiary,
        )
    }
}
