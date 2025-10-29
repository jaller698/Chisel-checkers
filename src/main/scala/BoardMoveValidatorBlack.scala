import chisel3._
import chisel3.util._

//I can make this one do a bunch of things.
//Return valid/invalid and then return the board after black did their move.

//This one will only validate black moves because only black plays.
//For now, it is only correct for black pawns.
//TODO: Implement functionality for black kings too.

//One thing that this one doesn't do at all is make sure that they are allowed to make that movement.

class BoardMoveValidatorBlack extends Module {
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

  /*
    SEVERAL THINGS TO FIGURE OUT:
        how do I set the output to be both the old board and the modified tiles.
            I think this is just a for loop with some slight modifications.
            Still thinking about it.
        how do I find out if there are forced moves.
        This, I dont know about yet.

   */

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

        for (i <- 0 to 31) {
          io.newboard(i) := io.board(i)
        }
        io.newboard(io.from) := "b000".U
        io.newboard(io.to) := "b011".U

        // sets the one between to be empty.
        when(io.from % 8.U < 4.U) {
          io.newboard(io.from +% 5.U) := "b000".U
        }.otherwise(
          io.newboard(io.from +% 4.U) := "b000".U
        )

        io.ValidMove := true.B

      }.otherwise {
        // In this case, we have found out that the move was invalid.
        io.ValidMove := false.B
        for (i <- 0 until 31) {
          io.newboard(i) := io.board(i)
        }
      }

      // if from%4==3 this isn't valid.
      // from has to be black.
      // the place in the middle has to be white
      // the place being jumped to has to be empty.
      // if we are on a row with

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

        for (i <- 0 to 31) {
          io.newboard(i) := io.board(i)
        }
        io.newboard(io.from) := "b000".U
        io.newboard(io.to) := "b011".U

        // sets the one between to be empty now.

        when(io.from % 8.U < 4.U) {
          io.newboard(io.from +% 4.U) := "b000".U
        }.otherwise(
          io.newboard(io.from +% 3.U) := "b000".U
        )
        io.ValidMove := true.B

        // We need to set the rest of the board.
        // Scala has some functional ways of doing this cleanly.
        // A vector tabulate, I imagine.

      }.otherwise {

        // In this case, we have found out that the move was invalid.
        io.ValidMove := false.B
        for (i <- 0 until 31) {
          io.newboard(i) := io.board(i)
        }
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

        for (i <- 0 to 31) {
          io.newboard(i) := io.board(i)
        }
        io.newboard(io.from) := "b000".U
        io.newboard(io.to) := "b011".U

      }.otherwise {
        io.ValidMove := false.B
        for (i <- 0 until 31) {
          io.newboard(i) := io.board(i)
        }
      }
      // half the rows use 3 to go down left.

    }
    is(4.U) {
      when(
        io.board(io.from) === "b011".U &&
          io.board(io.to) === "b000".U &&
          necessaryforcedmoves.io.out === false.B
      ) {
        io.ValidMove := true.B

        for (i <- 0 to 31) {
          io.newboard(i) := io.board(i)
        }
        io.newboard(io.from) := "b000".U
        io.newboard(io.to) := "b011".U

      }.otherwise {
        io.ValidMove := false.B
        for (i <- 0 until 31) {
          io.newboard(i) := io.board(i)
        }
      }
      // Here we just have to check that it's valid.

      // half the rows use 4 to go left.
      // The other use it to go right.

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
        for (i <- 0 to 31) {
          io.newboard(i) := io.board(i)
        }
        io.newboard(io.from) := "b000".U
        io.newboard(io.to) := "b011".U
        // The above felt like a double assignment but ChatGPT said it is correct.

        // idk yet.

      }.otherwise {
        io.ValidMove := false.B
        for (i <- 0 to 31) {
          io.newboard(i) := io.board(i)
        }
      }

    }

  }

}
