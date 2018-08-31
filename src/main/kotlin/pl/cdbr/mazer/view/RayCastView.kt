package pl.cdbr.mazer.view

import javafx.scene.image.WritableImage
import pl.cdbr.mazer.model.Config
import pl.cdbr.mazer.model.event.ExportDxf
import pl.cdbr.mazer.model.event.MazeChanged
import pl.cdbr.mazer.model.event.PlayerUpdate
import pl.cdbr.mazer.model.threeD.Maze3d
import pl.cdbr.mazer.model.threeD.Scene
import tornadofx.*

class RayCastView : View("3D maze") {
    private var maze: Maze3d? = null
    private val img = WritableImage(Config.screenX, Config.screenY)
//    private var ctx: GraphicsContext by singleAssign()

    override val root = vbox {
        imageview(img)
//        canvas(width = Config.screenX.toDouble(), height = Config.screenY.toDouble()) {
//            ctx = graphicsContext2D
//        }
    }

    init {
        subscribe<MazeChanged> { mc ->
            println("Maze changed")
            maze = Maze3d(mc.m, Config.mazeHeight)
//            drawMaze()
        }
        subscribe<PlayerUpdate> { pu ->
            val m = maze ?: return@subscribe
            val scene = Scene(pu.p, m)
            scene.drawOnto(img.pixelWriter)
        }
        subscribe<ExportDxf> {
            maze?.exportStl()
        }
    }

//    private fun drawMaze() {
//        val m = maze ?: return
//        ctx.fill = Color.WHITE
//        val maxY = ctx.canvas.height
//        val scale = 20.0
//        val offs = 10.0
//        ctx.fillRect(0.0, 0.0, ctx.canvas.width, maxY)
//        ctx.stroke = Config.wallColor
//        ctx.lineWidth = 1.0
//        ctx.fill = Config.floorColor
//
//        val faces = (0 .. 8).flatMap { y ->
//            (0 .. 8).flatMap { x ->
//                m.getFaces(x, y)
//            }
//        }
//
//        fun fillRect2(x: Double, y: Double, w: Double, h: Double) {
//            val tx = if (w < 0) { x + w } else { x }
//            val ty = if (h < 0) { y + h } else { y }
//            val tw = if (w < 0) { -w } else { w }
//            val th = if (h < 0) { -h } else { h }
//            ctx.fillRect(tx, ty, tw, th)
//        }
//
//        faces
//            .filter { it.color == Config.floorColor }
//            .forEach { f ->
//                val r = f.r
//                fillRect2(r.p1.x * scale + offs, maxY - (r.p3.y * scale) - offs, r.v1.dx * scale, r.v2.dy * scale)
//            }
//
//        faces
//            .filter { it.color == Config.wallColor }
//            .forEach { f ->
//                val r = f.r
//                ctx.strokeLine(r.p1.x * scale + offs, maxY - (r.p1.y * scale) - offs, r.p2.x * scale + offs, maxY - (r.p2.y * scale) - offs)
//            }
//    }
}
