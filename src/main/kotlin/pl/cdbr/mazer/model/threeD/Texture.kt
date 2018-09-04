package pl.cdbr.mazer.model.threeD

import javafx.scene.image.Image
import javafx.scene.paint.Color

data class Texture(val img: Image) {
    private val xs = img.width
    private val ys = img.height
    private val pixRead = img.pixelReader

    // współrzędne w zakresie 0..1 (powyżej 1 zawija się)
    fun colorAt(x: Double, y: Double): Color {
        val ix = ((xs * x) % xs).toInt()
        val iy = ((ys * y) % ys).toInt()
        return pixRead.getColor(ix, iy)
    }

    companion object {
        val wall = Texture(Image("texture/wall_512.jpg"))
        val ceil = Texture(Image("texture/ceil_512.jpg"))
        val floor = Texture(Image("texture/floor_512.jpg"))
    }
}