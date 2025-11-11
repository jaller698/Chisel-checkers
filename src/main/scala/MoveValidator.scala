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

    val from = Input(UInt(5.W))
    val to = Input(UInt(5.W))

    val newboard = Output(Vec(32, UInt(3.W)))

    val ValidMove = Output(Bool())
  })
  val necessaryforcedmoves = Module(new BlackForcedMoves())
  for (i <- 0 to 31) {
    necessaryforcedmoves.io.In(i) := io.board(i)
  }

  // First to check is that from is black and whether it is a black king or a black pawn.
  // second to check is that to is empty.
  // Then we need to check if they are far from eachother or not.
  // If they are far from each other, then we will add a check if there is something between

  val difference =
    io.to - io.from // I am just making it for black pawns for now.

  io.ValidMove := false.B
  for (i <- 0 to 31) {
    io.newboard(i) := io.board(i)
  }
  switch(difference) {
    is(9.U) { // jumping right. Could either be 4 or 5 to jump the first one.
      when(
        io.from % 4.U =/= 3.U && // This is because %4==3 can't do right double jumps.
          io.board(io.from) === "b011".U &&
          io.board(io.to) === "b000".U &&
          (
            (
              (io.from % 8.U < 3.U) &&
                (io.board(io.from + 5.U) === "b001".U)
            ) // checks that it is white.
              ||
                (
                  (io.from % 8.U >= 4.U) &&
                    (io.board(io.from + 4.U) === "b001".U)
                )
          )
      ) {

        // sets the one between to be empty.
        when(io.from % 8.U < 4.U) {
          io.newboard(io.from +% 5.U) := "b000".U
        }.otherwise(
          io.newboard(io.from +% 4.U) := "b000".U
        )

        io.ValidMove := true.B

      }

    }
    is(7.U) {
      when(
        io.from % 4.U =/= 0.U && // This is because %4==0 can't do left double jumps. double jumps.
          io.board(io.from) === "b011".U &&
          io.board(io.to) === "b000".U &&
          (
            (
              (io.from % 8.U < 4.U) &&
                (io.board(
                  io.from + 4.U
                ) === "b001".U) // checks that it is white.
            )
              ||
                (
                  (io.from % 8.U > 4.U) &&
                    (io.board(io.from + 3.U) === "b001".U)
                )
          )
      ) {

        // sets the one between to be empty.

        when(io.from % 8.U < 4.U) {
          io.newboard(io.from +% 4.U) := "b000".U
        }.otherwise(
          io.newboard(io.from +% 3.U) := "b000".U
        )
        io.ValidMove := true.B

      }

      // if from%4==0, this isn't valid.
    }
    is(3.U) {
      when(
        io.from % 8.U > 4.U &&
          io.board(io.from) === "b011".U &&
          io.board(io.to) === "b000".U &&
          necessaryforcedmoves.io.out === false.B
      ) {
        io.ValidMove := true.B

      }

    }
    is(4.U) {
      when(
        io.board(io.from) === "b011".U &&
          io.board(io.to) === "b000".U &&
          necessaryforcedmoves.io.out === false.B
      ) {
        io.ValidMove := true.B

      }
    }
    is(5.U) {

      // half of the rows use 5 to go right.
      when(
        io.from % 8.U < 3.U &&
          io.board(io.from) === "b011".U &&
          io.board(io.to) === "b000".U &&
          necessaryforcedmoves.io.out === false.B
      ) {
        io.ValidMove := true.B

      }

    }
  }

  when(io.ValidMove) {
    io.newboard(io.from) := "b000".U

    // Check if black pawn reaches the king row (positions 28-31)
    when(io.board(io.from) === "b011".U && io.to >= 28.U) {
      io.newboard(io.to) := "b100".U // Promote to black king
    }.otherwise {
      io.newboard(io.to) := io.board(io.from) // Normal move
    }
  }

}
