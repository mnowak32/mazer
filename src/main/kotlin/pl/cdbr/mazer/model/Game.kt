package pl.cdbr.mazer.model

import kotlin.math.PI
import kotlin.math.roundToInt
// Klasa reprezentująca położenie gracza w labiryncie (x i y) oraz
// kierunek, w którym jest zwrócony (heading, na razie nieużywane).
data class Player(var x: Double, var y: Double, var heading: Double, var pitch: Double = 0.0) {
    var xInt: Int
        get() = x.roundToInt()
        set(v) { x = v.toDouble() }

    var yInt: Int
        get() = y.roundToInt()
        set(v) { y = v.toDouble() }

    var headingDir: Dir
        get() = Dir.fromAngle(heading)
        set(d) { heading = d.heading() }

    fun step(len: Double) {
        val angRad = heading * PI  / 180.0
        val dx = Math.cos(angRad) * len
        val dy = Math.sin(angRad) * len
        x += dx
        y += dy
    }

    fun rotate(amount: Double) {
        heading = Dir.normalizeAngle(heading + amount)
    }

    fun headUp() {
        if (pitch < 50.0) { pitch += 10.0 }
    }
    fun headDown() {
        if (pitch > -50.0) { pitch -= 10.0 }
    }
}



// Reprezentacja pojedynczej "rozgrywki". Posiada planszę labiryntu
// (maze) oraz gracza poruszającego się poń.
data class Game(val maze: Maze, val player: Player = Player(0.0, 0.0, Dir.S.heading())) {
    // Resetuje położenie gracza na planszy.
    fun reset() {
        player.x = 0.0
        player.y = 0.0
    }

    // Próbuje ruszyć gracza w danym kierunku. Jeżeli nie uda się (brak wyjścia lub
    // mało prawdopodobny brak komórki na planszy) zwraca false.
    // W przeciwnym przypadku zwraca true, a pozycja gracza jest aktualizowana.
    fun tryMove(d: Dir, step: Double = 1.0): Boolean {
        val c = maze.getCell(player.xInt, player.yInt)
        return if (c == null) {
            false
        } else {
            if (d in c.exits) {
                val (nx, ny) = d.move(player.xInt, player.yInt)
                player.xInt = nx
                player.yInt = ny
                true
            } else {
                false
            }
        }
    }

}