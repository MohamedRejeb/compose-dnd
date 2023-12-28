package utils

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun handleLazyListScroll(
    lazyListState: LazyListState,
    dropIndex: Int,
): Unit = coroutineScope {
    val firstVisibleItemIndex = lazyListState.firstVisibleItemIndex
    val firstVisibleItemScrollOffset = lazyListState.firstVisibleItemScrollOffset

    // Workaround to fix scroll issue when dragging the first item
    if (dropIndex == 0 || dropIndex == 1)
        launch {
            lazyListState.scrollToItem(firstVisibleItemIndex, firstVisibleItemScrollOffset)
        }

    // Animate scroll when entering the first or last item
    val lastVisibleItemIndex =
        lazyListState.firstVisibleItemIndex + lazyListState.layoutInfo.visibleItemsInfo.lastIndex

    val firstVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull() ?: return@coroutineScope
    val scrollAmount = firstVisibleItem.size * 2f

    if (dropIndex <= firstVisibleItemIndex + 1)
        launch {
            lazyListState.animateScrollBy(-scrollAmount)
        }
    else if (dropIndex == lastVisibleItemIndex)
        launch {
            lazyListState.animateScrollBy(scrollAmount)
        }
}
