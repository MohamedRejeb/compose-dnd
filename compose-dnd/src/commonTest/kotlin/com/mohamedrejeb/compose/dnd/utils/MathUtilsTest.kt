package com.mohamedrejeb.compose.dnd.utils

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MathUtilsTest {

    @Test
    fun isRectanglesIntersectedSuccessTest() {
        val topLeft1 = Offset(0f, 0f)
        val size1 = Size(100f, 100f)
        val topLeft2 = Offset(50f, 50f)
        val size2 = Size(100f, 100f)

        val result = MathUtils.isRectangleIntersected(
            topLeft1 = topLeft1,
            size1 = size1,
            topLeft2 = topLeft2,
            size2 = size2,
        )

        assertTrue(result)
    }

    @Test
    fun isRectanglesIntersectedFailTest() {
        val topLeft1 = Offset(0f, 0f)
        val size1 = Size(100f, 100f)
        val topLeft2 = Offset(150f, 150f)
        val size2 = Size(100f, 100f)

        val result = MathUtils.isRectangleIntersected(
            topLeft1 = topLeft1,
            size1 = size1,
            topLeft2 = topLeft2,
            size2 = size2,
        )

        assertTrue(!result)
    }

    @Test
    fun overlappingAreaTest1() {
        val topLeft1 = Offset(0f, 0f)
        val size1 = Size(100f, 100f)
        val topLeft2 = Offset(50f, 50f)
        val size2 = Size(100f, 100f)

        val result = MathUtils.overlappingArea(
            topLeft1 = topLeft1,
            size1 = size1,
            topLeft2 = topLeft2,
            size2 = size2,
        )

        assertEquals(
            expected = 2500f,
            actual = result,
        )
    }

    @Test
    fun overlappingAreaTest2() {
        val topLeft1 = Offset(0f, 0f)
        val size1 = Size(10f, 10f)
        val topLeft2 = Offset(20f, 20f)
        val size2 = Size(10f, 10f)

        val result = MathUtils.overlappingArea(
            topLeft1 = topLeft1,
            size1 = size1,
            topLeft2 = topLeft2,
            size2 = size2,
        )

        assertEquals(
            expected = 0f,
            actual = result,
        )
    }

    @Test
    fun isPointInRectangleTest1() {
        val point = Offset(0f, 0f)
        val topLeft = Offset(0f, 0f)
        val size = Size(100f, 100f)

        val result = MathUtils.isPointInRectangle(
            point = point,
            topLeft = topLeft,
            size = size,
        )

        assertTrue(result)
    }

    @Test
    fun isPointInRectangleTest2() {
        val point = Offset(0f, 0f)
        val topLeft = Offset(100f, 100f)
        val size = Size(100f, 100f)

        val result = MathUtils.isPointInRectangle(
            point = point,
            topLeft = topLeft,
            size = size,
        )

        assertTrue(!result)
    }

    @Test
    fun isPointInRectangleTest3() {
        val point = Offset(100f, 100f)
        val topLeft = Offset(0f, 0f)
        val size = Size(100f, 100f)

        val result = MathUtils.isPointInRectangle(
            point = point,
            topLeft = topLeft,
            size = size,
        )

        assertTrue(result)
    }

}