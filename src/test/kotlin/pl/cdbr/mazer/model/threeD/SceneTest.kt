package pl.cdbr.mazer.model.threeD

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.assertNotNull
import kotlin.test.assertNull

enum class Setup(private val p: Point, private val v: Vector, private val r: Rect, private val expect: Boolean) {
    EASY1(
            Point.ZERO,
            Vector(1.0, 1.0, 0.0),
            Rect(Point(2.0, 5.0, 2.0), Vector(3.0, -3.0, 0.0), Vector(0.0, 0.0, -5.0)),
            true
    ),
    EASY2(
            Point(0.5, -0.5, 0.2),
            Vector(1.0, 1.0, 0.0),
            Rect(Point(2.0, 5.0, 2.0), Vector(3.0, -3.0, 0.0), Vector(0.0, 0.0, -5.0)),
            true
    ),
    EASY3(
            Point(0.5, -3.5, -0.2),
            Vector(1.0, 1.0, 0.0),
            Rect(Point(2.0, 5.0, 2.0), Vector(3.0, -3.0, 0.0), Vector(0.0, 0.0, -5.0)),
            false
    );

    fun testWith(f: Func) {
        val result = f.intersects(p, v, r)
        if (expect) {
            assertNotNull(result, "$f for $this")
        } else {
            assertNull(result, "$f for $this")
        }

    }
}

enum class Func(val intersects: (Point, Vector, Rect) -> Point?) {
    BASIC(Scene.Companion::intersectsRectangleBasic),
    MT(Scene.Companion::intersectsTriangleMT)
}

@RunWith(Parameterized::class)
class SceneTest(val case: Pair<Setup, Func>) {

    @Test
    fun intersectionTest() = case.first.testWith(case.second)

    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun testSetups() = listOf(
          Setup.EASY1 to Func.BASIC,
//          Setup.EASY1 to Func.MT,
          Setup.EASY2 to Func.BASIC,
//          Setup.EASY2 to Func.MT,
          Setup.EASY3 to Func.BASIC
//          Setup.EASY3 to Func.MT
        );
    }

}
