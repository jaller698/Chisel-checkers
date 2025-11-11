import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import CheckerRules._

class boardmovevalidatorblacktest
    extends AnyFlatSpec
    with ChiselScalatestTester {

  def convertToScalaBoard(dutBoard: Vec[UInt]): Vector[Piece] = {
    Vector.tabulate(32) { i =>
      dutBoard(i).peek().litValue.toInt match {
        case 0 => Empty
        case 1 => White
        case 2 => WhiteKing
        case 3 => Black
        case 4 => BlackKing
        case _ => Empty
      }
    }
  }

  behavior of "board move validator for black"

  it should "say that this simple move is a valid move and check that the board is correct after" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      dut.io.from.poke(22)
      dut.io.to.poke(26)

      // Convert hardware board to Scala board for validation
      val scalaBoard = convertToScalaBoard(dut.io.board)
      val expectedValid = isMoveValid(22, 26, scalaBoard, isWhiteTurn = false)

      dut.io.ValidMove.expect(
        expectedValid.B,
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

      val newScalaBoard = convertToScalaBoard(dut.io.newboard)
      val updatedBoard = applyMove(22, 26, scalaBoard)
      updatedBoard match {
        case Some(expectedBoard) =>
          for (i <- 0 to 31) {
            assert(
              newScalaBoard(i) == expectedBoard(i),
              s"Position $i: expected ${expectedBoard(i)}, got ${newScalaBoard(i)}"
            )
          }
        case None =>
          fail("applyMove returned None - move should have been valid")
      }
    }
  }

  it should "not be able to go up" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 17) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      val scalaBoard = convertToScalaBoard(dut.io.board)

      dut.io.from.poke(17)
      // 17 is just a random place in the middle of the board. Don't think so deeply about it.
      for (i <- 0 to 15) {
        dut.io.to.poke(i)
        val expectedValid = isMoveValid(17, i, scalaBoard, isWhiteTurn = false)
        dut.io.ValidMove.expect(
          expectedValid.B,
          s"going from 17 upwards to $i with black should be invalid"
        )
      }

      for (i <- 0 to 31) {
        if (i == 17) {
          dut.io.newboard(i).expect("b011".U, "the piece should still be at 17")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }
      }

      val newScalaBoard = convertToScalaBoard(dut.io.newboard)
      for (i <- 0 to 31) {
        assert(
          newScalaBoard(i) == scalaBoard(i),
          s"Position $i: board should remain unchanged"
        )
      }
    }
  }

  it should "not be able to walk when it can jump" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else if (i == 26) {
          dut.io.board(i).poke("b001".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      val scalaBoard = convertToScalaBoard(dut.io.board)

      dut.io.from.poke(22)
      dut.io.to.poke(25)

      val expectedValid1 = isMoveValid(22, 25, scalaBoard, isWhiteTurn = false)
      dut.io.ValidMove.expect(
        expectedValid1.B,
        s"going from 22 to 25 should be invalid because there is a forced move"
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

      val newScalaBoard1 = convertToScalaBoard(dut.io.newboard)
      for (i <- 0 to 31) {
        assert(
          newScalaBoard1(i) == scalaBoard(i),
          s"Position $i: board should remain unchanged after invalid move"
        )
      }

      dut.io.from.poke(22)
      dut.io.to.poke(31)

      val expectedValid2 = isMoveValid(22, 31, scalaBoard, isWhiteTurn = false)
      dut.io.ValidMove
        .expect(expectedValid2.B, "going from 22 to 31 should be allowed")

      for (i <- 0 to 31) {
        if (i == 31) {
          dut.io
            .newboard(i)
            .expect("b100".U, "31 should be black king (promoted)")
        } else {
          dut.io.newboard(i).expect("b000".U, s"$i should be empty")
        }
      }

      val newScalaBoard2 = convertToScalaBoard(dut.io.newboard)
      val updatedBoard = applyMove(22, 31, scalaBoard)
      updatedBoard match {
        case Some(expectedBoard) =>
          for (i <- 0 to 31) {
            assert(
              newScalaBoard2(i) == expectedBoard(i),
              s"Position $i: expected ${expectedBoard(i)}, got ${newScalaBoard2(i)}"
            )
          }
        case None =>
          fail("applyMove returned None - move should have been valid")
      }
    }
  }

  it should "invalidate the incorrect move and not change the board" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      val scalaBoard = convertToScalaBoard(dut.io.board)

      dut.io.from.poke(20)
      dut.io.to.poke(26)

      val expectedValid = isMoveValid(20, 26, scalaBoard, isWhiteTurn = false)
      dut.io.ValidMove.expect(
        expectedValid.B,
        s"it should be invalidated"
      )

      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have stayed at 22")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }
      }

      val newScalaBoard = convertToScalaBoard(dut.io.newboard)
      for (i <- 0 to 31) {
        assert(
          newScalaBoard(i) == scalaBoard(i),
          s"Position $i: board should remain unchanged after invalid move"
        )
      }
    }
  }

  it should "not be able to go from 11 to 16 and after be able to go to 15" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 11) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

      val scalaBoard = convertToScalaBoard(dut.io.board)

      dut.io.from.poke(11)
      dut.io.to.poke(16)

      val expectedValid1 = isMoveValid(11, 16, scalaBoard, isWhiteTurn = false)
      dut.io.ValidMove.expect(
        expectedValid1.B,
        s"it should be invalidated"
      )

      for (i <- 0 to 31) {
        if (i == 11) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have stayed at 11")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }
      }

      val newScalaBoard1 = convertToScalaBoard(dut.io.newboard)
      for (i <- 0 to 31) {
        assert(
          newScalaBoard1(i) == scalaBoard(i),
          s"Position $i: board should remain unchanged after invalid move"
        )
      }

      dut.io.from.poke(11)
      dut.io.to.poke(15)
      val expectedValid2 = isMoveValid(11, 15, scalaBoard, isWhiteTurn = false)
      dut.io.ValidMove.expect(
        expectedValid2.B,
        "This move should be valid!"
      )

      for (i <- 0 to 31) {
        if (i == 15) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 15")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }
      }

      val newScalaBoard2 = convertToScalaBoard(dut.io.newboard)
      val updatedBoard = applyMove(11, 15, scalaBoard)
      updatedBoard match {
        case Some(expectedBoard) =>
          for (i <- 0 to 31) {
            assert(
              newScalaBoard2(i) == expectedBoard(i),
              s"Position $i: expected ${expectedBoard(i)}, got ${newScalaBoard2(i)}"
            )
          }
        case None =>
          fail("applyMove returned None - move should have been valid")
      }
    }
  }

  it should "be able to capture a piece from 5 to 14" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 5) {
          dut.io.board(i).poke("b011".U)
        } else if (i == 9) {
          dut.io.board(i).poke("b001".U)
        } else
          dut.io.board(i).poke("b000".U)
      }

      val scalaBoard = convertToScalaBoard(dut.io.board)

      dut.io.from.poke(5)
      dut.io.to.poke(14)

      val expectedValid = isMoveValid(5, 14, scalaBoard, isWhiteTurn = false)
      dut.io.ValidMove.expect(
        expectedValid.B,
        s"it should be allowed to jump from 5 to 14, capturing 9"
      )

      for (i <- 0 to 31) {
        if (i == 14) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 14")
        } else {
          dut.io
            .newboard(i)
            .expect(
              "b000".U,
              s"piece $i should be empty! 5 and 9 are especially sketchy here!"
            )
        }
      }

      val newScalaBoard = convertToScalaBoard(dut.io.newboard)
      val updatedBoard = applyMove(5, 14, scalaBoard)
      updatedBoard match {
        case Some(expectedBoard) =>
          for (i <- 0 to 31) {
            assert(
              newScalaBoard(i) == expectedBoard(i),
              s"Position $i: expected ${expectedBoard(i)}, got ${newScalaBoard(i)}"
            )
          }
        case None =>
          fail("applyMove returned None - move should have been valid")
      }
    }
  }

  it should "be able to capture a piece from 11 to 18" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 11) {
          dut.io.board(i).poke("b011".U)
        } else if (i == 15) {
          dut.io.board(i).poke("b001".U)
        } else
          dut.io.board(i).poke("b000".U)
      }

      val scalaBoard = convertToScalaBoard(dut.io.board)

      dut.io.from.poke(11)
      dut.io.to.poke(18)

      val expectedValid = isMoveValid(11, 18, scalaBoard, isWhiteTurn = false)
      dut.io.ValidMove.expect(
        expectedValid.B,
        s"it should be allowed to jump from 11 to 18, capturing 15"
      )

      for (i <- 0 to 31) {
        if (i == 18) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 18")
        } else {
          dut.io
            .newboard(i)
            .expect(
              "b000".U,
              s"piece $i should be empty! 11 and 15 are especially sketchy here!"
            )
        }
      }

      val newScalaBoard = convertToScalaBoard(dut.io.newboard)
      val updatedBoard = applyMove(11, 18, scalaBoard)
      updatedBoard match {
        case Some(expectedBoard) =>
          for (i <- 0 to 31) {
            assert(
              newScalaBoard(i) == expectedBoard(i),
              s"Position $i: expected ${expectedBoard(i)}, got ${newScalaBoard(i)}"
            )
          }
        case None =>
          fail("applyMove returned None - move should have been valid")
      }
    }
  }

  it should "not be able to able to capture with black white black" in {
    test(new MoveValidator()) { dut =>
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

      val scalaBoard = convertToScalaBoard(dut.io.board)

      dut.io.from.poke(0)
      dut.io.to.poke(9)

      val expectedValid = isMoveValid(0, 9, scalaBoard, isWhiteTurn = false)
      dut.io.ValidMove.expect(
        expectedValid.B,
        s"it shouldn't be allowed to jump from 0 to 9, capturing 5. The move should fail"
      )

      for (i <- 0 to 31) {
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

      val newScalaBoard = convertToScalaBoard(dut.io.newboard)
      for (i <- 0 to 31) {
        assert(
          newScalaBoard(i) == scalaBoard(i),
          s"Position $i: board should remain unchanged after invalid move"
        )
      }
    }
  }

}
