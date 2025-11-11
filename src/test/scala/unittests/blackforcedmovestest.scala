import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class blackforcedmovestest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "blackforcedmoves"

  it should "not have forced moves on an empty board" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.out.expect(
        false.B,
        "there shouldn't be forced moves on an empty board"
      )

    }
  }

  it should "have a forcedmove with a black pawn on 22 and a white pawn on 26" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.In(i).poke("b011".U)
        } else if (i == 26) {
          dut.io.In(i).poke("b001".U)
        } else {
          dut.io.In(i).poke("b000".U)
        }
      }

      dut.io.out.expect(
        true.B,
        "should be a forced move here!"
      )

    }
  }

}
