import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class RandAtkTest extends AnyFlatSpec with ChiselScalatestTester with Matchers  {

  behavior of "RandAtk"

  it should "have a forcedmove with a black pawn on 22 and a white pawn on 26" in {
    test(new RandomAttack()) { dut =>
      val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil =
        Enum(5)

      val testVectors = Seq.tabulate(32) { i =>
        if (i < 12) sBlack
        else if (i == 16) sBlack
        else if (i >= 20) sWhite
        else sEmpty
      }
      val testAtks = Seq.tabulate(128) { i =>
        if (i == 83) true.B
        else false.B
      }
      dut.io.AtkPresent.poke(true.B)

      for (i <- 0 to 31) {
        dut.io.board(i).poke(testVectors(i))
      }
      for (i <- 0 to 127) {
        dut.io.whereWeCanMove(i).poke(testAtks(i))
      }

      dut.clock.step()
      dut.io.boardWrite(20).expect(sEmpty)
      dut.io.boardWrite(16).expect(sEmpty)
      dut.io.boardWrite(13).expect(sWhite)
    }
  }

}
