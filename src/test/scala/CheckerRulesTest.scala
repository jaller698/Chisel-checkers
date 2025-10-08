import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import CheckerRules._

class CheckerRulesTest extends AnyFlatSpec with Matchers {
  private def initialBoard: Vector[Piece] =
    Vector.tabulate(32) { i =>
      if (i < 12) Black
      else if (i >= 20) White
      else Empty
    }

  behavior of "CheckerRules.isMoveValid"

  it should "accept a simple forward-right white move into empty square (20 -> 16)" in {
    val b = initialBoard
    isMoveValid(20, 16, b) shouldBe true // row 5 col 0 -> row 4 col 1
  }

  it should "accept a simple forward-right black move into empty square (8 -> 13)" in {
    val b = initialBoard
    isMoveValid(8, 13, b) shouldBe true // row 2 col 1 -> row 3 col 2
  }

  it should "reject same-square move (20 -> 20)" in {
    val b = initialBoard
    isMoveValid(20, 20, b) shouldBe false
  }

  it should "reject move onto occupied destination (20 -> 21 both white)" in {
    val b = initialBoard
    isMoveValid(20, 21, b) shouldBe false
  }

  it should "accept a forward-left white move (21 -> 16)" in {
    val b = initialBoard
    isMoveValid(21, 16, b) shouldBe true
  }
}
