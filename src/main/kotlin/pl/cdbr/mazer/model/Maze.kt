package pl.cdbr.mazer.model

// Klasa reprezentująca pojedynczą komórkę labiryntu.
// Zna swoje położenie (x i y) oraz w którą stronę ma wyjścia (exits).
class Cell(
        val x: Int, val y: Int,
        val exits: MutableSet<Dir> = mutableSetOf()
) {
    val xd = x.toDouble()
    val yd = y.toDouble()

    // Funkcja "pączkująca" w podanym kierunku (d). Zwraca nowo wypączkowaną komórkę.
    // Tworzy z obu stron wyjścia w zbiorze "exits".
    fun growTo(d: Dir): Cell {
        val (newX, newY) = d.move(x, y)
        val rev = d.reverse()
        val c = Cell(newX, newY)
        exits.add(d)
        c.exits.add(rev)
        return c
    }

    //Funkcja pomocnicza do rysowania na konsoli
    fun lines() = Pair(
            if (Dir.N in exits) "##  " else "####",
            if (Dir.W in exits) "    " else "##  "
    )
}

// Klasa reprezentuje labirynt.
// Posiada wiedzę o rozmiarze (xs, ys) oraz o położeniu komórek (cells).
// Potrafi okreslić, czy komórka może "wypączkować" w którąś ze stron.
class Maze(val xs: Int, val ys: Int) {
    private val cells = Array(ys) { _ ->
        Array<Cell?>(xs) {
            null
        }
    }

    // Pierwsza komórka
    val startCell = putCell(Cell(0, 0))

    // Umieszcza komórkę w labiryncie oraz ją zwraca (umożliwiając method chaining).
    private fun putCell(c: Cell): Cell {
        cells[c.y][c.x] = c
        return c
    }

    // Zwraca komórkę w danym położeniu (x, y), lub null jeżeli
    // nie ma tam jeszcze komórki albo podano położenie poza labiryntem.
    fun getCell(x: Int, y: Int) = if (overLimits(x, y)) { null } else { cells[y][x] }

    // Sprawdza, czy położenie (x, y) znajduje się poza granicami labiryntu
    private fun overLimits(x: Int, y: Int) = (x < 0 || x >= xs || y < 0 || y >= ys)

    // Sprawdza, czy komórka (c) może "wypączkować" w daną stronę (d).
    // Jest to oczywiście możliwe tylko w granicach labiryntu.
    private fun canGrow(c: Cell, d: Dir): Boolean {
        //określa położenie w kierunku "d" względem komórki "c"
        val (nx, ny) = d.move(c.x, c.y)
        return if (overLimits(nx, ny)) {
            //jeżeli położenie poza labiryntem
            false
        } else {
            //czy podana lokalizacja jest pusta
            cells[ny][nx] == null
        }
    }

    // Próbuje "wypączkować" z danej komórki (c) w określonym kierunku (d).
    // Jeżeli się to udało, zwraca nową komórkę, w przeciwnym przypadku null.
    fun tryGrow(c: Cell, d: Dir) = if (canGrow(c, d)) {
            val newCell = c.growTo(d)
            putCell(newCell)
        } else {
            null
        }

    // Funkcja pomocnicza do rysowania labiryntu w konsoli.
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

// Generator labiryntu.
class MazeGen(private val maze: Maze) {
    fun generate() {
        branchFrom(maze.startCell) //zacznij od pierwszej komórki
    }

    private fun branchFrom(cell: Cell) {
        Dir.allDirs //wszystkie kierunki
            .shuffled() //w losowej kolejności
            .forEach {  dir ->
                //spróbuj "wypączkować" w tą stronę (dir == aktualny kierunek)
                maze.tryGrow(cell, dir)?.let {
                    //jeżeli się udało (tryGrow zwrócił nie-null), to rekurencyjnie brnij dalej
                    branchFrom(it)
                }
            }
    }
}

// W ramach testów tworzy labirynt i wypisuje go na konsoli.
fun main(args: Array<String>) {
    val m = Maze(20, 16)
    MazeGen(m).generate()
    m.print()
}
