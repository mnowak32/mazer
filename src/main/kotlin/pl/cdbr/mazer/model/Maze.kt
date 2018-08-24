package pl.cdbr.mazer.model

class Cell(
        val x: Int, val y: Int,
        val exits: MutableSet<Dir> = mutableSetOf()
) {

    fun growTo(d: Dir): Cell {
        val (newX, newY) = d.move(x, y)
        val rev = d.reverse()
        val c = Cell(newX, newY)
        exits.add(d)
        c.exits.add(rev)
        return c
    }

    fun lines() = Pair(
            if (Dir.N in exits) "##  " else "####",
            if (Dir.W in exits) "    " else "##  "
    )
}

class Maze(val xs: Int, val ys: Int) {
    private val cells = Array(ys) { _ ->
        Array<Cell?>(xs) {
            null
        }
    }

    val startCell = putCell(Cell(0, 0))

    private fun putCell(c: Cell): Cell {
        cells[c.y][c.x] = c
        return c
    }

    fun getCell(x: Int, y: Int) = if (overLimits(x, y)) { null } else { cells[y][x] }

    private fun overLimits(x: Int, y: Int) = (x < 0 || x >= xs || y < 0 || y >= ys)

    private fun canGrow(c: Cell, d: Dir): Boolean {
        val (nx, ny) = d.move(c.x, c.y)
        return if (overLimits(nx, ny)) {
            false
        } else {
            cells[ny][nx] == null
        }
    }

    fun tryGrow(c: Cell, d: Dir) = if (canGrow(c, d)) {
            val newCell = c.growTo(d)
            putCell(newCell)
        } else {
            null
        }

    fun print() {
        val emptyCell = "    " to "    "
        cells.forEach { row ->
            val lines = row.fold("" to "") { l, c ->
                val ln = c?.lines() ?: emptyCell
                (l.first + ln.first) to (l.second + ln.second)
            }
            println(lines.first + "##")
            println(lines.second + "##")
        }
        println("##".repeat(xs * 2 + 1))
    }
}

class MazeGen(private val maze: Maze) {
    fun generate() {
        branchFrom(maze.startCell)
    }

    private fun branchFrom(cell: Cell) {
        val emptyDirs = (Dir.valuesSet - cell.exits).shuffled()
        emptyDirs.forEach { emptyDir ->
            maze.tryGrow(cell, emptyDir)?.let {
                branchFrom(it)
            }
        }
    }
}

fun main(args: Array<String>) {
    val m = Maze(20, 16)
    MazeGen(m).generate()
    m.print()
}
