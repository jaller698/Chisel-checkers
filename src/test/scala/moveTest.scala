import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import CheckerRules._

class MoveTest extends AnyFlatSpec with ChiselScalatestTester with Matchers {

  
  behavior of "ChiselCheckers (current: just checking moves)"


  //Create test where it fails validmove check and cancels the move order.
  //Create attack search algo and make tests
  //Create attack move and make tests

  //Change this test to run over all the valid moves and try to do them
  //then also create one for all valid attacks.
  it should "Simple white move (21->17)" in {
    test(new Mover) { dut =>
      dut.io.from.poke(21.U)
      dut.io.to.poke(17.U)

      val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
      val testVectors = Seq.tabulate(32) { i =>
        if (i < 12) sBlack
        else if (i >= 20) sWhite
        else sEmpty
      }
      for (i <- 0 until 32) {
        dut.io.boardread(i).poke(testVectors(i))
      }

      dut.clock.step()
      dut.io.boardwrite(17).expect(testVectors(21))
      dut.io.boardwrite(21).expect(testVectors(17))
    }
  }

}
