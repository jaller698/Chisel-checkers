import chisel3._
import chisel3.util._

//This component just adds the scores for all the pieces on the board.
//I am guessing that a king equals 3 pawns.

class BoardEval1() extends Module {
  val io = IO(new Bundle {
    val In = Input(Vec(32, UInt(3.W))) // This is the most basic version.

    val score = Output(SInt(8.W)) // idk how big is should be yet.

  })

  val v = Wire(Vec(32, SInt(3.W)))
  // It is covering the range from -3 to 3, which feels like it is 3 bits.
  for (i <- 0 to 31) {
    v(i) := 0.S
  }

  for (i <- 0 to 31) {
    switch(io.In(i)) {
      is("b000".U) { v(i) := 0.S }
      is("b001".U) { v(i) := 1.S }
      is("b010".U) { v(i) := 3.S }
      is("b011".U) { v(i) := -1.S }
      is("b100".U) { v(i) := -3.S }
      // default { v(i) := 0.S }

    }
  }

  // We just do a treeReduce where we have a function in it.
  io.score := v.reduceTree((x, y) => x + y)

}
