package pl.cdbr.mazer.model

data class Player(var x: Int, var y: Int, var heading: Dir)

data class Game(val maze: Maze, val player: Player = Player(0, 0, Dir.S)) {
    fun reset() {
        player.x = 0
        player.y = 0
    }

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