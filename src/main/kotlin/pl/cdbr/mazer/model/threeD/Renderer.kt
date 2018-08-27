package pl.cdbr.mazer.model.threeD

import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import pl.cdbr.mazer.model.Config
import pl.cdbr.mazer.model.Player

data class FaceToRender(val f: Face, val p: Point) {
    val cos = f.cosineFrom(p)
    val dist = Vector.between(p, f.r.middle).length()
}

data class Scene(val p: Player, val maze: Maze3d) {
    val point = Point(p.x, p.y, maze.height * 0.6)
    val lookAt = Vector.UNIT.rotateZ(p.heading)
    val facesByZ = maze.facesForPosition(p)
            .map { FaceToRender(it, point) }
            .sortedBy { it.dist }

    fun drawOnto(img: WritableImage) {
        val pixels = img.pixelWriter

        val viewPort = TODO() //Rect()
        val pixelAngle = Config.viewPortAngle / Config.screenX
        val topLeftX = (Config)
        (0 .. Config.screenY).forEach { y ->
            (0 .. Config.screenX).forEach { x ->

            }
        }
    }

    // Möller–Trumbore intersection algorithm
    private fun intersects(origin: Point, lookAt: Vector, face: Rect) =
            intersectsTriangle(origin, lookAt, face) || intersectsTriangle(origin, lookAt, face.reversed)

    private fun intersectsTriangle(origin: Point, lookAt: Vector, face: Rect): Boolean {
        val pv = lookAt * face.v2
        val det = face.v1 % pv
        return if (inEpsilon(det)) {
            false
        } else {
            val inv = 1 / det
            val tv = Vector.between(origin, face.p1)
            val u = tv % pv * inv
            if (u < 0.0 || u > 1.0) {
                false
            } else {
                val qv = tv * face.v1
                val v = lookAt % qv * inv
                @Suppress("RedundantIf")
                if (v < 0.0 || (u + v) > 1.0) {
                    false
                } else {
//                    val t = face.v2 % qv * inv
                    true
                }
            }
        }
    }

    companion object {
        const val EPSILON = 0.000001
        fun inEpsilon(d: Double) = (d < EPSILON && d > -EPSILON)
    }
}