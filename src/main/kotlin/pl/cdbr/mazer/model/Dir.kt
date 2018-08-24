package pl.cdbr.mazer.model

// Reprezentacja czterech głównych kierunków.
// Funkcja "move()" zwraca nową parę współrzędnych na podstawie podanych (x, y) i kierunku.
// Funkcja "reverse()" zwraca kierunek odwrotny od danego.
enum class Dir(val move: (Int, Int) -> Pair<Int, Int>, val reverse: () -> Dir) {
    N({ x, y -> x to y - 1 }, { Dir.S }),
    E({ x, y -> x + 1 to y }, { Dir.W }),
    S({ x, y -> x to y + 1 }, { Dir.N }),
    W({ x, y -> x - 1 to y }, { Dir.E });

    companion object {
        val allDirs = setOf(N, E, S, W)
    }
}
