package pl.cdbr.mazer.model.threeD

import org.junit.Test

class SceneTest {

    @Test
    fun testIntersections() {
        val origin = Point.ZERO
        val lookAt = Vector(1.0, 1.0, 0.0)
        val target = Rect(Point(5.0, 2.0, 2.0), Vector(-3.0, 3.0, 0.0), Vector(0.0, 0.0, -4.0))
        assert(Scene.intersects(origin, lookAt, target))
    }
}
