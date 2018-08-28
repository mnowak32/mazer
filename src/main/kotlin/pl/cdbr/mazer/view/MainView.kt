package pl.cdbr.mazer.view

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import pl.cdbr.mazer.app.Styles
import pl.cdbr.mazer.model.*
import pl.cdbr.mazer.model.event.ExportDxf
import pl.cdbr.mazer.model.event.MazeChanged
import pl.cdbr.mazer.model.event.PlayerUpdate
import pl.cdbr.mazer.model.threeD.Vector
import tornadofx.*

class MainView : View("aMaaaaze!") {
    private var ctx: GraphicsContext by singleAssign()
    private var cnv: Canvas by singleAssign()
    private val canStart = SimpleBooleanProperty(false)
    private val started = SimpleBooleanProperty(false)
    private var game: Game? = null
    private val playerX = SimpleStringProperty("")
    private val playerY = SimpleStringProperty("")
    private val playerH = SimpleStringProperty("")

    override val root = vbox(spacing = 5) {
        hbox(spacing = 5) {
            button("Generate!") {
                action {
                    drawMaze()
                }
            }
            spacer { }
            button("Start") {
                enableWhen { canStart }
                action {
                    startWalking()
                }
            }
            button("Reset") {
                enableWhen { started }
                action {
                    resetGame()
                }
            }
            button("Go 3D!") {
                enableWhen { started }
                action {
                    go3d()
                }
            }
            spacer { }
            button("Exit") {
                action {
                    Platform.exit()
                }
            }
        }

        cnv = canvas(width = canvX, height = canvY) {
            addClass(Styles.maze)
            ctx = graphicsContext2D
        }
        hbox {
            text(playerX)
            spacer()
            text(playerY)
            spacer()
            text(playerH)
            spacer()
            button("Write DXF") {
                action {
                    fire(ExportDxf())
                }
            }
        }
    }.apply {
        addEventHandler(KeyEvent.KEY_PRESSED, ::kpHandler)
    }

    init {
        drawMaze()
        startWalking()
        go3d()
    }

    private fun go3d() {
        find<RayCastView>().openWindow()
        val g = game ?: return
        fire(MazeChanged(g.maze))
    }

    private fun resetGame() {
        val g = game ?: return
        showPlayer(g.player, clear = true)
        g.reset()
        showPlayer(g.player)
        fire(PlayerUpdate(g.player))
        game = g
    }

    private fun startWalking() {
        val g = game ?: return
        canStart.value = false
        started.value = true
        showPlayer(g.player)
        fire(PlayerUpdate(g.player))
        game = g
    }

    private fun kpHandler(ev: KeyEvent) {
        val g = game ?: return
        if (!started.value) {
            return
        }
        if (ev.code in consumedKeys) {
            ev.consume()
            showPlayer(g.player, clear = true)
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (ev.code) {
                KeyCode.LEFT -> g.player.rotate(90.0)
                KeyCode.RIGHT -> g.player.rotate(-90.0)
                KeyCode.UP -> {
                    val dir = g.player.headingDir
                    g.tryMove(dir)
                }
                KeyCode.DOWN -> {
                    val dir = g.player.headingDir.reverse()
                    g.tryMove(dir)
                }
            }
            showPlayer(g.player)
            playerX.value = g.player.x.toString()
            playerY.value = g.player.y.toString()
            playerH.value = g.player.heading.toString()

            fire(PlayerUpdate(g.player))
            game = g
        }
    }

    private fun drawMaze() {
        val maze = Maze(Config.mazeX, Config.mazeY)
        MazeGen(maze).generate()
        game = Game(maze)
//        maze.print()

        ctx.fill = Color.WHITESMOKE
        ctx.fillRect(0.0, 0.0, cnv.width, cnv.height)

        ctx.fill = Color.DARKRED
        (0 until Config.mazeY).forEach { y ->
            (0 until Config.mazeX).forEach { x ->
                val c = maze.getCell(x, y)
                if (c != null) {
                    val startX = x * 4
                    val startY = y * 4
                    if (Dir.S in c.exits) {
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
                    if (Dir.N in c.exits) {
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

        canStart.value = true
        started.value = false
        fire(MazeChanged(maze))
    }

    private fun intRect(x0: Int, y0: Int, xd: Int, yd: Int) {
        val cx0 = x0 * sqSize
        val cy0 = canvY - y0 * sqSize
        val cx1 = xd * sqSize
        val cy1 = -yd * sqSize
        ctx.rect(cx0, cy0, cx1, cy1)
    }

    private fun showPlayer(p: Player, clear: Boolean = false) {
        showPlayer(p.x, p.y, p.heading, clear)
    }

    private fun showPlayer(x: Double, y: Double, hd: Double, clear: Boolean = false) {
        ctx.fill = if (clear) Color.WHITESMOKE else Color.ORANGE
        val pos = Vector(x + 0.5, y + 0.5, 0.0)
        val tri = listOf(
                Vector(0.25, 0.0, 0.0),
                Vector(-0.15, 0.15, 0.0),
                Vector(-0.15, -0.15, 0.0)
        ).map { it.rotateZ(hd).toPoint() + pos }
        val xArr = tri.map { it.x * Config.tileSize }.toDoubleArray()
        val yArr = tri.map { canvY - it.y * Config.tileSize }.toDoubleArray()
        ctx.fillPolygon(xArr, yArr, 3)
    }

    companion object {
        val canvX = Config.mazeX * Config.tileSize * 1.0
        val canvY = Config.mazeY * Config.tileSize * 1.0
        val sqSize = Config.tileSize / 4.0

        val consumedKeys = setOf(KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT)
    }
}