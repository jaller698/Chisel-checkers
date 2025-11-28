import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class boardeval2test extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "boardeval2_component"

  it should "have score 0 on empty board" in {
    test(new boardeval2()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.score.expect(
        0.S,
        s"the score should be 0 when there is nothing on the board"
      )
    }
  }

  it should "reward white pawn closer to promotion with position bonus" in {
    test(new boardeval2()) { dut =>
      for (i <- 0 to 31) {
        if (i == 0) {
          dut.io
            .In(i)
            .poke(
              "b001".U
            ) // White pawn at row 0: +1 material + 3 position = +4
        } else if (i == 20) {
          dut.io
            .In(i)
            .poke(
              "b001".U
            ) // White pawn at row 5: +1 material + 0 position = +1
        } else {
          dut.io.In(i).poke("b000".U)
        }
      }

      // Material: 2 white pawns = +2
      // Position: row 0 pawn = +3, row 5 pawn = 0
      // Total: 2 + 3 = 5
      dut.io.score.expect(
        5.S,
        s"white pawn at row 0 should get +3 position bonus, total score should be 5"
      )
    }
  }

  it should "combine material and position correctly for both colors" in {
    test(new boardeval2()) { dut =>
      for (i <- 0 to 31) {
        if (i == 0) {
          dut.io
            .In(i)
            .poke(
              "b001".U
            ) // White pawn at row 0: +1 + 3 = +4
        } else if (i == 31) {
          dut.io
            .In(i)
            .poke(
              "b011".U
            ) // Black pawn at row 7: -1 + (-3) = -4
        } else {
          dut.io.In(i).poke("b000".U)
        }
      }

      // White: +4, Black: -4, Total: 0
      dut.io.score.expect(
        0.S,
        s"material and position should combine correctly: white +4, black -4 = 0"
      )
    }
  }
}
