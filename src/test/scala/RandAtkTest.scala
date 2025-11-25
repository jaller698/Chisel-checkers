import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RandAtkTest extends AnyFlatSpec with ChiselScalatestTester with Matchers {

  behavior of "RandAtk"

  it should "RandAtk Leads to 21 taking 16 moving to 12" in {
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
        if (i == 85) true.B // 21*4 +1 (3 is up left, 1 is up right)
        else if (i == 83) true.B // 20*4 +3 (3 is up left, 1 is up right)
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
      // dut.io.boardWrite(20).expect(sEmpty)
      // dut.io.boardWrite(16).expect(sEmpty)
      // dut.io.boardWrite(13).expect(sWhite)
      dut.io.boardWrite(21).expect(sEmpty)
      dut.io.boardWrite(16).expect(sEmpty)
      dut.io.boardWrite(12).expect(sWhite)
    }
  }
  it should "Random Movement" in {
    test(new RandomAttack()) { dut =>
      val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil =
        Enum(5)

      val testVectors = Seq.tabulate(32) { i =>
        if (i < 12) sBlack
        else if (i == 16) sBlack
        else if (i >= 20) sWhite
        else sEmpty
      }

      // space*4 + 0/1/2/3 (0, is move up left, 1 atk up left, 2 move up right, 3 atk up right)
      val testAtks = Seq.tabulate(128) { i =>
        if (i == 82) true.B
        else if (i == 84) true.B
        else if (i == 86) true.B
        else if (i == 88) true.B
        else if (i == 90) true.B
        else if (i == 92) true.B
        else if (i == 94) true.B
        else false.B
      }
      dut.io.AtkPresent.poke(false.B)

      for (i <- 0 to 31) {
        dut.io.board(i).poke(testVectors(i))
      }
      for (i <- 0 to 127) {
        dut.io.whereWeCanMove(i).poke(testAtks(i))
      }

      dut.clock.step()

      dut.io.boardWrite(23).expect(sEmpty)
      dut.io.boardWrite(18).expect(sWhite)
      dut.io.boardWrite(22).expect(sWhite)
      dut.io.boardWrite(21).expect(sWhite)
      dut.io.boardWrite(20).expect(sWhite)
    }
  }
  it should "ExtraAttack" in {
    test(new ExtraAttack()) { dut =>
      val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil =
        Enum(5)

      val testVectors = Seq.tabulate(32) { i =>
        if (i == 0) sBlack
        else if (i == 14) sBlack
        else if (i == 15) sBlack
        else if (i == 18) sWhite
        else sEmpty
      }

      for (i <- 0 to 31) {
        dut.io.board(i).poke(testVectors(i))
      }
      dut.io.piece.poke(18)

      dut.clock.step()

      dut.io.boardWrite(18).expect(sEmpty)
      dut.io.boardWrite(14).expect(sEmpty)
      dut.io.boardWrite(9).expect(sWhite)
      dut.io.moved.expect(true.B)

    }
  }

}
