package pl.cdbr.mazer.model

// Klasa reprezentująca położenie gracza w labiryncie (x i y) oraz
// kierunek, w którym jest zwrócony (heading, na razie nieużywane).
data class Player(var x: Int, var y: Int, var heading: Dir)

// Reprezentacja pojedynczej "rozgrywki". Posiada planszę labiryntu
// (maze) oraz gracza poruszającego się poń.
data class Game(val maze: Maze, val player: Player = Player(0, 0, Dir.S)) {
    // Resetuje położenie gracza na planszy.
    fun reset() {
        player.x = 0
        player.y = 0
    }

    // Próbuje ruszyć gracza w danym kierunku. Jeżeli nie uda się (brak wyjścia lub
    // mało prawdopodobny brak komórki na planszy) zwraca false.
    // W przeciwnym przypadku zwraca true, a pozycja gracza jest aktualizowana.
    fun tryMove(d: Dir): Boolean {
        val c = maze.getCell(player.x, player.y)
        return if (c == null) {
            false
        } else {
            if (d in c.exits) {
                val (nx, ny) = d.move(player.x, player.y)
                player.x = nx
                player.y = ny
                player.heading = d
                true
            } else {
                false
            }
        }
    }

}