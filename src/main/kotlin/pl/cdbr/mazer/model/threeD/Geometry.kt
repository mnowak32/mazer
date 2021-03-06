@file:Suppress("unused")

package pl.cdbr.mazer.model.threeD

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


data class Point(val x: Double, val y: Double, val z: Double) {
    operator fun plus(v: Vector) = Point(x + v.dx, y + v.dy, z + v.dz)

    fun toVector() = Vector(x, y, z)

    override fun toString() = "Point($x, $y, $z)"

    companion object {
        val ZERO = Point(0.0, 0.0, 0.0)
    }
}

data class Vector(val dx: Double, val dy: Double, val dz: Double) {//technicznie to samo co punkt, ale semantycznie nie
    operator fun plus(v: Vector) = Vector(dx + v.dx, dy + v.dy, dz + v.dz)
    operator fun minus(v: Vector) = Vector(dx - v.dx, dy - v.dy, dz - v.dz)
    operator fun unaryMinus() = Vector(-dx, -dy, -dz)
    // cross product / iloczyn wektorowy
    operator fun times(v: Vector) = Vector(
                dy * v.dz - dz * v.dy,
                dz * v.dx - dx * v.dz,
                dx * v.dy - dy * v.dx
        )
    operator fun times(m: Double) = Vector(dx * m, dy * m, dz * m)
    operator fun div(m: Double) = Vector(dx / m, dy / m, dz / m)
    // dot product / iloczyn skalarny
    infix fun dot(v: Vector) = dx * v.dx + dy * v.dy + dz * v.dz

    fun length() = Math.sqrt(dx * dx + dy * dy + dz * dz)
    // tworzy wektor o długości 1.0 (lub 0, jeżeli i tak był zerowy)
    fun normalize(): Vector {
        val len = length()
        return if (len == 0.0) {
            Vector.ZERO
        } else {
            this / len
        }
    }

    fun rotateZ(fi: Double): Vector {
        val angRad = fi * PI / 180.0
        val cosFi = cos(angRad)
        val sinFi = sin(angRad)
        return Vector(dx * cosFi - dy * sinFi, dx * sinFi + dy * cosFi, dz)
    }

    fun rotateY(fi: Double): Vector {
        val angRad = fi * PI / 180.0
        val cosFi = cos(angRad)
        val sinFi = sin(angRad)
        return Vector(dx * cosFi + dz * sinFi, dy, - dx * sinFi + dz * cosFi)
    }

    fun toPoint() = Point(dx, dy, dz)

    companion object {
        val ZERO = Vector(0.0, 0.0, 0.0)
        fun between(p1: Point, p2: Point) = Vector(p2.x - p1.x, p2.y - p1.y, p2.z - p1.z)
    }
}

data class Rect(val p1: Point, val v1: Vector, val v2: Vector) {
    val p2 = p1 + v1
    val p3 = p2 + v2
    val p4 = p1 + v2
    val reversed by lazy { Rect(p3, -v1, -v2) }
    val v1n = v1.normalize()
    val v1l = v1.length()
    val v2n = v2.normalize()
    val v2l = v2.length()

    val normal = (v2n * v1n)
    val middle = p1 + v1 * 0.5 + v2 * 0.5

    fun translate(v: Vector) = Rect(p1 + v, v1, v2)
    // Obrót wokół osi Z (punkt 0, 0)
    fun rotateZ(fi: Double) = Rect(p1.toVector().rotateZ(fi).toPoint(), v1.rotateZ(fi), v2.rotateZ(fi))
    // Obrót wokół osi Y (punkt 0, 0)
    fun rotateY(fi: Double) = Rect(p1.toVector().rotateY(fi).toPoint(), v1.rotateY(fi), v2.rotateY(fi))

    // Oblicza punkt na powierzchni prostokąta. dx i dy to współczynniki
    // przesunięcia wzdłuż wektorów v1 i v2 (od 0 do 1)
    fun pointAt(dx: Double, dy: Double) = p1 + v1 * dx + v2 * dy

    // Funkcja odwrotna do poprzedniej - na podstawie punktu zwraca współrzędne w
    // "układzie współrzędnych" czworokąta (zero w p1, osie v1 i v2).
    fun rectCoords(p: Point): Pair<Double, Double> {
        val vp = Vector.between(p1, p)
        return (vp dot v1n) / v1l to (vp dot v2n) / v2l
    }

    override fun toString() = "Rect($p1, $p2, $p3, $p4, norm: $normal)"
}
