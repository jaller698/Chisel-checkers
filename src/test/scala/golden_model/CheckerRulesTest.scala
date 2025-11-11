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

  it should "reject backward move for regular white piece (16 -> 20)" in {
    val b = initialBoard.updated(20, Empty).updated(16, White)
    isMoveValid(16, 20, b) shouldBe false
  }

  it should "reject backward move for regular black piece (13 -> 8)" in {
    val b = initialBoard.updated(8, Empty).updated(13, Black)
    isMoveValid(13, 8, b) shouldBe false
  }

  it should "accept backward move for white king" in {
    val b = initialBoard.updated(20, Empty).updated(16, WhiteKing)
    isMoveValid(16, 20, b) shouldBe true
  }

  it should "accept backward move for black king" in {
    val b = initialBoard.updated(8, Empty).updated(13, BlackKing)
    isMoveValid(13, 8, b) shouldBe true
  }

  behavior of "CheckerRules.isJump"

  it should "identify a valid jump for white piece" in {
    val piece = White
    isJump(20, 13, piece) shouldBe true // 2 rows up, 2 cols over
  }

  it should "identify a valid jump for black piece" in {
    val piece = Black
    isJump(8, 17, piece) shouldBe true // 2 rows down, 2 cols over
  }

  it should "reject simple move as jump" in {
    val piece = White
    isJump(20, 16, piece) shouldBe false // only 1 row
  }

  it should "identify valid jump for white king in any direction" in {
    isJump(16, 9, WhiteKing) shouldBe true // backward jump
    isJump(10, 17, WhiteKing) shouldBe true // forward jump
  }

  behavior of "CheckerRules.isJumpValid"

  it should "accept valid jump over opponent" in {
    val b = initialBoard.updated(16, Black).updated(13, Empty)
    isJumpValid(20, 13, b) shouldBe true // white jumps black at 16
  }

  it should "reject jump over own piece" in {
    val b = initialBoard.updated(16, White).updated(13, Empty)
    isJumpValid(20, 13, b) shouldBe false // white cannot jump white
  }

  it should "reject jump over empty square" in {
    val b = initialBoard.updated(16, Empty).updated(13, Empty)
    isJumpValid(20, 13, b) shouldBe false
  }

  it should "reject jump to occupied square" in {
    val b = initialBoard.updated(16, Black).updated(13, White)
    isJumpValid(20, 13, b) shouldBe false // destination occupied
  }

  behavior of "CheckerRules.getJumpedIndex"

  it should "calculate correct jumped index for diagonal jump" in {
    getJumpedIndex(20, 13) shouldBe 16 // middle square
  }

  it should "calculate correct jumped index for reverse diagonal" in {
    getJumpedIndex(14, 21) shouldBe 17
  }

  behavior of "CheckerRules.isOpponent"

  it should "recognize white and black as opponents" in {
    isOpponent(White, Black) shouldBe true
    isOpponent(Black, White) shouldBe true
  }

  it should "recognize white king and black as opponents" in {
    isOpponent(WhiteKing, Black) shouldBe true
    isOpponent(Black, WhiteKing) shouldBe true
  }

  it should "reject same color pieces as opponents" in {
    isOpponent(White, White) shouldBe false
    isOpponent(Black, Black) shouldBe false
    isOpponent(WhiteKing, White) shouldBe false
  }

  it should "reject empty as opponent" in {
    isOpponent(White, Empty) shouldBe false
    isOpponent(Empty, Black) shouldBe false
  }

  behavior of "CheckerRules.canBecomeKing"

  it should "promote white piece reaching row 0" in {
    canBecomeKing(4, 0, White) shouldBe true
  }

  it should "promote black piece reaching row 7" in {
    canBecomeKing(27, 31, Black) shouldBe true
  }

  it should "not promote white piece not at row 0" in {
    canBecomeKing(20, 16, White) shouldBe false
  }

  it should "not promote already kinged pieces" in {
    canBecomeKing(4, 0, WhiteKing) shouldBe false
    canBecomeKing(27, 31, BlackKing) shouldBe false
  }

  behavior of "CheckerRules.hasAvailableJumps"

  it should "detect available jump for piece" in {
    val b = initialBoard.updated(16, Black).updated(13, Empty)
    hasAvailableJumps(20, b) shouldBe true
  }

  it should "return false when no jumps available" in {
    val b = initialBoard
    hasAvailableJumps(20, b) shouldBe false
  }

  behavior of "CheckerRules.isMoveValid with turn checking"

  it should "accept white move on white's turn" in {
    val b = initialBoard
    isMoveValid(20, 16, b, isWhiteTurn = true) shouldBe true
  }

  it should "reject white move on black's turn" in {
    val b = initialBoard
    isMoveValid(20, 16, b, isWhiteTurn = false) shouldBe false
  }

  it should "accept black move on black's turn" in {
    val b = initialBoard
    isMoveValid(8, 13, b, isWhiteTurn = false) shouldBe true
  }

  it should "reject black move on white's turn" in {
    val b = initialBoard
    isMoveValid(8, 13, b, isWhiteTurn = true) shouldBe false
  }

  it should "enforce mandatory jump rule" in {
    // Setup: white at 20, black at 16, empty at 13
    val b = initialBoard.updated(16, Black).updated(13, Empty)
    // White has a jump available, so simple move should be rejected
    isMoveValid(20, 17, b, isWhiteTurn = true) shouldBe false
    // But the jump should be accepted
    isMoveValid(20, 13, b, isWhiteTurn = true) shouldBe true
  }

  behavior of "CheckerRules.applyMove"

  it should "execute simple move correctly" in {
    val b = initialBoard
    val result = applyMove(20, 16, b)
    result should not be None
    result.get(20) shouldBe Empty
    result.get(16) shouldBe White
  }

  it should "execute jump and remove jumped piece" in {
    val b = initialBoard.updated(16, Black).updated(13, Empty)
    val result = applyMove(20, 13, b)
    result should not be None
    result.get(20) shouldBe Empty
    result.get(13) shouldBe White
    result.get(16) shouldBe Empty // jumped piece removed
  }

  it should "promote white piece to king at row 0" in {
    val b = Vector.fill(32)(Empty).updated(4, White).updated(0, Empty)
    val result = applyMove(4, 0, b)
    result should not be None
    result.get(0) shouldBe WhiteKing
  }

  it should "promote black piece to king at row 7" in {
    val b = Vector.fill(32)(Empty).updated(27, Black).updated(31, Empty)
    val result = applyMove(27, 31, b)
    result should not be None
    result.get(31) shouldBe BlackKing
  }

  it should "return None for invalid move from empty square" in {
    val b = initialBoard.updated(20, Empty)
    applyMove(20, 16, b) shouldBe None
  }

  behavior of "CheckerRules edge cases"

  it should "reject out of bounds indices" in {
    val b = initialBoard
    isMoveValid(-1, 16, b) shouldBe false
    isMoveValid(20, 32, b) shouldBe false
    isMoveValid(20, -1, b) shouldBe false
  }

  it should "handle multiple jumps scenario" in {
    // Setup board where white can jump multiple pieces
    val b = Vector
      .fill(32)(Empty)
      .updated(20, White)
      .updated(16, Black)
      .updated(13, Empty)
      .updated(9, Black)
      .updated(6, Empty)

    hasAvailableJumps(20, b) shouldBe true
    // First jump
    val afterFirst = applyMove(20, 13, b)
    afterFirst should not be None
    // Check if second jump is available from new position
    hasAvailableJumps(13, afterFirst.get) shouldBe true
  }
}
