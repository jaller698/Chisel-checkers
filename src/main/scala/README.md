# Hardware Modules

This directory contains the Chisel hardware implementation of the Checkers game engine.

## Core Modules

### Game Engine
- **[`main.scala`](main.scala)** - Top-level `ChiselCheckers` module with three operational modes (Build, Play, View) and handshake protocol
- **[`MoveValidator2.scala`](MoveValidator2.scala)** - Core move validation logic: diagonal moves, jumps, captures, king promotion

### Move Detection
- **[`BlackForcedMoves.scala`](blackforcedmoves.scala)** - Detects mandatory jumps for black pieces
- **[`WhiteForcedMoves.scala`](whiteforcedmoves.scala)** - Detects mandatory jumps for white pieces
- **[`LegalMovesForWhite.scala`](legalmovesforwhite.scala)** - Enumerates all legal moves for white (128-bit output: 32 positions × 4 directions)

### Board Evaluation
- **[`boardeval1.scala`](boardeval1.scala)** - Basic material scoring (pawn=±1, king=±3)
- **[`boardeval2.scala`](boardeval2.scala)** - Enhanced evaluation with positional bonuses for pieces near promotion

### AI Components (Work in Progress)
- **[`iterator.scala`](iterator.scala)** - Minimax-style opponent using move iteration and board evaluation
- **[`RandAtk.scala`](RandAtk.scala)** - Attack-focused move generation with random selection

## Board Representation

32 dark squares indexed 0-31:
```
    0  1  2  3  4  5  6  7
0 |    0     1     2     3  |
1 |  4     5     6     7    |
2 |    8     9    10    11  |
3 | 12    13    14    15    |
4 |   16    17    18    19  |
5 | 20    21    22    23    |
6 |   24    25    26    27  |
7 | 28    29    30    31    |
```

## Piece Encoding

```scala
// 000: Empty, 001: White, 010: White King, 011: Black, 100: Black King
val sEmpty :: sWhite :: sWhiteKing :: sBlack :: sBlackKing :: Nil = Enum(5)
```

## Design Patterns

- **State Machines**: `RegInit` registers with `switch/is` statements
- **Handshake Protocol**: `valid`/`ack` signals for synchronization
- **Parallel Hardware**: Scala `for` loops unroll into parallel checking circuits

---

See [Test Documentation](../../test/scala/README.md) for testing information.