import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class boardmovevalidatorblacktest
    extends AnyFlatSpec
    with ChiselScalatestTester {

  behavior of "board move validator for black"

  it should "say that this simple thing is a valid move" in {
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
        s"the score should be 0 when there is nothing on the board"
      )

    }
  }

}
