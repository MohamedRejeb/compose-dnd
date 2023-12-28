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
