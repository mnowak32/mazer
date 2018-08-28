package pl.cdbr.mazer.model.threeD

import javafx.scene.image.PixelWriter
import javafx.scene.paint.Color
import pl.cdbr.mazer.model.Config
import pl.cdbr.mazer.model.Player

data class FaceToRender(val f: Face, val p: Point) {
    val cos = f.cosineFrom(p)
    val dist = Vector.between(p, f.r.middle).length()
}

data class Scene(val p: Player, val maze: Maze3d) {
    private val point = Point(p.x, p.y, maze.height * 0.6)
    private val facesByZ = maze.facesForPosition(p)
            .map { FaceToRender(it, point) }
//            .filter { it.cos <= 0 } //odrzuć niewidoczne (odwrócone "tyłem")
            .sortedBy { it.dist }

    fun drawOnto(pixels: PixelWriter) {
        val viewPort = currentViewPort()
        (0 until Config.screenY).forEach { y ->
            (0 until Config.screenX).forEach { x ->
                val ratioX = (x.toDouble() / Config.screenX)
                val ratioY = (y.toDouble() / Config.screenY)
                val lookAt = Vector.between(viewPort.pointAt(ratioX, ratioY), point)
                val face = facesByZ
                        .find { intersects(point, lookAt, it.f.r) }
                val color = face?.let {
                    it.f.color.interpolate(Color.BLACK, (1.0 - it.cos))
                } ?: Color.MAGENTA
                pixels.setColor(x, y, color)
            }
        }
    }


    private fun currentViewPort() = baseViewPort.rotateZ(p.heading).translate(point.toVector())

    private fun intersects(origin: Point, lookAt: Vector, face: Rect) =
            intersectsTriangle(origin, lookAt, face) || intersectsTriangle(origin, lookAt, face.reversed)

    // Möller–Trumbore intersection algorithm
    private fun intersectsTriangle(origin: Point, lookAt: Vector, face: Rect): Boolean {
        val pv = lookAt * face.v1
        val det = face.v2 dot pv
        return if (inEpsilon(det)) {
            false
        } else {
            val inv = 1 / det
            val tv = Vector.between(origin, face.p1)
            val u = tv dot pv * inv
            if (u < 0.0 || u > 1.0) {
                false
            } else {
                val qv = tv * face.v2
                val v = lookAt dot qv * inv
                @Suppress("RedundantIf")
                if (v < 0.0 || (u + v) > 1.0) {
                    false
                } else {
//                    val t = face.v1 % qv * inv
                    true
                }
            }
        }
    }

    companion object {
        private const val EPSILON = 0.000001
        fun inEpsilon(d: Double) = (d < EPSILON && d > -EPSILON)

        // viewPort dla pozycji (0,0,0) i lookAt (1,0,0)
        private val baseViewPort = Rect(
                Point(Config.viewPortDistance, Config.viewPortWidth / 2, Config.viewPortHeight / 2),
                Vector(0.0, -Config.viewPortWidth, 0.0),
                Vector(0.0, 0.0, -Config.viewPortHeight)
        )
    }
}