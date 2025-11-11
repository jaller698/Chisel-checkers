import chisel3._
import chisel3.util._

class ChiselCheckers() extends Module {
  val io = IO(new Bundle {

    // Testing IO
    val resetEmpty = Input(Bool())
    val placePiece = Input(UInt(5.W)) // Position
    val colorToPut = Input(Bool()) // 0 is black, one is white.
    // End Testing IO

    val mode = Input(UInt(2.W))
    /* I have three modes:
      BUILDBOARD (00)
      PLAYMOVE   (01)
      VIEWBOARD  (10)
     */

    val reset = Input(Bool())
    val from = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val to = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val isMoveValid = Output(Bool())
    val ready = Output(Bool()) // player can read isMoveValid

    val colorAtTile = Output(
      UInt(3.W) // The color at a given tile, used for VIEWBOARD mode
    )
  })

  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
  val board_size = 32

  val board = RegInit(VecInit(Seq.tabulate(board_size) { i =>
    if (i < 12) sBlack
    else if (i >= 20) sWhite
    else sEmpty
  }))

  io.isMoveValid := false.B
  io.colorAtTile := 0.U // just for init, idk if good yet
  io.ready := false.B

  switch(io.mode) {
    is("b00".U) { // buildboard.
      when(io.reset) { // we are making a new piece, which may be either empty or standard position.
        when(!io.resetEmpty) {
          board := VecInit(Seq.tabulate(board_size) { i =>
            if (i < 12) sBlack
            else if (i >= 20) sWhite
            else sEmpty
          })
        }.otherwise {
          board := VecInit(Seq.tabulate(board_size) { i =>
            sEmpty
          })
        }
      }.otherwise { // We are trying to place a piece.
        when(io.colorToPut) {
          board(io.placePiece) := sWhite
        }.otherwise {
          board(io.placePiece) := sBlack
        }

      }

    }

    is("b01".U) { // PLAYBOARD

      // Use the black move validator
      val moveValidator = Module(new MoveValidator())
      moveValidator.io.board := board
      moveValidator.io.from := io.from
      moveValidator.io.to := io.to

      io.isMoveValid := moveValidator.io.ValidMove

      when(moveValidator.io.ValidMove) {
        // update the register with the validator's new board
        board := moveValidator.io.newboard
      }
    }
    is("b10".U) { // viewboard.

      io.colorAtTile := board(io.from)

    }
  }

}

object ChiselCheckers extends App {
  println("Generating the Chisel Checkers hardware")
  emitVerilog(new ChiselCheckers())
}
