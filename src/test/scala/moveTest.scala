import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import CheckerRules._

class MoveTest extends AnyFlatSpec with ChiselScalatestTester with Matchers {

  
  behavior of "ChiselCheckers (current: just checking moves)"

  it should "Simple white move (21->17)" in {
    test(new Mover) { dut =>

      val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)


      dut.io.from.poke(21.U)
      dut.io.to.poke(17.U)

      // val testVectors = Seq(
      //   Seq(sBlack, sBlack, sBlack, sBlack),
      //   Seq(sBlack, sBlack, sBlack, sBlack),
      //   Seq(sBlack, sBlack, sBlack, sBlack),
      //   Seq(sEmpty, sEmpty, sEmpty, sEmpty),
      //   Seq(sEmpty, sEmpty, sEmpty, sEmpty),
      //   Seq(sWhite, sWhite, sWhite, sWhite),
      //   Seq(sWhite, sWhite, sWhite, sWhite),
      //   Seq(swhite, swhite, sWhite, sWhite)
      // )
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
