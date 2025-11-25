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
    val movedOne = Output(UInt(5.W))
  })
  io.movedOne := 33.U
  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)

  val boardTMP = RegInit(VecInit(Seq.tabulate(32) { i =>
    if (i < 12) sBlack
    else if (i >= 20) sWhite
    else sEmpty
  }))

  boardTMP := io.board
  io.boardWrite := boardTMP
  // make a mutex lock.
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
            boardTMP := io.board
            boardTMP(piece) := sEmpty
            boardTMP(to_jump_over) := sEmpty
            boardTMP(to_jump_to) := sWhite
            io.movedOne := to_jump_to.U
          }
        }
        if (k % 4 == 3) {
          if (row_curr - 2 >= 0 && col_curr + 2 <= 7) {
            val to_jump_over = idx(row_curr - 1, col_curr + 1)
            val to_jump_to = idx(row_curr - 2, col_curr + 2)
            boardTMP := io.board
            boardTMP(piece) := sEmpty
            boardTMP(to_jump_over) := sEmpty
            boardTMP(to_jump_to) := sWhite
            io.movedOne := to_jump_to.U
          }
        }
        // Break out of for loop

      }
    }
  }.otherwise {
    for (k <- 0 to 127 by 2) {

      when(io.whereWeCanMove(k) === true.B) {
        val piece = (k / 4)
        val row_curr = row(piece)
        val col_curr = col(piece)

        if (k % 4 == 0) {
          if (row_curr - 2 >= 0 && col_curr - 2 >= 0) {
            val to_jump_over = idx(row_curr - 1, col_curr - 1)
            boardTMP := io.board
            boardTMP(piece) := sEmpty
            boardTMP(to_jump_over) := sWhite
          }
        }
        if (k % 4 == 2) {
          if (row_curr - 2 >= 0 && col_curr + 2 <= 7) {
            val to_jump_over = idx(row_curr - 1, col_curr + 1)
            boardTMP := io.board
            boardTMP(piece) := sEmpty
            boardTMP(to_jump_over) := sWhite
          }
        }
        // Break out of for loop

      }

    }
  }

}

class ExtraAttack extends Module {
  val io = IO(new Bundle {
    val board = Input(Vec(32, UInt(3.W)))
    val piece = Input(UInt(5.W))
    val boardWrite = Output(Vec(32, UInt(3.W)))
    val moved = Output(Bool())
  })
  io.moved := false.B
  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)

  val boardTMP = RegInit(VecInit(Seq.tabulate(32) { i =>
    if (i < 12) sBlack
    else if (i >= 20) sWhite
    else sEmpty
  }))

  boardTMP := io.board
  io.boardWrite := boardTMP

  val rowa = (io.piece / 4.U)
  val rola =
    ((io.piece / 4.U) + 1.U) % 2.U + (io.piece % 4.U) + (io.piece % 4.U)

  when(rowa % 2.U === 0.U) {
    when(rowa - 2.U >= 0.U) {
      when(rola - 2.U >= 0.U && rola + 2.U <= 7.U) { // can go both ways

        when(
          io.board(io.piece - 4.U) === sBlack && io.board(
            io.piece - 9.U
          ) === sEmpty
        ) {
          boardTMP(io.piece) := sEmpty
          boardTMP(io.piece - 4.U) := sEmpty
          boardTMP(io.piece - 9.U) := sWhite
          io.moved := true.B
        }.elsewhen(
          io.board(io.piece - 3.U) === sBlack && io.board(
            io.piece - 7.U
          ) === sEmpty
        ) {
          boardTMP(io.piece) := sEmpty
          boardTMP(io.piece - 3.U) := sEmpty
          boardTMP(io.piece - 7.U) := sWhite
          io.moved := true.B
        }

      }.elsewhen(rola - 2.U >= 0.U) { // can only go left
        when(
          io.board(io.piece - 4.U) === sBlack && io.board(
            io.piece - 9.U
          ) === sEmpty
        ) {
          boardTMP(io.piece) := sEmpty
          boardTMP(io.piece - 4.U) := sEmpty
          boardTMP(io.piece - 9.U) := sWhite
          io.moved := true.B
        }

      }.otherwise { // can only go right
        when(
          io.board(io.piece - 3.U) === sBlack && io.board(
            io.piece - 7.U
          ) === sEmpty
        ) {
          boardTMP(io.piece) := sEmpty
          boardTMP(io.piece - 3.U) := sEmpty
          boardTMP(io.piece - 7.U) := sWhite
          io.moved := true.B
        }
      }
    }

  }.otherwise {
    when(rowa - 2.U >= 0.U) {
      when(rola - 2.U >= 0.U && rola + 2.U <= 7.U) { // can go both ways

        when(
          io.board(io.piece - 5.U) === sBlack && io.board(
            io.piece - 9.U
          ) === sEmpty
        ) {
          boardTMP(io.piece) := sEmpty
          boardTMP(io.piece - 5.U) := sEmpty
          boardTMP(io.piece - 9.U) := sWhite
          io.moved := true.B
        }.elsewhen(
          io.board(io.piece - 4.U) === sBlack && io.board(
            io.piece - 7.U
          ) === sEmpty
        ) {
          boardTMP(io.piece) := sEmpty
          boardTMP(io.piece - 4.U) := sEmpty
          boardTMP(io.piece - 7.U) := sWhite
          io.moved := true.B
        }

      }.elsewhen(rola - 2.U >= 0.U) { // can only go left
        when(
          io.board(io.piece - 5.U) === sBlack && io.board(
            io.piece - 9.U
          ) === sEmpty
        ) {
          boardTMP(io.piece) := sEmpty
          boardTMP(io.piece - 5.U) := sEmpty
          boardTMP(io.piece - 9.U) := sWhite
          io.moved := true.B
        }

      }.otherwise { // can only go right
        when(
          io.board(io.piece - 4.U) === sBlack && io
            .board(io.piece - 7.U) === sEmpty
        ) {
          boardTMP(io.piece) := sEmpty
          boardTMP(io.piece - 4.U) := sEmpty
          boardTMP(io.piece - 7.U) := sWhite
          io.moved := true.B
        }
      }

    }
  }

}
