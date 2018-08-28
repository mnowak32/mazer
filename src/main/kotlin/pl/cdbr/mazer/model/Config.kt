package pl.cdbr.mazer.model

import javafx.scene.paint.Color

object Config {
    const val mazeX = 20
    const val mazeY = 16
    const val tileSize = 32

    const val screenX = 320
    const val screenY = 240

    const val mazeHeight = 1.0
    const val viewPortWidth = 0.01
    const val viewPortHeight = 0.075
    const val viewPortDistance = 0.01


    val ceilColor: Color = Color.DARKOLIVEGREEN
    val floorColor: Color = Color.DARKGREY
    val wallColor: Color = Color.DARKSLATEGREY
}