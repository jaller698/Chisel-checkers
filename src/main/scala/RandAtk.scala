import chisel3._
import chisel3.util._

class RandomAttack extends Module {

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
    val AtkPresent = Input(Bool())
    val board = Input(Vec(32, UInt(3.W)))
    val whereWeCanMove = Input(Vec(4 * 32, Bool()))
    val boardWrite = Output(Vec(32, UInt(3.W)))
  })

  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
  io.boardWrite := io.board
  when(io.AtkPresent === true.B) {
    for (k <- 1 to 127 by 2) {

      when(io.whereWeCanMove(k) === true.B) {
        val piece = (k / 4)
        val row_curr = row(piece)
        val col_curr = col(piece)

        if (k % 4 == 1) {
          if (row_curr - 2 >= 0 && col_curr - 2 >= 0) {
            val to_jump_over = idx(row_curr - 1, col_curr - 1)
            val to_jump_to = idx(row_curr - 2, col_curr - 2)
            io.boardWrite(piece) := sEmpty
            io.boardWrite(to_jump_over) := sEmpty
            io.boardWrite(to_jump_to) := sWhite
          }
        }
        if (k % 4 == 3) {
          if (row_curr - 2 >= 0 && col_curr + 2 <= 7) {
            val to_jump_over = idx(row_curr - 1, col_curr + 1)
            val to_jump_to = idx(row_curr - 2, col_curr + 2)
            io.boardWrite(piece) := sEmpty
            io.boardWrite(to_jump_over) := sEmpty
            io.boardWrite(to_jump_to) := sWhite
          }
        }

      }

    }
  }.otherwise{
    for (k <- 0 to 127 by 2) {

      when(io.whereWeCanMove(k) === true.B) {
        val piece = (k / 4)
        val row_curr = row(piece)
        val col_curr = col(piece)

        if (k % 4 == 0) {
          if (row_curr - 2 >= 0 && col_curr - 2 >= 0) {
            val to_jump_over = idx(row_curr - 1, col_curr - 1)
            io.boardWrite(piece) := sEmpty
            io.boardWrite(to_jump_over) := sEmpty

          }
        }
        if (k % 4 == 2) {
          if (row_curr - 2 >= 0 && col_curr + 2 <= 7) {
            val to_jump_over = idx(row_curr - 1, col_curr + 1)
            io.boardWrite(piece) := sEmpty
            io.boardWrite(to_jump_over) := sEmpty

          }
        }

      }

    }
  }

}
