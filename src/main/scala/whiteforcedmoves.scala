import chisel3._
import chisel3.util._

class WhiteForcedMoves() extends Module {

  private def row(i: Int) = i / 4
  private def col(i: Int): Int = {
    val r = row(i)
    val offset = if (r % 2 == 0) 1 else 0
    offset + 2 * (i % 4)
  }
  private def idx(row: Int, col: Int): Int = {
    if (row < 0 || row >= 8 || col < 0 || col >= 8)
      throw new IllegalArgumentException(s" $row  or $col out of range")
    if ((row + col) % 2 != 1)
      throw new IllegalArgumentException(s" $row , $col hit a white square")
    return row * 4 + col / 2
  }

  val io = IO(new Bundle {
    val In = Input(Vec(32, UInt(3.W)))
    val out = Output(Bool())

  })
  val forcedmoves = VecInit(Seq.tabulate(32 * 4) { i =>
    false.B
  })

  for (i <- 0 to 31) {

    val row_curr = row(i)
    val col_curr = col(i)

    // can jump down left
    if (row_curr <= 7 - 2 && col_curr >= 2) {
      val to_jump_over = idx(row_curr + 1, col_curr - 1)
      val to_jump_to = idx(row_curr + 2, col_curr - 2)
      forcedmoves(i * 2) := (
        (io.In(i) === "b010".U) // white king only
          &&
            (io.In(to_jump_over) === "b011".U || io.In(
              to_jump_over
            ) === "b100".U) // black piece
            &&
            io.In(
              to_jump_to
            ) === "b000".U
      )

    }
    // can jump down right:
    if (row_curr <= 7 - 2 && col_curr <= 7 - 2) {
      val to_jump_over = idx(row_curr + 1, col_curr + 1)
      val to_jump_to = idx(row_curr + 2, col_curr + 2)

      forcedmoves(i * 2 + 1) := (
        (io.In(i) === "b010".U)
          &&
            (io.In(to_jump_over) === "b011".U || io.In(
              to_jump_over
            ) === "b100".U)
            &&
            io.In(to_jump_to) === "b000".U
      )

    }
    // must jump up right
    if (row_curr >= 2 && col_curr <= 7 - 2) {
      val to_jump_over = idx(row_curr - 1, col_curr + 1)
      val to_jump_to = idx(row_curr - 2, col_curr + 2)

      forcedmoves(i * 2 + 2) := (
        (io.In(i) === "b001".U || io.In(i) === "b010".U) // white piece
          &&
            (io.In(to_jump_over) === "b011".U || io.In(
              to_jump_over
            ) === "b100".U) // blackpiece confirm.
            &&
            io.In(to_jump_to) === "b000".U
      )

    }

    // must jump up left:
    if (row_curr >= 2 && col_curr >= 2) {
      val to_jump_over = idx(row_curr - 1, col_curr - 1)
      val to_jump_to = idx(row_curr - 2, col_curr - 2)

      forcedmoves(i * 2 + 3) := (
        (io.In(i) === "b001".U || io.In(i) === "b010".U) // whitepiece
          &&
            (io.In(to_jump_over) === "b011".U || io.In(
              to_jump_over
            ) === "b100".U) // blackpiece confirm
            &&
            io.In(to_jump_to) === "b000".U
      )

    }
  }

  io.out := forcedmoves.reduceTree((x, y) => x || y)

}
