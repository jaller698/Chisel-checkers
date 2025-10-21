import chisel3._
import chisel3.util._

class ChiselCheckers() extends Module {
  val io = IO(new Bundle {

    val mode = Input(UInt(2.W))
    /* I have three modes:
      BUILDBOARD (00)
      PLAYMOVE   (01)
      VIEWBOARD  (10)
     */

    // used for BUILDBOARD
    val reset = Input(Bool())
    val resetEmpty = Input(Bool())
    val placePiece = Input(UInt(5.W)) // Position
    val colorToPut = Input(Bool()) // 0 is black, one is white.
    // if set to true we set an empty board.
    // used for PLAYMOVE:
    val from = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val to = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val isMoveValid = Output(Bool())
    // used for viewboard:
    // we also use FROM mentioned in PLAYBOARD.
    val colorAtTile = Output(
      UInt(3.W)
    ) // sends out one from the enum below, which is one of sEmpty, sWhite, etc.

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

      val from = io.from
      val to = io.to
      val piece = board(from)

      switch(piece) {
        is(sEmpty) {
          io.isMoveValid := false.B
        }
        is(sWhite) {
          val fromCol = io.from % 4.U
          val toCol = io.to % 4.U
          when(
            (io.to === io.from - 4.U || io.to === io.from - 5.U) &&
              !(fromCol === 0.U && toCol === 3.U) &&
              !(fromCol === 3.U && toCol === 0.U)
          ) {
            io.isMoveValid := true.B
          }.otherwise {
            io.isMoveValid := false.B
          }

        }
        is(sBlack) {
          when(io.to === io.from + 4.U || io.to === io.from + 3.U) { // 90% sure this is wrong, havent fixed.
            io.isMoveValid := true.B
          }.otherwise {
            io.isMoveValid := false.B
          }
        }
        is(sWhiteKing, sBlackKing) {
          when(
            (to === from - 4.U || to === from - 5.U || to === from + 4.U || to === from + 3.U) &&
              ((to % 4.U =/= 0.U || from % 4.U =/= 3.U) && (to % 4.U =/= 3.U || from % 4.U =/= 0.U))
          ) {
            io.isMoveValid := true.B
          }.otherwise {
            io.isMoveValid := false.B
          }
        }
      }

    }
    is("b10".U) { // viewboard.

      io.colorAtTile := board(io.from)

    }
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
