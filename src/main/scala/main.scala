import chisel3._
import chisel3.util._

class ChiselCheckers(n: Int) extends Module {
  val io = IO(new Bundle {
    val from = Input(UInt(n.W)) // A numbered place on the board (default 0-31)
    val to = Input(UInt(n.W)) // A numbered place on the board (default 0-31)
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

  printBoard()

  io.isMoveValid := false.B

  when(io.reset) {
    board := VecInit(Seq.tabulate(board_size) { i =>
      if (i < 12) sBlack
      else if (i >= 20) sWhite
      else sEmpty
    })
  }
  

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
      when(io.to === io.from + 4.U || io.to === io.from + 3.U) {
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

object ChiselCheckers extends App {
  println("Generating the Chisel Checkers hardware")
  emitVerilog(new ChiselCheckers(16))
}

