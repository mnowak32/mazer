package pl.cdbr.mazer.model.threeD

import javafx.scene.image.PixelWriter
import javafx.scene.paint.Color
import pl.cdbr.mazer.model.Config
import pl.cdbr.mazer.model.Player

data class FaceToRender(val f: Face, val p: Point) {
//    val cos = f.cosineFrom(p)
    val cos = f.cosinesFrom(p)
    val dist = Vector.between(p, f.r.middle).length()
    fun colorAt(point: Point?): Color {
        return if (point == null) {
            Color.CYAN
        } else {
            val (dv1, dv2) = f.r.rectCoords(point)
            val col = f.texture.colorAt(dv1, dv2)
            val dist = Vector.between(p, point).length()
//            val cap = cos.cosAtPoint(dv1, dv2)
            val brightness = 1 / (dist * dist)
            col.interpolate(Color.BLACK, 1.0 - brightness)
        }
    }


    override fun toString() = "\n$f, cos: $cos"
}

data class Scene(val p: Player, val maze: Maze3d) {
    private val point = Point(p.x, p.y, Config.headHeight)
    private val facesByZ = maze.facesForPosition(p)
            .map { FaceToRender(it, point) }
            .filter { it.cos.c1 <= 0 } //odrzuć niewidoczne (odwrócone "tyłem")
//            .filter { it.cos <= 0 } //odrzuć niewidoczne (odwrócone "tyłem")
            .sortedBy { it.dist }
            .asSequence()

    fun drawOnto(pixels: PixelWriter) {
//        println("Faces in view: ${facesByZ.toList().map{it.cos}}")
        val viewPort = currentViewPort()
//        println("ViewPort: $viewPort")
        (0 until Config.screenY).forEach { y ->
            val ratioY = (y.toDouble() / Config.screenY)
            (0 until Config.screenX).forEach { x ->
                val ratioX = (x.toDouble() / Config.screenX)
                val lookAt = Vector.between(point, viewPort.pointAt(ratioX, ratioY))
                val pf = facesByZ
                        .map { intersects(point, lookAt, it.f.r) to it }
                        .find { it.first != null }
                val color = pf?.let {
                    val (point, face) = it
                    face.colorAt(point)
//                    face.f.color.interpolate(Color.BLACK, (1.0 + face.cos))
                } ?: Color.MAGENTA
                pixels.setColor(x, y, color)
            }
        }
    }

    private fun currentViewPort() = baseViewPort
            .rotateY(p.pitch)
            .rotateZ(p.heading)
            .translate(point.toVector())

    companion object {
        private const val EPSILON = 0.000001
        private fun inEpsilon(d: Double) = (d < EPSILON && d > -EPSILON)

        // viewPort dla pozycji (0,0,0) i lookAt (1,0,0)
        private val baseViewPort = Rect(
                Point(Config.viewPortDistance, Config.viewPortWidth / 2, Config.viewPortHeight / 2),
                Vector(0.0, -Config.viewPortWidth, 0.0),
                Vector(0.0, 0.0, -Config.viewPortHeight)
        )

        internal fun intersects(origin: Point, lookAt: Vector, face: Rect) =
//            intersectsTriangleMT(origin, lookAt, face) || intersectsTriangleMT(origin, lookAt, face.reversed)
                intersectsRectangleBasic(origin, lookAt, face)

        // Möller–Trumbore intersection algorithm
        internal fun intersectsTriangleMT(origin: Point, lookAt: Vector, face: Rect): Point? {
            val pv = lookAt * face.v1
            val det = face.v2 dot pv
            return if (inEpsilon(det)) {
                null
            } else {
                val inv = 1 / det
                val tv = Vector.between(origin, face.p1)
                val u = tv dot pv * inv
                if (u < 0.0 || u > 1.0) {
                    null
                } else {
                    val qv = tv * face.v2
                    val v = lookAt dot qv * inv
                    @Suppress("RedundantIf")
                    if (v < 0.0 || (u + v) > 1.0) {
                        null
                    } else {
//                    val t = face.v1 % qv * inv
                        face.p1
                    }
                }
            }
        }

        // Implementacja "naiwna" - z podstawowych zasad geometrii wektorów
        // może nie najwydajniejsza, ale działa!
        internal fun intersectsRectangleBasic(origin: Point, lookAt: Vector, face: Rect): Point? {
            //   Cześć 1 - przecięcie promienia z płaszczyzną.
            //
            // Płaszczyzna, w której zawarty jest czworokąt "face" zdefiniowana jest
            // przez punkt (przyjmujemy face.p1) oraz wektor normalny (face.normal).
            // Do każdego punktu leżącego na płaszczyźnie (p) możemy przypisać wektor pomiędzy
            // p1 -> p; z definicji wektor ten leży na płaszczyźnie oraz jest prostopadły
            // do w. normalnego. Iloczyn skalarny tych wektorów jest więc równy 0:
            //   (1) Vector.between(p, face.p1) dot face.normal = 0
            // Punkt "origin" i wektor "lookAt" definiują półprostą, której przecięcie z płaszczyną nas
            // interesuje - promień. Każdy punkt na promieniu można określić parametrycznie jako:
            //   (2) p = origin + lookAt * t, gdzie t >= 0
            // Podstawiając (2) do (1) i rozwiązując w poszukiwaniu "t" otrzymujemy:
            //   (3) t = (Vector.between(origin, face.p1) dot face.normal) / (lookAt dot face.normal)
            // Z rónania (3) wyodrębnijmy dzielnik:
            //   (4) d = lookAt dot face.normal
            // Zwróćmy uwagę, że znak "d" jest tożsamy ze znakiem kosinusa kąta pomiędzy
            // wektorami "lookAt" oraz "face.normal". Wynika z tego, że:
            //  - jeżeli "d" jest równe 0, to płaszczyzna jest równoległa do promienia i jest niewidoczna,
            //  - jeżeli "d" jest > 0, to płaszczyna jest odwrócona "tyłem" i nie powinna być wyświetlona.
            // Przyrównanie do "0" w przypadku liczb Double zastępujemy funkcją "inEpsilon()", czyli
            // oba warunki redukujemy do jednego
            //   (5) d > -EPSILON
            val d = lookAt dot face.normal
            if (d > -EPSILON) { //płaszczyzna niewidoczna
                return null
            }
            val t = (Vector.between(origin, face.p1) dot face.normal) / d
            if (t <= 0) { //płaszczyzna "z tyłu" promienia
                return null
            }
            // "p" jest punktem przecięcia
            val p = origin + lookAt * t

            //   Część 2 - sprawdzenie, czy "p" leży wewnątrz czworokąta "face"
            // Dla każdego z wierzchołków (n in 1 .. 4) określamy 2 wektory:
            //  - vn - wektor biegnący z pn po krawędzi (jest już określony przy tworzeniu "face")
            //  - cn - wektor pomiędzy pn i p
            //   (6) cn = Vector.between(pn, p)
            // Obliczamy iloczyn wektorowy "h" pomiędzy "vn" i "cn", a następnie iloczyn skalarny
            // pomiędzy "h" i "face.normal".
            //   (7) hn = vn * cn
            //   (8) dp = hn dot face.normal
            // Jeżeli wektory "h" i "face.normal" są skierowane w tą samą stronę
            // (iloczyn (8) > 0) oznacza to, że punkt "p" leży po lewej stronie krawędzi "vn".
            // Ze względu na to, że w naszej geometrii wierzchołki czworokąta są
            // liczone zgodnie z ruchem wskazówek przy wektorze normalnym skierowanym
            // "na" liczącego, to test odwracamy - sprawdzamy prawą stronę krawędzi.
            // Jeżeli test będzie prawdziwy dla wszystkich krawędzi, oznacza to, że "p"
            // leży w środku czworokąta.
            fun onRightOf(vert: Point, edge: Vector, p: Point): Boolean {
                val cn = Vector.between(vert, p)
                val hn = edge * cn
                return (hn dot face.normal) < EPSILON
            }

            return if (onRightOf(face.p1, face.v1, p) &&
                    onRightOf(face.p2, face.v2, p) &&
                    onRightOf(face.p3, -face.v1, p) &&
                    onRightOf(face.p4, -face.v2, p)) {
                p
            } else {
                null
            }
        }
    }
}