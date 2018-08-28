package pl.cdbr.mazer.model.event

import pl.cdbr.mazer.model.Player
import tornadofx.*

data class PlayerUpdate(val p: Player): FXEvent()