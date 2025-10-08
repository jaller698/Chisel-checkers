object CheckerRules {
  sealed trait Piece {
    def isWhite: Boolean; def isBlack: Boolean; def isKing: Boolean
  }
  case object Empty extends Piece {
    val isWhite = false; val isBlack = false; val isKing = false
  }
  case object White extends Piece {
    val isWhite = true; val isBlack = false; val isKing = false
  }
  case object WhiteKing extends Piece {
    val isWhite = true; val isBlack = false; val isKing = true
  }
  case object Black extends Piece {
    val isWhite = false; val isBlack = true; val isKing = false
  }
  case object BlackKing extends Piece {
    val isWhite = false; val isBlack = true; val isKing = true
  }

  // Index corresponding to 32 dark squares
  private def row(i: Int) = i / 4
  private def col(i: Int): Int = {
    val r = row(i)
    val offset = if (r % 2 == 0) 1 else 0
    offset + 2 * (i % 4)
  }

  // Convert 8x8 board into linear 0-31 indexing
  private def idx(row: Int, col: Int): Option[Int] = {
   
    if (row < 0 || row >= 8 || col < 0 || col >= 8) None

    if((row+col)%2!=1)return None
    return Some(row*4+col/2)
  }


  case class Move(from: Int, to: Int)

  def isSimpleDiagonal(from: Int, to: Int, piece: Piece): Boolean = {
    if (from == to) return false
    val rowDelta = row(to) - row(from)
    val colDelta = math.abs(col(to) - col(from))
    if (colDelta != 1) return false
    piece match {
      case White     => rowDelta == -1
      case Black     => rowDelta == 1
      case WhiteKing => math.abs(rowDelta) == 1
      case BlackKing => math.abs(rowDelta) == 1
      case Empty     => false
    }
  }

  def isMoveValid(from: Int, to: Int, board: Vector[Piece]): Boolean = {
    if (from < 0 || from >= 32 || to < 0 || to >= 32) return false
    if (board(to) != Empty) return false
    val piece = board(from)
    if (piece == Empty) return false
    isSimpleDiagonal(from, to, piece)
  }
}
