import chisel3._
import chisel3.util._

class MoveValidator extends Module {
  val io = IO(new Bundle {
    val from = Input(UInt(5.W))
    val to = Input(UInt(5.W))
    val piece = Input(UInt(4.W))
    val isMoveValid = Output(Bool())
  })
  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
  io.isMoveValid := false.B

  switch(io.piece) {
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
        (io.to === io.from - 4.U || io.to === io.from - 5.U || io.to === io.from + 4.U || io.to === io.from + 3.U) &&
          ((io.to % 4.U =/= 0.U || io.from % 4.U =/= 3.U) && (io.to % 4.U =/= 3.U || io.from % 4.U =/= 0.U))
      ) {
        io.isMoveValid := true.B
      }.otherwise {
        io.isMoveValid := false.B
      }
    }
  }
}
