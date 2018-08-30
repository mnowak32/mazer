package pl.cdbr.mazer.model

import javafx.scene.paint.Color

object Config {
    const val mazeX = 20
    const val mazeY = 16
    const val tileSize = 32

    const val screenX = 640
    const val screenY = 480

    const val mazeHeight = 0.6
    const val headHeight = 0.35 //wartość bezwzględna, lepiej żeby była mniejsza niż mazeHeight :)
    const val viewPortWidth = 0.02
    const val viewPortHeight = 0.015
    const val viewPortDistance = 0.005


    val ceilColor: Color = Color.DARKOLIVEGREEN
    val floorColor: Color = Color.DARKGREY
    val wallColor: Color = Color.DARKSLATEGREY
}