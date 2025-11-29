import chisel3._
import chisel3.util._

// MoveValidator validates moves for a checkers-like game.
class MoveValidator extends Module {
  val io = IO(new Bundle {
    val board = Input(Vec(32, UInt(3.W)))
    val color = Input(UInt(1.W)) // 0 = black, 1 = white
    val from = Input(UInt(5.W))
    val to = Input(UInt(5.W))

    val newboard = Output(Vec(32, UInt(3.W)))
    val ValidMove = Output(Bool())
  })

  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)

  // Forced moves modules for both colors (white uses BlackForcedMoves for now)
  val forcedmovesblack = Module(new BlackForcedMoves())
  val forcedmoveswhite = Module(new BlackForcedMoves())
  for (i <- 0 until 32) {
    forcedmovesblack.io.In(i) := io.board(i)
    forcedmoveswhite.io.In(i) := io.board(i)
  }
  val forcedmoves = Wire(Bool())
  forcedmoves := Mux(
    io.color === 0.U,
    forcedmovesblack.io.out,
    forcedmoveswhite.io.out
  )

  // Check that the piece being moved matches the color
  val colorMovingFromIsCorrect = (
    (io.color === 0.U && (io
      .board(io.from) === sBlack || io.board(io.from) === sBlackKing)) ||
      (io.color === 1.U && (io.board(io.from) === sWhite || io.board(
        io.from
      ) === sWhiteKing))
  )

  // Check that the destination is empty
  val movingToEmpty = (io.board(io.to) === sEmpty)

  // Difference between destination and source
  val difference = Wire(SInt(6.W))
  difference := io.to.asSInt - io.from.asSInt

  // Check that the move is in a valid direction for the piece
  val validDirection = Wire(Bool())
  when(difference < 0.S) {
    validDirection := io.board(io.from) =/= sBlack // Not a black pawn
  }.otherwise {
    validDirection := io.board(io.from) =/= sWhite // Not a white pawn
  }

  // Used for jumps: is there a piece in between, and is it the correct color?
  val isThereAnInBetween = Wire(Bool())
  isThereAnInBetween := false.B
  val coordinateInBetween = WireDefault(0.U(5.W))

  val correctColorInBetween = Wire(Bool())
  correctColorInBetween := false.B
  when(isThereAnInBetween) {
    correctColorInBetween := (
      (io.color === 0.U && (io.board(coordinateInBetween) === sWhite || io
        .board(coordinateInBetween) === sWhiteKing)) ||
        (io.color === 1.U && (io.board(coordinateInBetween) === sBlack || io
          .board(coordinateInBetween) === sBlackKing))
    )
  }.otherwise {
    correctColorInBetween := true.B
  }

  // Check that the move is a valid jump or step, and set in-between coordinate if needed
  val validDifference = Wire(Bool())
  validDifference := false.B

  switch(difference) {
    is(9.S(6.W)) { // jump right forward
      validDifference := (io.from(1, 0) =/= 3.U)
      when(io.from(2, 0) <= 4.U) {
        coordinateInBetween := io.from + 5.U
      }.otherwise {
        coordinateInBetween := io.from + 4.U
      }
      isThereAnInBetween := true.B
    }
    is(7.S(6.W)) { // jump left forward
      validDifference := (io.from(1, 0) =/= 0.U)

      //when(io.from% 8.U<= 4.U) {
      when(io.from(2) === 0.U) {
        coordinateInBetween := io.from + 4.U
      }.otherwise {
        coordinateInBetween := io.from + 3.U
      }
      isThereAnInBetween := true.B
    }
    is(3.S(6.W)) { // step left forward
      isThereAnInBetween := false.B
      validDifference := (io.from(2, 0) >= 5.U && !forcedmoves)
    }
    is(4.S(6.W)) { // step right forward
      isThereAnInBetween := false.B
      validDifference := !forcedmoves
    }
    is(5.S(6.W)) { // step far right forward
      isThereAnInBetween := false.B
      validDifference := (io.from(2, 0) < 3.U && !forcedmoves)
    }
    is(-9.S(6.W)) { // jump right backward
      validDifference := (io.from(1, 0) =/= 0.U)
      //when(io.from(2, 0) <= 4.U) {
      
      when(io.from(2) === 0.U) {
        coordinateInBetween := io.from - 4.U
      }.otherwise {
        coordinateInBetween := io.from - 5.U
      }
      isThereAnInBetween := true.B
    }
    is(-7.S(6.W)) { // jump left backward
      validDifference := (io.from(1, 0) =/= 3.U)
      when(io.from(2) === 0.U) {
        coordinateInBetween := io.from - 3.U
      }.otherwise {
        coordinateInBetween := io.from - 4.U
      }
      isThereAnInBetween := true.B
    }
    is(-3.S(6.W)) { // step left backward
      isThereAnInBetween := false.B
      validDifference := (io.from(2, 0) <= 5.U && !forcedmoves)
    }
    is(-4.S(6.W)) { // step right backward
      isThereAnInBetween := false.B
      validDifference := !forcedmoves
    }
    is(-5.S(6.W)) { // step far right backward
      isThereAnInBetween := false.B
      validDifference := (io.from(2, 0) >= 5.U && !forcedmoves)
    }
  }

  // Valid move if all checks pass
  io.ValidMove := colorMovingFromIsCorrect && movingToEmpty && correctColorInBetween && validDirection && validDifference

  // Default: newboard is the same as the input board
  io.newboard := io.board
  when(io.ValidMove) {
    when(isThereAnInBetween) {
      io.newboard(coordinateInBetween) := sEmpty // Remove captured piece
    }

    io.newboard(io.from) := sEmpty
    io.newboard(io.to) := io.board(io.from)

    when(io.to >= 28.U && io.color === 0.U) {
      io.newboard(io.to) := sBlackKing // black king
    }
    when(io.to <= 3.U && io.color === 1.U) {
      io.newboard(io.to) := sWhiteKing // white king.
    }
  }
}
