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
    if ((row + col) % 2 != 1) return None
    return Some(row * 4 + col / 2)
  }

  case class Move(from: Int, to: Int)

  def isSimpleDiagonal(from: Int, to: Int, piece: Piece): Boolean = {
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

  def isJump(from: Int, to: Int, piece: Piece): Boolean = {
    val rowDelta = row(to) - row(from)
    val colDelta = math.abs(col(to) - col(from))
    if (colDelta != 2) return false
    piece match {
      case White     => rowDelta == -2
      case Black     => rowDelta == 2
      case WhiteKing => math.abs(rowDelta) == 2
      case BlackKing => math.abs(rowDelta) == 2
      case Empty     => false
    }
  }

  // Get the index of the jumped piece
  def getJumpedIndex(from: Int, to: Int): Int = {
    val fromRow = row(from)
    val fromCol = col(from)
    val toRow = row(to)
    val toCol = col(to)
    val jumpedRow = (fromRow + toRow) / 2
    val jumpedCol = (fromCol + toCol) / 2
    jumpedRow * 4 + jumpedCol / 2
  }

  // Check if the jumped piece is an opponent
  def isOpponent(piece1: Piece, piece2: Piece): Boolean = {
    (piece1.isWhite && piece2.isBlack) || (piece1.isBlack && piece2.isWhite)
  }

  // Validate a jump move
  def isJumpValid(from: Int, to: Int, board: Vector[Piece]): Boolean = {
    if (from < 0 || from >= 32 || to < 0 || to >= 32) return false
    if (from == to) return false
    if (board(to) != Empty) return false

    val piece = board(from)
    if (piece == Empty) return false
    if (!isJump(from, to, piece)) return false

    // Check if there is an opponent piece to jump
    val jumpedIdx = getJumpedIndex(from, to)
    if (jumpedIdx < 0 || jumpedIdx >= 32) return false
    val jumpedPiece = board(jumpedIdx)
    isOpponent(piece, jumpedPiece)
  }

  // Check if piece has any available jumps
  def hasAvailableJumps(from: Int, board: Vector[Piece]): Boolean = {
    val piece = board(from)
    if (piece == Empty) return false

    // Check all possible jump destinations
    (0 until 32).exists { to =>
      isJumpValid(from, to, board)
    }
  }

  // Check if any piece of given color has jumps available
  def hasAvailableJumpsForColor(board: Vector[Piece], isWhiteTurn: Boolean): Boolean = {
    (0 until 32).exists { from =>
      val piece = board(from)
      piece != Empty && piece.isWhite == isWhiteTurn && hasAvailableJumps(
        from,
        board
      )
    }
  }

  def canBecomeKing(from: Int, to: Int, piece: Piece): Boolean = {
    piece match {
      case White     => row(to) == 0
      case Black     => row(to) == 7
      case WhiteKing => false
      case BlackKing => false
      case Empty     => false
    }
  }

  // Complete move validation with turn checking and mandatory jumps
  def isMoveValid(from: Int, to: Int, board: Vector[Piece], isWhiteTurn: Boolean): Boolean = {
    if (from < 0 || from >= 32 || to < 0 || to >= 32) return false
    if (from == to) return false
    if (board(to) != Empty) return false

    val piece = board(from)
    if (piece == Empty) return false

    // Check if it's the correct player's turn
    if (piece.isWhite != isWhiteTurn) return false

    // Check if jump is available - if so, must jump
    val mustJump = hasAvailableJumpsForColor(board, isWhiteTurn)

    if (isJump(from, to, piece)) {
      isJumpValid(from, to, board)
    } else if (isSimpleDiagonal(from, to, piece)) {
      // Cannot make simple move if jump is available
      !mustJump
    } else {
      false
    }
  }

  // Overload for backward compatibility
  def isMoveValid(from: Int, to: Int, board: Vector[Piece]): Boolean = {
    if (from < 0 || from >= 32 || to < 0 || to >= 32) return false
    if (from == to) return false
    if (board(to) != Empty) return false
    val piece = board(from)
    if (piece == Empty) return false
    isSimpleDiagonal(from, to, piece)
  }

  // Apply a move and return new board state
  def applyMove(from: Int, to: Int, board: Vector[Piece]): Option[Vector[Piece]] = {
    val piece = board(from)
    if (piece == Empty) return None

    var newBoard = board.updated(from, Empty).updated(to, piece)

    // Handle jump - remove jumped piece
    if (isJump(from, to, piece)) {
      val jumpedIdx = getJumpedIndex(from, to)
      if (jumpedIdx < 0 || jumpedIdx >= 32) return None
      newBoard = newBoard.updated(jumpedIdx, Empty)
    }

    // Handle king promotion
    if (canBecomeKing(from, to, piece)) {
      val newPiece = piece match {
        case White => WhiteKing
        case Black => BlackKing
        case _     => piece
      }
      newBoard = newBoard.updated(to, newPiece)
    }

    Some(newBoard)
  }
}
