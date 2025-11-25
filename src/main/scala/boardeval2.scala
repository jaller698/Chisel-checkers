import chisel3._
import chisel3.util._

// This component evaluates the pieces on the board while also giving rows position an additional score
// This way , lets say a given scenario where the current boardeval is equal but black (or white) has a better score because of the rows position.
// Furthermore, the boardeval2 will be able to make a decision based on the rows position.

class boardeval2() extends Module {
  val io = IO(new Bundle {
    val In = Input(Vec(32, UInt(3.W)))
    val color = Input(UInt(1.W))

    val score = Output(SInt(8.W))
  })

  // Need more bits to store the positional score, 5 bits should be enough.
  val v = Wire(Vec(32, SInt(5.W)))

  for (i <- 0 to 31) {
    v(i) := 0.S
  }

  for (i <- 0 to 31) {
    val row_pos = i.U / 4.U // Calculate row as hardware value
    val material_score = WireDefault(0.S(5.W))
    val position_score = WireDefault(0.S(5.W))

    // Obtain material score as in boardeval1.
    switch(io.In(i)) {
      is("b000".U) { material_score := 0.S }
      is("b001".U) { material_score := 1.S }
      is("b010".U) { material_score := 3.S }
      is("b011".U) { material_score := -1.S }
      is("b100".U) { material_score := -3.S }
    }

    when(io.In(i) === "b001".U) { // white pawn
      // row 0 = +3, row 1 = +2, row 2 = +1, row 3+ = 0
      when(row_pos === 0.U) {
        position_score := 3.S
      }.elsewhen(row_pos === 1.U) {
        position_score := 2.S
      }.elsewhen(row_pos === 2.U) {
        position_score := 1.S
      }.otherwise {
        position_score := 0.S
      }
    }.elsewhen(io.In(i) === "b011".U) { // black pawn
      // row 7 = -3, row 6 = -2, row 5 = -1, row 4- = 0
      when(row_pos === 7.U) {
        position_score := -3.S
      }.elsewhen(row_pos === 6.U) {
        position_score := -2.S
      }.elsewhen(row_pos === 5.U) {
        position_score := -1.S
      }.otherwise {
        position_score := 0.S
      }
    }
    // white/black Kings don't gain position bonus

    v(i) := material_score + position_score
  }

  val total_score = v.reduceTree((x, y) => x + y)
  io.score := total_score
}
