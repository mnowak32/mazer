package pl.cdbr.mazer.view

import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyEvent
import pl.cdbr.mazer.model.Config
import tornadofx.*

class RayCastView : View("3D maze") {
    private var ctx: GraphicsContext by singleAssign()

    override val root = borderpane {
        canvas(width = canvX, height = canvY) {
            ctx = graphicsContext2D
        }
    }.apply {
        addEventHandler(KeyEvent.KEY_PRESSED, ::kpHandler)
    }

    private fun kpHandler(ev: KeyEvent) {
    }

    companion object {
        val canvX = Config.screenX.toDouble()
        val canvY = Config.screenY.toDouble()
    }
}
