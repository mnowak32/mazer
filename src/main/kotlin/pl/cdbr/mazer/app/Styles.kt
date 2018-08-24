package pl.cdbr.mazer.app

import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import tornadofx.*

class Styles : Stylesheet() {
    companion object {
        val maze by cssclass()
    }

    init {
        maze {
            borderStyle += BorderStrokeStyle.SOLID
            borderWidth += box(1.px)
            borderColor += box(Color.DARKGREEN)
        }
    }
}