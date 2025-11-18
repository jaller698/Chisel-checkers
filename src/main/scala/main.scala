import chisel3._
import chisel3.util._

class ChiselCheckers extends Module {
  val io = IO(new Bundle {

    // Testing IO
    val resetEmpty = Input(Bool())
    val placePiece = Input(UInt(5.W)) // Position
    val colorToPut = Input(Bool()) // 0 is black, one is white.
    // End Testing IO

    val op = Input(UInt(2.W)) // Requested operation

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
  // Op selector states
  val sBuild :: sPlay :: sView :: Nil = Enum(3)
  // State machine states
  val sIdle :: sProcessing :: sOutput :: Nil = Enum(3)
  val board_size = 32

  val board = RegInit(VecInit(Seq.tabulate(board_size) { i =>
    if (i < 12) sBlack
    else if (i >= 20) sWhite
    else sEmpty
  }))

  // Default assignments (hardware best practice)
  // Handshake registers
  val isMoveValidReg = RegInit(false.B)
  val colorAtTileReg = RegInit(sEmpty)
  val stateReg = RegInit(sIdle)

  io.isMoveValid := isMoveValidReg
  io.colorAtTile := colorAtTileReg
  io.valid := false.B

  // State machine implementation
  switch(stateReg) {
    is(sIdle) {
      io.valid := false.B
      when(io.op === sBuild || io.op === sPlay || io.op === sView) {
        stateReg := sProcessing
      }
    }
    is(sProcessing) {
      switch(io.op) {
        is(sBuild) {
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
          }.otherwise {
            board(io.placePiece) := Mux(io.colorToPut, sWhite, sBlack)
            isMoveValidReg := false.B
            colorAtTileReg := sEmpty
          }
        }
        is(sPlay) {
          val moveValidator = Module(new MoveValidator())
          moveValidator.io.board := board
          moveValidator.io.from := io.from
          moveValidator.io.to := io.to
          // TODO: This should not be a input. It should be derived from the piece at 'from' in the component.
          moveValidator.io.color := Mux(
            board(io.from) === sBlack || board(io.from) === sBlackKing,
            0.U,
            1.U
          )
          isMoveValidReg := moveValidator.io.ValidMove
          colorAtTileReg := sEmpty
          when(moveValidator.io.ValidMove) {
            board := moveValidator.io.newboard
          }
        }
        is(sView) {
          colorAtTileReg := board(io.from)
          isMoveValidReg := false.B
        }
      }
      stateReg := sOutput
    }
    is(sOutput) {
      io.valid := true.B
      when(io.ack) {
        io.valid := false.B
        stateReg := sIdle
        isMoveValidReg := false.B
      }
    }
  }
}

object ChiselCheckers extends App {
  println("Generating the Chisel Checkers hardware")
  emitVerilog(new ChiselCheckers())
}
