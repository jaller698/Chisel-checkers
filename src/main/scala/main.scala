import chisel3._
import chisel3.util._

class ChiselCheckers() extends Module {
  val io = IO(new Bundle {

    // Testing IO
    val resetEmpty = Input(Bool())
    val placePiece = Input(UInt(5.W)) // Position
    val colorToPut = Input(Bool()) // 0 is black, one is white.
    // End Testing IO

    val mode = Input(UInt(2.W)) // Mode selector as UInt
    /* I have three modes:
      BUILDBOARD (00)
      PLAYMOVE   (01)
      VIEWBOARD  (10)
     */

    val reset = Input(Bool())
    val from = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val to = Input(UInt(5.W)) // A numbered place on the board (default 0-31)
    val isMoveValid = Output(Bool())
    val colorAtTile =
      Output(UInt(3.W)) // The color at a given tile, used for VIEWBOARD mode
    val valid = Output(Bool()) // Output is ready to be read
    val ack = Input(Bool()) // Consumer acknowledges output

  })

  // Board tile states
  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
  // Mode selector states
  val sBuild :: sPlay :: sView :: Nil = Enum(3)
  val board_size = 32

  val board = RegInit(VecInit(Seq.tabulate(board_size) { i =>
    if (i < 12) sBlack
    else if (i >= 20) sWhite
    else sEmpty
  }))

  // Default assignments (hardware best practice)
  // Handshake registers
  val validReg = RegInit(false.B)
  val isMoveValidReg = RegInit(false.B)
  val colorAtTileReg = RegInit(sEmpty)

  io.isMoveValid := isMoveValidReg
  io.colorAtTile := colorAtTileReg
  io.valid := validReg

  // Ensure mode input is within valid range
  require(io.mode.getWidth == 2, "io.mode must be 2 bits wide")
  // Only allow valid mode values (0, 1, 2)
  assert(io.mode <= 2.U, "io.mode must be 0, 1, or 2")

  // Use enum for mode selection
  // Handshake logic: only update outputs when not waiting for ack
  switch(io.mode) {
    is(sBuild) {
      when(!validReg) {
        when(io.reset) {
          when(!io.resetEmpty) {
            board := VecInit(Seq.tabulate(board_size) { i =>
              if (i < 12) sBlack
              else if (i >= 20) sWhite
              else sEmpty
            })
          }.otherwise {
            board := VecInit(Seq.fill(board_size)(sEmpty))
          }
          isMoveValidReg := false.B
          colorAtTileReg := sEmpty
          validReg := true.B
        }.otherwise {
          board(io.placePiece) := Mux(io.colorToPut, sWhite, sBlack)
          isMoveValidReg := false.B
          colorAtTileReg := sEmpty
          validReg := true.B
        }
      }
    }
    is(sPlay) {
      val moveValidator = Module(new MoveValidator())
      moveValidator.io.board := board
      moveValidator.io.from := io.from
      moveValidator.io.to := io.to
      moveValidator.io.color := 0.U
      // I've set 0 to be black. Probably a bad decision hehe

      when(!validReg) {
        isMoveValidReg := moveValidator.io.ValidMove
        colorAtTileReg := sEmpty
        validReg := true.B
        when(moveValidator.io.ValidMove) {
          board := moveValidator.io.newboard
        }
      }
    }
    is(sView) {
      when(!validReg) {
        colorAtTileReg := board(io.from)
        isMoveValidReg := false.B
        validReg := true.B
      }
    }
  }

  // Clear validReg when ack is received
  when(validReg && io.ack) {
    validReg := false.B
  }
}

object ChiselCheckers extends App {
  println("Generating the Chisel Checkers hardware")
  emitVerilog(new ChiselCheckers())
}
