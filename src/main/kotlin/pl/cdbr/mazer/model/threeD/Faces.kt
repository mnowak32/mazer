package pl.cdbr.mazer.model.threeD

import javafx.scene.paint.Color
import pl.cdbr.mazer.model.*
import java.io.File
import kotlin.math.roundToLong

data class Face(val r: Rect, val texture: Texture) {
    inner class Cosines(val c1: Double, val c2: Double, val c3: Double, val c4: Double) {
        // implementacja cieniowania Gourad'a
        fun cosAtPoint(p: Point?): Double {
            if (p == null) { return 0.0 }
            val (dv1, dv2) = r.rectCoords(p)
//            println("$dv1, $dv2")
            return cosAtPoint(dv1, dv2)
        }
        fun cosAtPoint(dv1: Double, dv2: Double): Double {
//            println("$dv1, $dv2")
            val c12 = c2 * dv1 + c1 * (1 - dv1)
            val c34 = c3 * dv1 + c4 * (1 - dv1)
            return c12 * (1 - dv2) + c34 * dv2
        }

        override fun toString() = "Cosines($c1, $c2, $c3, $c4)"

    }

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
    fun cosineFrom(p: Point) = (Vector.between(p, r.middle).normalize() dot r.normal)
    fun cosinesFrom(p: Point) = Cosines(
            Vector.between(p, r.p1).normalize() dot r.normal,
            Vector.between(p, r.p2).normalize() dot r.normal,
            Vector.between(p, r.p3).normalize() dot r.normal,
            Vector.between(p, r.p4).normalize() dot r.normal
    )
    fun translate(v: Vector) = Face(r.translate(v), texture)
    fun rotateZ(fi: Double) = Face(r.rotateZ(fi), texture)

    fun type() = when (texture) {
        Texture.wall -> "W"
        Texture.ceil -> "C"
        Texture.floor -> "F"
        else -> "?"
    }
    override fun toString() = "Face(${type()}, $r)"
}

// Klasa wykonuje konwersję labiryntu 2d (Maze) na listę ścian w każdej lokalizacji (x, y)
data class Maze3d(private val maze: Maze, val height: Double) {
    // ściany w przypadku istnienia wyjścia w danym kierunku
    private val exitFaces = listOf(
            Face(Rect(Point(0.25, 0.25, 0.0), Vector(0.25, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Texture.floor),
            Face(Rect(Point(0.5, 0.25, height), Vector(-0.25, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Texture.ceil),
            Face(Rect(Point(0.25, 0.25, height), Vector(0.25, 0.0, 0.0), Vector(0.0, 0.0, -height)), Texture.wall),
            Face(Rect(Point(0.5, -0.25, height), Vector(-0.25, 0.0, 0.0), Vector(0.0, 0.0, -height)), Texture.wall)
    )
    // ściana gdy nie ma wyjścia
    private val noExitFaces = listOf(
            Face(Rect(Point(0.25, 0.25, height), Vector(0.0, -0.5, 0.0), Vector(0.0, 0.0, -height)), Texture.wall)
    )
    private val centralFaces = listOf(
            Face(Rect(Point(-0.25, 0.25, 0.0), Vector(0.5, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Texture.floor),
            Face(Rect(Point(0.25, 0.25, height), Vector(-0.5, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Texture.ceil)
    )

    //dla każdej lokalizacji w labiryncie tworzymy listę ścian
    // ograniczających ten obszar.
    private val faces = Array(maze.ys) { y ->
        Array(maze.xs) { x ->
            val c = maze.getCell(x, y)
            if (c == null) {
                emptyList()
            } else {
                val vect = Vector(c.xd, c.yd, 0.0)
                (Dir.allDirs.flatMap { dirFaces(c, it) } + centralFaces)
                        .map { it.translate(vect) }
            }
        }
    }

    fun getFaces(x: Int, y: Int) = faces[y][x]

    private fun getCellsInDir(here: Cell, d: Dir): List<Cell> {
        return if (here.exits.contains(d)) {
            val (newX, newY) = d.move(here.x, here.y)
            val newCell = maze.getCell(newX, newY)
            if (newCell != null) {
                listOf(newCell) + getCellsInDir(newCell, d)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun facesForPosition(p: Player): List<Face> {
        //pobieramy ściany dla:
        // - aktualnej pozycji
        // - wszystkich pozycji w widocznych kierunkach w linii prostej (jeżeli są przejścia)
        val here = maze.getCell(p.xInt, p.yInt)
        return if (here != null) {
            val dirs = Dir.inView(p.heading)
            val posList = here.exits.intersect(dirs)
            //includes "here"
            val cells = listOf(here) + posList.flatMap { getCellsInDir(here, it) }
            cells.flatMap { faces[it.y][it.x] }
        } else {
            emptyList()
        }
    }

    fun dirFaces(c: Cell, d: Dir): List<Face> {
        return if (d in c.exits) {
            exitFaces
        } else {
            noExitFaces
        }.map { it.rotateZ(d.heading()) }
    }

    fun exportStl() {
        File("export.stl").printWriter().use { out ->
            out.println("solid maze")

            val allFaces = faces.flatMap { it.flatMap { it } }
//            val allFaces = facesForPosition(Player(0.0, 0.0, 0.0))
            allFaces.forEach { f ->
                val rect = f.r
                out.println("""
  facet normal ${rect.normal.dx.toExport()} ${rect.normal.dy.toExport()} ${rect.normal.dz.toExport()}
    outer loop
      vertex ${rect.p1.x.toExport()} ${rect.p1.y.toExport()} ${rect.p1.z.toExport()}
      vertex ${rect.p4.x.toExport()} ${rect.p4.y.toExport()} ${rect.p4.z.toExport()}
      vertex ${rect.p2.x.toExport()} ${rect.p2.y.toExport()} ${rect.p2.z.toExport()}
    endloop
  endfacet
  facet normal ${rect.normal.dx.toExport()} ${rect.normal.dy.toExport()} ${rect.normal.dz.toExport()}
    outer loop
      vertex ${rect.p3.x.toExport()} ${rect.p3.y.toExport()} ${rect.p3.z.toExport()}
      vertex ${rect.p2.x.toExport()} ${rect.p2.y.toExport()} ${rect.p2.z.toExport()}
      vertex ${rect.p4.x.toExport()} ${rect.p4.y.toExport()} ${rect.p4.z.toExport()}
    endloop
  endfacet""")
            }

            out.println("endsolid")
        }
    }

}
fun Double.toExport() = (this * 100.0).roundToLong() / 10.0

