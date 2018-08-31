package pl.cdbr.mazer.model.threeD

import org.junit.Test

import org.junit.Assert.*

class RectTest {
    private val rect = Rect(Point(0.0, 0.0, 0.0), Vector(5.0, 0.0, 0.0), Vector(0.0, -2.0, 0.0))
    private val EPSILON = 0.00001

    @Test
    fun testPointAt() {
        val p = rect.pointAt(0.5, 0.5)
        println(p)
        assertEquals(Point(2.5, -1.0, 0.0), p)
    }

    @Test
    fun testRectCoords() {
        val (dx, dy) = rect.rectCoords(Point(2.5, -1.0, 0.0))
        println("$dx, $dy")
        assertEquals(0.5, dx, EPSILON)
        assertEquals(0.5, dy, EPSILON)
    }
}