package pl.cdbr.mazer.model.event

import pl.cdbr.mazer.model.Maze
import tornadofx.*

data class MazeChanged(val m: Maze): FXEvent()