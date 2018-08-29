package pl.cdbr.mazer.model.threeD

import javafx.scene.paint.Color
import pl.cdbr.mazer.model.Config
import pl.cdbr.mazer.model.Cell
import pl.cdbr.mazer.model.Dir
import pl.cdbr.mazer.model.Maze
import pl.cdbr.mazer.model.Player
import java.io.File
import kotlin.math.roundToLong

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
    fun cosineFrom(p: Point) = (Vector.between(p, r.middle).normalize() dot r.normal)
    fun translate(v: Vector) = Face(r.translate(v), color)
    fun rotateZ(fi: Double) = Face(r.rotateZ(fi), color)

    fun type() = when (color) {
        Config.wallColor -> "W"
        Config.ceilColor -> "C"
        Config.floorColor -> "F"
        else -> "?"
    }
    override fun toString() = "Face(${type()}, $r)"
}

// Klasa wykonuje konwersję labiryntu 2d (Maze) na listę ścian w każdej lokalizacji (x, y)
data class Maze3d(private val maze: Maze, val height: Double) {
    // ściany w przypadku istnienia wyjścia w danym kierunku
    private val exitFaces = listOf(
            Face(Rect(Point(0.25, 0.25, 0.0), Vector(0.25, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Config.floorColor),
            Face(Rect(Point(0.5, -0.25, height), Vector(-0.25, 0.0, 0.0), Vector(0.0, 0.5, 0.0)), Config.ceilColor),
            Face(Rect(Point(0.25, 0.25, height), Vector(0.25, 0.0, 0.0), Vector(0.0, 0.0, -height)), Config.wallColor),
            Face(Rect(Point(0.5, -0.25, height), Vector(-0.25, 0.0, 0.0), Vector(0.0, 0.0, -height)), Config.wallColor)
    )
    // ściana gdy nie ma wyjścia
    private val noExitFaces = listOf(
            Face(Rect(Point(0.25, 0.25, height), Vector(0.0, -0.5, 0.0), Vector(0.0, 0.0, -height)), Config.wallColor)
    )
    private val centralFaces = listOf(
            Face(Rect(Point(-0.25, 0.25, 0.0), Vector(0.5, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Config.floorColor),
            Face(Rect(Point(0.25, -0.25, height), Vector(-0.5, 0.0, 0.0), Vector(0.0, 0.5, 0.0)), Config.ceilColor)
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

    fun facesForPosition(p: Player): List<Face> {
        //pobieramy ściany dla:
        // - aktualnej pozycji
        // - pozycji + 1 w widocznych kierunkach (jeżeli jest przejście)
        val here = maze.getCell(p.xInt, p.yInt)
        return if (here != null) {
            val dirs = Dir.inView(p.heading)
            val posList = here.exits.intersect(dirs)
            val cells = listOf(here) + posList.map { it.move(p.xInt, p.yInt) }.mapNotNull { maze.getCell(it.first, it.second)}
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

