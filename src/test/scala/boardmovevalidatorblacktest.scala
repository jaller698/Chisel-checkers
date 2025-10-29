import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class boardmovevalidatorblacktest
    extends AnyFlatSpec
    with ChiselScalatestTester {

  behavior of "board move validator for black"

  it should "say that this simple move is a valid move and check that the board is correct after" in {
    test(new BoardMoveValidatorBlack()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      dut.io.from.poke(22)
      dut.io.to.poke(26)

      dut.io.ValidMove.expect(
        true.B,
        s"going from 22 to 26 with black should be valid"
      )

      for (i <- 0 to 31) {
        if (i == 26) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 26")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "not be able to go up" in {
    test(new BoardMoveValidatorBlack()) { dut =>
      for (i <- 0 to 31) {
        if (i == 17) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      dut.io.from.poke(17)
      // 17 is just a random place in the middle of the board. Don't think so deeply about it.
      for (i <- 0 to 15) {
        dut.io.to.poke(i)
        dut.io.ValidMove.expect(
          false.B,
          s"going from 17 upwards to $i with black should be invalid"
        )

      }

      for (i <- 0 to 31) {
        if (i == 17) {
          dut.io.newboard(i).expect("b011".U, "the piece should still be at 16")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "not be able to walk when it can jump" in {
    test(new BoardMoveValidatorBlack()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else if (i == 26) {
          dut.io.board(i).poke("b001".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      dut.io.from.poke(22)
      dut.io.to.poke(25)
      // 17 is just a random place in the middle of the board. Don't think so deeply about it.

      dut.io.ValidMove.expect(
        false.B,
        s"going from 22 to 25 should be invalid because there is a forcedmove"
      )

      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.newboard(i).expect("b011".U, "22 should still be black.")
        } else if (i == 26) {
          dut.io.newboard(i).expect("b001".U, "26 should still be white")
        } else {
          dut.io.newboard(i).expect("b000".U, s"$i should be empty")
        }
      }

      dut.io.from.poke(22)
      dut.io.to.poke(31)

      dut.io.ValidMove.expect(true.B, "going from 22 to 31 should be allowed")

      for (i <- 0 to 31) {
        if (i == 31) {
          dut.io.newboard(i).expect("b011".U, "31 should be black")
        } else {
          dut.io.newboard(i).expect("b000".U, s"$i should be empty")
        }
      }

    }
  }

  it should "invalidate the incorrect move and not change the board" in {
    test(new BoardMoveValidatorBlack()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      dut.io.from.poke(20)
      dut.io.to.poke(26)

      dut.io.ValidMove.expect(
        false.B,
        s"it should be invalidated"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 22) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have stayed at 22")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "not be able to go from 11 to 16 and after be able to go to 15" in {
    test(new BoardMoveValidatorBlack()) { dut =>
      for (i <- 0 to 31) {
        if (i == 11) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      dut.io.from.poke(11)
      dut.io.to.poke(16)

      dut.io.ValidMove.expect(
        false.B,
        s"it should be invalidated"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 11) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have stayed at 11")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

      dut.io.from.poke(11)
      dut.io.to.poke(15)
      dut.io.ValidMove.expect(
        true.B,
        "This move should be valid!"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 15) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 11")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "be able to capture a piece from 5 to 14" in {
    test(new BoardMoveValidatorBlack()) { dut =>
      for (i <- 0 to 31) {
        if (i == 5) {
          dut.io.board(i).poke("b011".U)

        } else if (i == 9) {
          dut.io.board(i).poke("b001".U)
        } else
          dut.io.board(i).poke("b000".U)

      }

      dut.io.from.poke(5)
      dut.io.to.poke(14)

      dut.io.ValidMove.expect(
        true.B,
        s"it should be allowed to jump from 5 to 14, capturing 9"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 14) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 11")
        } else {
          dut.io
            .newboard(i)
            .expect(
              "b000".U,
              s"piece $i should be empty! 5 and 9 are especially sketchy here!"
            )
        }

      }

    }
  }

  it should "be able to capture a piece from 11 to 18" in {
    test(new BoardMoveValidatorBlack()) { dut =>
      for (i <- 0 to 31) {
        if (i == 11) {
          dut.io.board(i).poke("b011".U)

        } else if (i == 15) {
          dut.io.board(i).poke("b001".U)
        } else
          dut.io.board(i).poke("b000".U)

      }

      dut.io.from.poke(11)
      dut.io.to.poke(18)

      dut.io.ValidMove.expect(
        true.B,
        s"it should be allowed to jump from 11 to 18, capturing 15"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 18) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 18")
        } else {
          dut.io
            .newboard(i)
            .expect(
              "b000".U,
              s"piece $i should be empty! 5 and 9 are especially sketchy here!"
            )
        }

      }

    }
  }

  it should "not be able to able to capture with black white black" in {
    test(new BoardMoveValidatorBlack()) { dut =>
      for (i <- 0 to 31) {
        if (i == 0) {
          dut.io.board(i).poke("b011".U)

        } else if (i == 5) {
          dut.io.board(i).poke("b001".U)
        } else if (i == 9) {
          dut.io.board(i).poke("b011".U)

        } else
          dut.io.board(i).poke("b000".U)

      }

      dut.io.from.poke(0)
      dut.io.to.poke(9)

      dut.io.ValidMove.expect(
        false.B,
        s"it shouldn't be allowed to jump from 0 to 9, capturing 5. The move should fail"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 0) {
          dut.io
            .newboard(i)
            .expect("b011".U, "0 should still have a black piece")
        } else if (i == 5) {
          dut.io
            .newboard(i)
            .expect("b001".U, "5 should still have a white piece")
        } else if (i == 9) {
          dut.io
            .newboard(i)
            .expect("b011".U, "9 should still have a black piece")

        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

}
