package pl.cdbr.mazer.model.threeD

import javafx.scene.image.PixelWriter
import javafx.scene.paint.Color
import pl.cdbr.mazer.model.Config
import pl.cdbr.mazer.model.Player

data class FaceToRender(val f: Face, val p: Point) {
    val cos = f.cosineFrom(p)
    val dist = Vector.between(p, f.r.middle).length()

    override fun toString() = "\n$f"
}

data class Scene(val p: Player, val maze: Maze3d) {
    private val point = Point(p.x, p.y, maze.height * 0.6)
    private val facesByZ = maze.facesForPosition(p)
            .map { FaceToRender(it, point) }
//            .filter { it.cos <= 0 } //odrzuć niewidoczne (odwrócone "tyłem")
            .sortedBy { it.dist }

    fun drawOnto(pixels: PixelWriter) {
//        println("Faces in view: $facesByZ")
        val viewPort = currentViewPort()
//        println("ViewPort: $viewPort")
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

    companion object {
        private const val EPSILON = 0.000001
        fun inEpsilon(d: Double) = (d < EPSILON && d > -EPSILON)

        // viewPort dla pozycji (0,0,0) i lookAt (1,0,0)
        private val baseViewPort = Rect(
                Point(Config.viewPortDistance, Config.viewPortWidth / 2, Config.viewPortHeight / 2),
                Vector(0.0, -Config.viewPortWidth, 0.0),
                Vector(0.0, 0.0, -Config.viewPortHeight)
        )

        internal fun intersects(origin: Point, lookAt: Vector, face: Rect) =
//            intersectsTriangleMT(origin, lookAt, face) || intersectsTriangleMT(origin, lookAt, face.reversed)
            intersectsRectangleSimple(origin, lookAt, face)

        // Möller–Trumbore intersection algorithm
        private fun intersectsTriangleMT(origin: Point, lookAt: Vector, face: Rect): Boolean {
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

        // simple geometric algorithm
        private fun intersectsRectangleSimple(origin: Point, lookAt: Vector, face: Rect): Boolean {
            val d = face.normal dot face.p1.toVector()
            val dotNandL = face.normal dot lookAt
            return if (inEpsilon(dotNandL)) { //normal i lookAt są prostopadłe
                false
            } else {
                val t = ((face.normal dot origin.toVector()) + d) / dotNandL
                if (t < 0.0) { // face jest z tyłu "kamery"
                    false
                } else {
                    val p = origin + lookAt * t

                    fun isOnLeft(edge: Vector, toPoint: Vector): Boolean {
                        val c = edge * toPoint
                        return (face.normal dot c) < 0.0
                    }
                    //sprawdź po kolei, czy punkt "p" jest po
                    // lewej stronie każdej krawędzi (na płaszczyźnie "face")
                    isOnLeft(face.v1, Vector.between(face.p1, p))
                            &&
                    isOnLeft(face.v2, Vector.between(face.p2, p))
                            &&
                    isOnLeft(-face.v1, Vector.between(face.p3, p))
                            &&
                    isOnLeft(-face.v2, Vector.between(face.p4, p))
                }
            }

        }
    }
}