package pl.cdbr.mazer.model.threeD

import javafx.scene.paint.Color
import pl.cdbr.mazer.app.Config
import pl.cdbr.mazer.model.Cell
import pl.cdbr.mazer.model.Dir
import pl.cdbr.mazer.model.Maze

data class Face(val r: Rect, val color: Color) {
    // Funkcja poniższa wykorzystywana jest do dwóch rzeczy:
    // 1. Back-face culling:
    // Jeżeli kąt pomiędzy wektorami:
    //  - pomiędzy kamerą i środkiem ściany oraz
    //  - wektorem normalnym ściany
    // jest ostry, to znaczy, że "widzimy" jej tył - czyli chuja tam widzimy.
    // Realizacja polega na obliczeniu iloczynu skalarnego (%) pomiędzy tymi wektorami -
    // jeżeli jest < 0, to kąt jest rozwarty i ściana jest widoczna.
    // Jeżeli oba wektory są jednostkowe, to iloczyn równa się cosinusowi tego kąta.
    // 2. Cieniowanie:
    // Ten sam cosinus (na minusie) określa jasność ściany przy cieniowaniu płaskim.
    fun cosineFrom(p: Point) = (Vector.between(p, r.middle).normalize() % r.normal)
}

data class Maze3d(private val maze: Maze, private val height: Double) {
    //dla każdej lokalizacji w labiryncie tworzymy listę ścian
    // ograniczających ten obszar.
    private val faces = Array(maze.ys) { y ->
        Array(maze.xs) { x ->
            val c = maze.getCell(x, y)
            if (c == null) {
                emptyList<Face>()
            } else {
                Dir.allDirs.map { exitFaces(c, it) }
                +
                centralFaces(c)
            }
        }
    }

    private val exitRects = mapOf(
            Dir.N to listOf(
                    Rect(Point(-0.25, -0.5, 0.0), Vector(0.5, 0.0, 0.0), Vector(0.0, 0.25, 0.0)),
                    Rect()
            ),
            Dir.E to listOf(

            )
    )

    fun exitFaces(c: Cell, d: Dir): List<Face> {
        return if (d in c.exits) {
            val ceil =
            listOf(

            )
        } else {

        }
    }

    fun centralFaces(c: Cell): List<Face> {
        //sufit
        val ceil = Rect(Point(c.xd - 0.25, c.yd - 0.25, 0.0), Vector(0.5, 0.0, 0.0), Vector(0.0, 0.5, 0.0))
        //podłoga
        val floor = Rect(Point(c.xd - 0.25, c.yd - 0.25, height), Vector(0.5, 0.0, 0.0), Vector(0.0, 0.5, 0.0))
        return listOf(Face(ceil, Config.ceilColor), Face(floor, Config.floorColor))
    }

}

