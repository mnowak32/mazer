package pl.cdbr.mazer.model

import kotlin.math.*

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
    fun tryMove(step: Double = 0.1): Boolean {
        val c = maze.getCell(player.xInt, player.yInt)
        return if (c == null) {
            false
        } else {
            val angRad = player.heading * PI  / 180.0
            val dx = step * cos(angRad)
            val dy = step * sin(angRad)
            val (tx, ty) = (player.x + dx) to (player.y + dy)
            if (isFloorAt(tx, ty)) {
                player.x = tx
                player.y = ty
                true
            } else {
                false
            }
        }
    }

    fun isFloorAt(x: Double, y: Double): Boolean {
        // każda komórka wygląda tak:
        // +--------+
        // |##::::##|
        // |::    ::|
        // |::    ::|
        // |##::::##|
        // +--------+
        // części "#" są zawsze zajęte
        // części " " są zawsze wolne
        // części ":" zależą od wyjść w danej komórce
        // środek ma współrzędne (x, y)
        // komórka rozciąga się w zakresie (x-0.5 .. x+0.5), (y-0.5 .. y+0.5)
        val c = maze.getCell(x.toInt(), y.toInt()) ?: return false

        //bierzemy częsci ułamkowe
        val (fx, fy) = (x % 1) to (y % 1)
        //3 przypadki:
        // 1. fx, fy pokazują środek komórki - zawsze wolne
        // 2. fx, fy pokazują narożniki komórki - zawsze zajęte
        // 3. else - trzeba sprawdzić wyjście
        val (fax, fay) = abs(fx) to abs(fy)
        return if (fax < cellDivider && fay < cellDivider) {
            // przypadek 1
            true
        } else {
            if (fax >= cellDivider && fay >= cellDivider) {
                // przypadek 2
                false
            } else {
                val dir = when {
                    fx > cellDivider -> Dir.E
                    fx < -cellDivider -> Dir.W
                    fy > cellDivider -> Dir.N
                    else -> Dir.S
                }
                (dir in c.exits)
            }
        }
    }

    companion object {
        // trzeba zachować jakiś odstęp od ściany, żeby nie "patrzeć" przez nie.
        val minWallDist = Config.viewPortDistance * 2
        val cellDivider = 0.25 + minWallDist
    }
}