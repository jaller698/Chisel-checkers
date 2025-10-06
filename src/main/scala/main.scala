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

  //val ne = sWhiteKing
  //printf(cf"$ne")

  val board = RegInit(VecInit(Seq.tabulate(board_size) { i =>
    if (i < 12) sBlack
    else if (i >= 20) sWhite
    else sEmpty
  }))

  io.isMoveValid := false.B

  // val move= Module(new Mover())
  // move.io.boardread:=board
  // move.io.to:=io.to
  // move.io.from:=io.from
  // board:=move.io.boardwrite


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

//Note this class does not consider if the move is legal or not, that needs to be checked beforehand
class Mover extends Module {
  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
  val io = IO(new Bundle {
    //val boardread = Input(Vec(32, UInt(4.W))) // The current boardstate
    val from = Input(UInt(20.W)) // A numbered place on the board (default 0-31)
    val to = Input(UInt(20.W)) // A numbered place on the board (default 0-31)
    val boardwrite = Output(Vec(32, UInt(4.W))) //The boardstate we return after the move
  })


  val boardread = RegInit(VecInit(Seq.tabulate(32) { i =>
    if (i < 12) sBlack
    else if (i >= 20) sWhite
    else sEmpty
  }))
  
  val tmp1=boardread
  
  //val tmp1=io.boardread
  //printf(cf"$tmp1")
  tmp1(io.to):=tmp1(io.from)
  tmp1(io.from):=sEmpty

  io.boardwrite:=tmp1

}

object ChiselCheckers extends App {
  println("Generating the Chisel Checkers hardware")
  emitVerilog(new ChiselCheckers(16))
}
