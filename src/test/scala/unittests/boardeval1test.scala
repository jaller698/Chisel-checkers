import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class boardeval1test extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "legalmovesforwhite_component"

  it should "have score 0 on empty board" in {
    test(new BoardEval1()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.score.expect(
        0.S,
        s"the score should be 0 when there is nothing on the board"
      )

    }
  }

  it should "have score 0 on standard board" in {
    test(new BoardEval1()) { dut =>
      for (i <- 0 to 31) {
        if (i < 12) {
          dut.io.In(i).poke("b011".U)

        } else if (i >= 20) {
          dut.io.In(i).poke("b001".U)

        } else {
          dut.io.In(i).poke("b000".U)

        }
      }

      dut.io.score.expect(
        0.S,
        s"the score should be 0 when there is nothing on the board"
      )

    }
  }

  it should "place 3 black and 1 white and get score -2" in {
    test(new BoardEval1()) { dut =>
      for (i <- 0 to 31) {
        if (i == 14 || i == 17 | i == 19) {
          dut.io.In(i).poke("b011".U)

        } else if (i == 20) {
          dut.io.In(i).poke("b001".U)

        } else {
          dut.io.In(i).poke("b000".U)

        }
      }

      dut.io.score.expect(
        -2.S,
        s"the score should be 0 when there is nothing on the board"
      )

    }
  }

  

  // I should have more stuff on here.

}
