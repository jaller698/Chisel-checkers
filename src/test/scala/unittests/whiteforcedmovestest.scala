import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class whiteforcedmovestest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "whiteforcedmoves"

  it should "not have forced moves on an empty board" in {
    test(new WhiteForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.out.expect(
        false.B,
        "there shouldn't be forced moves on an empty board"
      )
    }
  }

  it should "have a forced move with a white pawn on 26 and a black pawn on 22" in {
    test(new WhiteForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        if (i == 26) {
          dut.io.In(i).poke("b001".U) // white normal piece
        } else if (i == 22) {
          dut.io.In(i).poke("b011".U) // black normal piece
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


