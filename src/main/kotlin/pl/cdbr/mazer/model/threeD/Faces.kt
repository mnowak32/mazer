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
    fun cosineFrom(p: Point) = (Vector.between(r.middle, p).normalize() dot r.normal)
    fun translate(v: Vector) = Face(r.translate(v), color)
    fun rotateZ(fi: Double) = Face(r.rotateZ(fi), color)
}

// Klasa wykonuje konwersję labiryntu 2d (Maze) na listę ścian w każdej lokalizacji (x, y)
data class Maze3d(private val maze: Maze, val height: Double) {
    // ściany w przypadku istnienia wyjścia w danym kierunku
    private val exitFaces = listOf(
            Face(Rect(Point(0.25, 0.25, 0.0), Vector(0.25, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Config.floorColor),
            Face(Rect(Point(0.25, 0.25, height), Vector(0.25, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Config.ceilColor),
            Face(Rect(Point(0.25, 0.25, height), Vector(0.25, 0.0, 0.0), Vector(0.0, 0.0, -height)), Config.wallColor),
            Face(Rect(Point(0.5, -0.25, height), Vector(-0.25, 0.0, 0.0), Vector(0.0, 0.0, -height)), Config.wallColor)
    )
    // ściana gdy nie ma wyjścia
    private val noExitFaces = listOf(
            Face(Rect(Point(0.25, 0.25, height), Vector(0.0, -0.5, 0.0), Vector(0.0, 0.0, -height)), Config.wallColor)
    )
    private val centralFaces = listOf(
            Face(Rect(Point(-0.25, 0.25, 0.0), Vector(0.5, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Config.floorColor),
            Face(Rect(Point(-0.25, 0.25, height), Vector(0.5, 0.0, 0.0), Vector(0.0, -0.5, 0.0)), Config.ceilColor)
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

    fun exportDfx() {
        File("export.dxf").printWriter().use { out ->
            out.println("""
999
DXF created from aMaze
0
SECTION
2
HEADER
9
${'$'}ACADVER
1
AC1006
9
${'$'}INSBASE
10
0.0
20
0.0
30
0.0
9
${'$'}EXTMIN
10
0.0
20
0.0
9
${'$'}EXTMAX
10
1000.0
20
1000.0
0
ENDSEC
0
SECTION
2
BLOCKS
0
ENDSEC
0
SECTION
2
ENTITIES
            """.trimIndent())

            val allFaces = faces.flatMap { it.flatMap { it } }
            allFaces.forEach { f ->
                val rect = f.r
                out.println("""
0
3DFACE
62
3
10
${rect.p1.x.toExport()}
20
${rect.p1.y.toExport()}
30
${rect.p1.z.toExport()}
11
${rect.p2.x.toExport()}
21
${rect.p2.y.toExport()}
31
${rect.p2.z.toExport()}
12
${rect.p3.x.toExport()}
22
${rect.p3.y.toExport()}
32
${rect.p3.z.toExport()}
13
${rect.p4.x.toExport()}
23
${rect.p4.y.toExport()}
33
${rect.p4.z.toExport()}
                """.trimIndent())
            }

            out.print("""
0
ENDSEC
0
EOF
            """.trimIndent())
        }
    }

}
fun Double.toExport() = (this * 100.0).roundToLong() / 10.0

