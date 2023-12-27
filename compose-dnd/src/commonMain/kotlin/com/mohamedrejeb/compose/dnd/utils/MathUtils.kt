package com.mohamedrejeb.compose.dnd.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.plus
import kotlin.math.max
import kotlin.math.min

internal object MathUtils {
    /**
     * Check if two rectangles are intersected
     *
     * @param topLeft1 Top left point of first rectangle
     * @param size1 Size of first rectangle
     * @param topLeft2 Top left point of second rectangle
     * @param size2 Size of second rectangle
     * @return True if rectangles are intersected, false otherwise
     */
    fun isRectangleIntersected(
        topLeft1: Offset,
        size1: Size,
        topLeft2: Offset,
        size2: Size,
    ): Boolean {
        if (topLeft1.x > topLeft2.x + size2.width)
            return false

        if (topLeft1.x + size1.width < topLeft2.x)
            return false

        if (topLeft1.y > topLeft2.y + size2.height)
            return false

        if (topLeft1.y + size1.height < topLeft2.y)
            return false

        return true
    }

    /**
     * Calculate overlapping area of two rectangles
     *
     * @param topLeft1 Top left point of first rectangle
     * @param size1 Size of first rectangle
     * @param topLeft2 Top left point of second rectangle
     * @param size2 Size of second rectangle
     * @return Overlapping area
     */
    fun overlappingArea(
        topLeft1: Offset,
        size1: Size,
        topLeft2: Offset,
        size2: Size,
    ): Float {
        val bottomRight1 = topLeft1 + Offset(size1.width, size1.height)
        val bottomRight2 = topLeft2 + Offset(size2.width, size2.height)

        val topLeft = Offset(
            x = max(topLeft1.x, topLeft2.x),
            y = max(topLeft1.y, topLeft2.y),
        )

        val bottomRight = Offset(
            x = min(bottomRight1.x, bottomRight2.x),
            y = min(bottomRight1.y, bottomRight2.y),
        )

        val width = bottomRight.x - topLeft.x
        val height = bottomRight.y - topLeft.y

        return if (width > 0f && height > 0f)
            width * height
        else
            0f
    }

    /**
     * Check if point is in rectangle
     *
     * @param point Point to check
     * @param topLeft Top left point of rectangle
     * @param size Size of rectangle
     * @return True if point is in rectangle, false otherwise
     */
    fun isPointInRectangle(
        point: Offset,
        topLeft: Offset,
        size: Size,
    ): Boolean {
        return point.x >= topLeft.x
                && point.x <= topLeft.x + size.width
                && point.y >= topLeft.y
                && point.y <= topLeft.y + size.height
    }
}