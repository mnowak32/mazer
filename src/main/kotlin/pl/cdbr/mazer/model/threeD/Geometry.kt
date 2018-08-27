package pl.cdbr.mazer.model.threeD


data class Point(val x: Double, val y: Double, val z: Double) {
    operator fun plus(v: Vector) = Point(x + v.dx, y + v.dy, z + v.dz)
}

data class Vector(val dx: Double, val dy: Double, val dz: Double) {//technicznie to samo co punkt, ale semantycznie nie
    operator fun plus(v: Vector) = Vector(dx + v.dx, dy + v.dy, dz + v.dz)
    // cross product / iloczyn wektorowy
    operator fun times(v: Vector) = Vector(
                dy * v.dz - dz * v.dy,
                dz * v.dx - dx * v.dz,
                dx * v.dy - dy * v.dx
        )
    operator fun times(m: Double) = Vector(dx * m, dy * m, dz * m)
    operator fun div(m: Double) = times(1 / m)
    // dot product / iloczyn skalarny
    operator fun rem(v: Vector) = dx * v.dx + dy * v.dy + dz * v.dz

    fun length() = Math.sqrt(dx.sqr() + dy.sqr() + dz.sqr())
    fun normalize(): Vector {
        val len = length()
        return if (len == 0.0) {
            Vector.ZERO
        } else {
            this / len
        }
    }

    companion object {
        val ZERO = Vector(0.0, 0.0, 0.0)
        fun between(p1: Point, p2: Point) = Vector(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z)
    }
}

data class Rect(val p1: Point, val v1: Vector, val v2: Vector) {
    val p2 = p1 + v1
    val p3 = p2 + v2
    val p4 = p1 + v2

    val normal = (v2 * v1).normalize()
    val middle = p1 + v1 * 0.5 + v2 * 0.5
}

fun Double.sqr() = this * this
