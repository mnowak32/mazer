package pl.cdbr.mazer.model

import kotlin.math.abs

// Reprezentacja czterech głównych kierunków.
// Funkcja "move()" zwraca nową parę współrzędnych na podstawie podanych (x, y) i kierunku.
// Funkcja "reverse()" zwraca kierunek odwrotny od danego.
enum class Dir(val move: (Int, Int) -> Pair<Int, Int>, val reverse: () -> Dir, val heading: () -> Double) {
    N({ x, y -> x to y - 1 }, { Dir.S }, { 90.0 }),
    E({ x, y -> x + 1 to y }, { Dir.W }, { 0.0 }),
    S({ x, y -> x to y + 1 }, { Dir.N }, { 270.0 }),
    W({ x, y -> x - 1 to y }, { Dir.E }, { 180.0 });

    companion object {
        val allDirs = setOf(N, E, S, W)
        fun fromAngle(head: Double) = when {
            head > 45.0 && head <= 135.0 -> Dir.N
            head > 135.0 && head <= 225.0 -> Dir.W
            head > 225.0 && head <= 315.0 -> Dir.S
            else -> Dir.E
        }
        fun inView(head: Double) = values().filter {
            val diff = abs(it.heading() - head)
            diff <= 90.0 || diff >= 270.0
        }
    }
}
