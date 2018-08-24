package pl.cdbr.mazer.view

import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import pl.cdbr.mazer.app.Config
import pl.cdbr.mazer.app.Styles
import pl.cdbr.mazer.model.*
import tornadofx.*

class MainView : View("aMaaaaze!") {
    private var ctx: GraphicsContext by singleAssign()
    private var cnv: Canvas by singleAssign()
    private val notGenerated = SimpleBooleanProperty(true)
    private var game: Game? = null

    override val root = vbox {
        hbox {
            button("Generate!") {
                action {
                    drawMaze()
                }
            }
            spacer { }
            button("Start") {
                disableWhen { notGenerated }
                action {
                    startWalking()
                }
            }
        }
        cnv = canvas(width = canvX, height = canvY) {
            addClass(Styles.maze)
            ctx = graphicsContext2D
        }
    }.apply {
        addEventHandler(KeyEvent.KEY_PRESSED, ::kpHandler)
    }

    fun startWalking() {
        val g = game ?: return
        g.reset()
        showPlayer(g.player)
        game = g
    }

    fun kpHandler(ev: KeyEvent) {
        val g = game ?: return
        val dir = when (ev.code) {
            KeyCode.UP -> Dir.N
            KeyCode.DOWN -> Dir.S
            KeyCode.LEFT -> Dir.W
            KeyCode.RIGHT -> Dir.E
            else -> null
        }
        if (dir != null) {
            showPlayer(g.player, clear = true)
            val moved = g.tryMove(dir)
            if (!moved) {
                //pokaż efekt uderzania o ścianę...
            }
            showPlayer(g.player)
            game = g
        }
    }

    fun drawMaze() {
        val maze = Maze(Config.mazeX, Config.mazeY)
        MazeGen(maze).generate()
        game = Game(maze)
//        maze.print()

        ctx.fill = Color.WHITESMOKE
        ctx.fillRect(0.0, 0.0, cnv.width, cnv.height)

        ctx.fill = Color.DARKRED
        (0 .. Config.mazeY).forEach { y ->
            (0 .. Config.mazeX).forEach { x ->
                val c = maze.getCell(x, y)
                if (c != null) {
                    val startX = x * 4
                    val startY = y * 4
                    if (Dir.N in c.exits) {
                        intRect(startX, startY, 1, 1)
                        intRect(startX + 3, startY, 1, 1)
                    } else {
                        intRect(startX, startY, 4, 1)
                    }
                    if (Dir.W !in c.exits) {
                        intRect(startX, startY + 1, 1, 2)
                    }
                    if (Dir.E !in c.exits) {
                        intRect(startX + 3, startY + 1, 1, 2)
                    }
                    if (Dir.S in c.exits) {
                        intRect(startX, startY + 3, 1, 1)
                        intRect(startX + 3, startY + 3, 1, 1)
                    } else {
                        intRect(startX, startY + 3, 4, 1)
                    }
                }
            }
        }
        ctx.fill()
        ctx.beginPath()
        ctx.fill = Color.LIGHTPINK
        intRect(1, 1, 2, 2)
        ctx.fill()
        ctx.beginPath()
        ctx.fill = Color.LIGHTGREEN
        intRect(Config.mazeX * 4 - 3, Config.mazeY * 4 - 3, 2, 2)
        ctx.fill()

        notGenerated.value = false
    }

    private fun intRect(x0: Int, y0: Int, xd: Int, yd: Int) {
        val cx0 = x0 * sqSize
        val cy0 = y0 * sqSize
        val cx1 = xd * sqSize
        val cy1 = yd * sqSize
        ctx.rect(cx0, cy0, cx1, cy1)
    }

    private fun showPlayer(p: Player, clear: Boolean = false) = showPlayer(p.x, p.y, clear)

    private fun showPlayer(x: Int, y: Int, clear: Boolean = false) {
        ctx.fill = if (clear) Color.WHITESMOKE else Color.ORANGE
        ctx.fillOval((x + playerOffsetRatio) * Config.tileSize, (y + playerOffsetRatio) * Config.tileSize,
                playerSize, playerSize)
    }


    companion object {
        val canvX = Config.mazeX * Config.tileSize * 1.0
        val canvY = Config.mazeY * Config.tileSize * 1.0
        val sqSize = Config.tileSize / 4.0
        val playerSizeRatio = 0.4
        val playerSize = playerSizeRatio * Config.tileSize
        val playerOffsetRatio = (1 - playerSizeRatio) / 2
    }
}