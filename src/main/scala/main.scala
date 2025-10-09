import chisel3._
import chisel3.util._
import os.move

class ChiselCheckers() extends Module {
  val io = IO(new Bundle {
    val from = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val to = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val reset = Input(Bool())
    val isMoveValid = Output(Bool())
  })
  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
  val board_size = 32

  val board = RegInit(VecInit(Seq.tabulate(board_size) { i =>
    if (i < 12) sBlack
    else if (i >= 20) sWhite
    else sEmpty
  }))

  val moveValidator = Module(new MoveValidator())
  moveValidator.io.from := io.from
  moveValidator.io.to := io.to
  moveValidator.io.piece := board(io.from)

  io.isMoveValid := moveValidator.io.isMoveValid

  when(io.reset) {
    board := VecInit(Seq.tabulate(board_size) { i =>
      if (i < 12) sBlack
      else if (i >= 20) sWhite
      else sEmpty
    })
  }
}

//Note this class does not consider if the move is legal or not, that needs to be checked beforehand
class Mover extends Module {
  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
  val io = IO(new Bundle {
    val boardread = Input(Vec(32, UInt(4.W))) // The current boardstate
    val from = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val to = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val boardwrite =
      Output(Vec(32, UInt(4.W))) // The boardstate we return after the move
  })

  // Implement check for valid move.

  io.boardwrite := io.boardread
  io.boardwrite(io.to) := io.boardread(io.from)
  io.boardwrite(io.from) := sEmpty

}

object ChiselCheckers extends App {
  println("Generating the Chisel Checkers hardware")
  emitVerilog(new ChiselCheckers())
}
