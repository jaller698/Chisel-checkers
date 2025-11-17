import chisel3._
import chisel3.util._

//I can make this one do a bunch of things.
//Return valid/invalid and then return the board after black did their move.

//This one will only validate black moves because only black plays.
//For now, it is only correct for black pawns.
//TODO: Implement functionality for black kings too.
//TODO: extend it to white as well.
//  When adding white functionality
//    we need to add an input that is the color moving and
//    and building a whiteforcedmoves component.
//      This should be fairly easy by copying code from blackforcedmoves

class MoveValidator extends Module {
  val io = IO(new Bundle {

    val board = Input(Vec(32, UInt(3.W)))

    val color = Input(UInt(1.W)) // 0 is black, 1 is white.
    val from = Input(UInt(5.W))
    val to = Input(UInt(5.W))

    val newboard = Output(Vec(32, UInt(3.W)))

    val ValidMove = Output(Bool())

    // debugsignals

    val out_colorMovingFromIsCorrect = Output(Bool())
    val out_movingToEmpty = Output(Bool())
    val out_correctColorInBetween = Output(Bool())
    val out_validDirection = Output(Bool())
    val out_validDifference = Output(Bool())
    val out_difference = Output(SInt())
    val out_forcedmoves = Output(Bool())

  })

  val forcedmovesblack = Module(new BlackForcedMoves())
  for (i <- 0 to 31) {
    forcedmovesblack.io.In(i) := io.board(i)
  }
  val forcedmoveswhite = Module(new BlackForcedMoves())
  for (i <- 0 to 31) {
    forcedmoveswhite.io.In(i) := io.board(i)
  }
  val forcedmoves = Wire(Bool())
  when(io.color === 0.U) {
    forcedmoves := forcedmovesblack.io.out
  }.otherwise {
    forcedmoves := forcedmoveswhite.io.out
  }

  // first checking that color moving is correct.
  val colorMovingFromIsCorrect =
    (
      io.color === 0.U && ( // color moving is black
        io.board(io.from) === "b011".U || // black pawn
          io.board(io.from) === "b100".U // black king
      )
    ) ||
      (
        io.color === 1.U && ( // color moving is white
          io.board(io.from) === "b001".U || // white pawn
            io.board(io.from) === "b010".U // white king.
        )
      )
  // second checking that we are moving to an empty place.
  val movingToEmpty = (io.board(io.to) === "b000".U)

  // I won't zero out the thing in between before I know that it is a valid move.

  val difference = Wire(SInt(6.W))
  difference := io.to.asSInt - io.from.asSInt // This one is needed a lot.
  // val difference = io.to.asSInt - io.from.asSInt //This one is needed a lot.

  // if difference is positive, it can't be a white pawn.
  // if difference is negative it can't be a black pawn.
  val validDirection = Wire(Bool()) // idk if I need something around this.
  when(difference < 0.S) {
    validDirection := io.board(io.from) =/= "b011".U
    // maybe wrong. if diff under zero, cant be a black pawn.

  }.otherwise {
    validDirection := io.board(io.from) =/= "b001".U
    // maybe wrong
  }

  /*
        With the difference, I will do 2 things.
        1. check that the difference is valid at all.
        2. Check that the thing in between is valid.
            Set the coordinate in between to be zero-ed out IF the movement is valid in the end.
   */

  // These two will be set again in the difference switch statement.
  val isThereAnInBetween = Wire(Bool())
  isThereAnInBetween := false.B
  val coordinateInBetween = WireDefault(0.U(5.W))

  val correctColorInBetween = Wire(
    Bool()
  ) // I'd init this without a default value if I knew more Chisel
  correctColorInBetween := false.B
  when(isThereAnInBetween) {
    correctColorInBetween :=
      (io.color === 0.U &&
        (
          io.board(coordinateInBetween) === "b001".U ||
            io.board(coordinateInBetween) === "b010".U
        )) || (
        io.color === 1.U && (
          io.board(coordinateInBetween) === "b011".U ||
            io.board(coordinateInBetween) === "b100".U
        )
      )
  }.otherwise {
    correctColorInBetween := true.B
  }

  // check that it is a valid movement with the difference.
  // To know this, I need to do modulo stuff that would be based on the row of from or to
  // checks that it is a valid move and then checks then zeroes out the thing in between.
  val validDifference = Wire(Bool())
  validDifference := false.B

  switch(difference) {
    /*
is(11.S(6.W)){
        validDifference:=false.B
    }
     */

    is(9.S(6.W)) { // jumping right. Could either be 4 or 5 to jump the first one.

      validDifference := (io.from % 4.U =/= 3.U)
      when(io.from % 8.U <= 4.U) {
        coordinateInBetween := io.from + 5.U
      }.otherwise {
        coordinateInBetween := io.from + 4.U
      }
      isThereAnInBetween := true.B
      // not always valid.
      // check which one is in the middle and color and such.
    }
    is(7.S(6.W)) {

      validDifference := (io.from % 4.U =/= 0.U)
      when(io.from % 8.U <= 4.U) {
        coordinateInBetween := io.from + 4.U
      }.otherwise {
        coordinateInBetween := io.from + 3.U
      }
      isThereAnInBetween := true.B
      // not always valid.
      // check which one is in the middle and color and such.

    }
    is(3.S(6.W)) {

      isThereAnInBetween := false.B
      validDifference := (io.from % 8.U >= 5.U && forcedmoves === false.B)
      // check if it is a valid move.
      // check if move is forced.

    }
    is(4.S(6.W)) {

      isThereAnInBetween := false.B
      validDifference := (forcedmoves === false.B)
      // check if move is forced.

    }
    is(5.S(6.W)) {

      isThereAnInBetween := false.B
      validDifference := (io.from % 8.U < 3.U && forcedmoves === false.B)

      // check if it is a valid move.
      // check if move is forced.

    }
    is(-9.S(6.W)) { // jumping right. Could either be 4 or 5 to jump the first one.
      validDifference := (io.from % 4.U =/= 0.U)
      when(io.from % 8.U <= 4.U) {
        coordinateInBetween := io.from - 4.U
      }.otherwise {
        coordinateInBetween := io.from - 5.U
      }
      isThereAnInBetween := true.B
      // not always valid.
      // check which one is in the middle and color and such.

    }
    is(-7.S(6.W)) {
      validDifference := (io.from % 4.U =/= 3.U)
      when(io.from % 8.U <= 4.U) {
        coordinateInBetween := io.from - 3.U
      }.otherwise {
        coordinateInBetween := io.from - 4.U
      }
      isThereAnInBetween := true.B
      // not always valid.
      // check which one is in the middle and color and such.

    }
    is(-3.S(6.W)) {
      isThereAnInBetween := false.B
      validDifference := (io.from % 8.U <= 5.U && forcedmoves === false.B)
      // check if it is a valid move.
      // check if move is forced.

    }
    is(-4.S(6.W)) {
      isThereAnInBetween := false.B
      validDifference := (forcedmoves === false.B)

    }
    is(-5.S(6.W)) {
      isThereAnInBetween := false.B
      validDifference := (io.from % 8.U >= 5.U && forcedmoves === false.B)
      // check if it is a valid move.
      // check if move is forced.

    }
    /*
    is(-11.S(6.W)){
        validDifference:=false.B

    }
     */

  }

  io.out_colorMovingFromIsCorrect := colorMovingFromIsCorrect
  io.out_movingToEmpty := movingToEmpty
  io.out_correctColorInBetween := correctColorInBetween
  io.out_validDirection := validDirection
  io.out_validDifference := validDifference
  io.out_difference := difference
  io.out_forcedmoves := forcedmoves

  io.ValidMove :=
    colorMovingFromIsCorrect &&
      movingToEmpty &&
      correctColorInBetween &&
      validDirection &&
      validDifference

  io.newboard := io.board
  when(io.ValidMove) {
    when(isThereAnInBetween) {
      io.newboard(coordinateInBetween) := "b000".U
      // This is supposed to deal with killing the thing in between.
    }

    io.newboard(io.from) := "b000".U
    io.newboard(io.to) := io.board(io.from)

    when(io.to / 8.U === 7.U && io.color === 0.U) {
      io.newboard(io.to) := "b100".U // black king
    }
    when(io.to / 8.U === 0.U && io.color === 1.U) {
      io.newboard(io.to) := "b010".U // white king.
    }

  }

}
