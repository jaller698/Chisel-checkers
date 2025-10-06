import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import CheckerRules._

class MoveTest extends AnyFlatSpec with ChiselScalatestTester {

  val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
//   private def initialBoard: Vec(32, UInt(4.W)) =
//     Vector.tabulate(32) { i =>
//       if (i < 12) sBlack
//       else if (i >= 20) sWhite
//       else sEmpty
//     }
//   val board = VecInit(Seq.tabulate(32) { i =>
//     if (i < 12) sBlack
//     else if (i >= 20) sWhite
//     else sEmpty
//   })

  behavior of "ChiselCheckers (current: just checking moves)"


  it should "Simple white move (21->17)" in {
    test(new Mover) { dut =>
      dut.io.from.poke(21.U)
      dut.io.to.poke(17.U)
      //dut.io.boardread.poke(board)
      dut.clock.step()
      dut.io.boardwrite(17).expect(sWhite)
      dut.io.boardwrite(21).expect(sEmpty)
    }
  }


}
