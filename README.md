# Chisel Checkers
[![CI Chisel tests](https://github.com/jaller698/RISC-V-interpreter/actions/workflows/scala.yml/badge.svg)](https://github.com/jaller698/RISC-V-interpreter/actions/workflows/scala.yml)
[![Scala format check](https://github.com/jaller698/Chisel-checkers/actions/workflows/scalafmt.yml/badge.svg)](https://github.com/jaller698/Chisel-checkers/actions/workflows/scalafmt.yml)

A hardware implementation of the classic game of Checkers (Draughts) written in [Chisel](https://www.chisel-lang.org/), a hardware construction language embedded in Scala. This project demonstrates digital logic design principles by implementing game logic, move validation, and board state management entirely in hardware.

## Project Purpose

This project implements a complete Checkers game engine in hardware, capable of:
- Managing an 8×8 checkerboard with 32 playable dark squares
- Validating moves according to official Checkers rules
- Enforcing mandatory jump rules
- Promoting pieces to kings when they reach the opposite end
- Supporting both single moves and jump captures
- Providing multiple operational modes for setup, gameplay, and board inspection

The design targets FPGA implementation and has been synthesized for the Xilinx Artix-7 (Nexys 4 DDR board).

## Getting Started

### Prerequisites

#### 1. **Java Development Kit (JDK)**: Version 8 or higher
   ```bash
   java -version
   ```

#### 2. **Scala Build Tool (sbt)**: Version 1.11.7 or compatible

   **Check your current version:**
   ```bash
   sbt --version
   ```

   **If sbt is not installed or you need to update it:**

   **Linux (Debian/Ubuntu):**
   ```bash
   echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | sudo tee /etc/apt/sources.list.d/sbt.list
   echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | sudo tee /etc/apt/sources.list.d/sbt_old.list
   curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | sudo tee /etc/apt/trusted.gpg.d/sbt.asc
   sudo apt-get update
   sudo apt-get install sbt
   ```

   **Linux (RPM-based - Fedora/RHEL/CentOS):**
   ```bash
   # Remove old Bintray repo file if it exists
   sudo rm -f /etc/yum.repos.d/bintray-rpm.repo || true
   curl -L https://www.scala-sbt.org/sbt-rpm.repo > sbt-rpm.repo
   sudo mv sbt-rpm.repo /etc/yum.repos.d/
   sudo yum install sbt
   ```

   **macOS:**
   ```bash
   brew install sbt
   ```

   **Windows:**
   ```powershell
   # Option 1 - MSI Installer (recommended):
   # Download from: https://www.scala-sbt.org/download.html
   # Run the .msi installer and follow the installation wizard

   # Option 2 - Chocolatey package manager:
   choco install sbt

   # Option 3 - Scoop package manager:
   scoop install sbt
   ```

   For more installation options and detailed instructions, visit: https://www.scala-sbt.org/download.html

#### 3. **Chisel Dependencies**: Automatically managed by sbt (see [build.sbt](build.sbt))
   - Chisel 6.7.0
   - ChiselTest 6.0.0

#### 4. **Optional: Vivado** (for FPGA synthesis)
   - Xilinx Vivado 2025.1 or compatible version

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/jaller698/Chisel-checkers.git
   cd Chisel-checkers
   ```

2. Compile the project:
   ```bash
   sbt compile
   ```

3. Run tests:
   ```bash
   sbt test
   ```

## Usage

### Running Tests

The project includes comprehensive unit tests and integration tests:

```bash
# Run all tests
sbt test

# Run specific test suite
sbt "testOnly *ValidMoveTest"
sbt "testOnly *IntegrationTests"
sbt "testOnly *movevalidatortest"
```

### Generating Verilog

Generate synthesizable Verilog/SystemVerilog output:

```bash
sbt run
```

This produces [`ChiselCheckers.sv`](ChiselCheckers.sv) in the project root.

### Playing the Game

Run the interactive test to manually play the game:

```bash
sbt "testOnly PlayerIOTest"
```

This allows you to:
- View the current board state
- Enter moves (from/to positions 0-31)
- Have the hardware play against you
- See move validation results in real-time

### FPGA Synthesis

Synthesize the design for Xilinx FPGAs using the provided Tcl script:

```bash
vivado -mode batch -source scripts/synth.tcl
```

Results will be in the [`vivado_build/`](vivado_build/) directory, including:
- Utilization report: [`ChiselCheckers_synth_utilization.txt`](vivado_build/ChiselCheckers_synth_utilization.txt)
- Design checkpoint: `ChiselCheckers_synth.dcp`

**Current Resource Usage** (Artix-7 xc7a100t):
- LUTs: 622
- Flip-Flops: 102
- No DSP blocks or Block RAM used

## Architecture

#### Hardware Modules

- **[`ChiselCheckers`](src/main/scala/main.scala)**: Top-level module coordinating all game functionality
- **[`MoveValidator2`](src/main/scala/MoveValidator2.scala)**: Core logic for validating moves according to Checkers rules (current implementation)
- **[`MoveValidator`](src/main/scala/MoveValidator.scala)**: Earlier version of move validation logic
- **[`BlackForcedMoves`](src/main/scala/blackforcedmoves.scala)** / **[`WhiteForcedMoves`](src/main/scala/whiteforcedmoves.scala)**: Detect mandatory jump situations
- **[`LegalMovesForWhite`](src/main/scala/legalmovesforwhite.scala)**: Enumerate all legal moves for white pieces
- **[`BoardEval1`](src/main/scala/boardeval1.scala)**: Simple board evaluation function (material count)
- **[`Iterator`](src/main/scala/iterator.scala)**: AI opponent implementation (work in progress)

#### Verification Components

- **[`CheckerRules`](src/test/scala/golden_model/CheckerRules.scala)**: Golden reference model implementing Checkers rules in software. This serves as the source of truth for validating hardware behavior, ensuring the hardware implementation matches expected game logic.
- **[`CheckerRulesTest`](src/test/scala/golden_model/CheckerRulesTest.scala)**: Comprehensive tests for the golden model itself

### Piece Encoding

Pieces are encoded as 3-bit values:
- `000` (0): Empty square
- `001` (1): White pawn
- `010` (2): White king
- `011` (3): Black pawn
- `100` (4): Black king

### Board Representation

The 32 dark squares of a standard checkerboard are indexed 0-31:
```
    0  1  2  3  4  5  6  7
   -------------------
0 |    0     1     2     3  |
1 |  4     5     6     7    |
2 |    8     9    10    11  |
3 | 12    13    14    15    |
4 |   16    17    18    19  |
5 | 20    21    22    23    |
6 |   24    25    26    27  |
7 | 28    29    30    31    |
   -------------------
```

## Game Rules Implemented

The hardware enforces official Checkers rules:

1. **Movement**:
   - Pawns move diagonally forward one square
   - Kings move diagonally forward or backward one square
   - All moves must be to empty squares

2. **Captures (Jumps)**:
   - Jump over opponent's piece to empty square beyond
   - Jumped piece is removed from the board
   - Multiple consecutive jumps possible (not yet fully implemented)

3. **Mandatory Jumps**:
   - If a jump is available, player must take it
   - Simple moves are invalid when jumps are possible
   - Enforced by [`BlackForcedMoves`](src/main/scala/blackforcedmoves.scala) and [`WhiteForcedMoves`](src/main/scala/whiteforcedmoves.scala)

4. **King Promotion**:
   - White pawn reaching row 0 becomes white king
   - Black pawn reaching row 7 becomes black king

<!-- ## Operational Modes

The [`ChiselCheckers`](src/main/scala/main.scala) module operates in three modes, controlled by the `mode` input:

### Mode 00: Build Board

Configure the initial board state:
- **Reset**: Assert `reset` high with `resetEmpty`:
  - `resetEmpty = false`: Initialize standard starting position
  - `resetEmpty = true`: Clear board to empty
- **Place Pieces**: After reset, use `placePiece` (position) and `colorToPut` (0=black, 1=white) to manually place pieces

### Mode 01: Play Move

Validate and execute moves:
- Set `from` and `to` positions (0-31)
- Read `isMoveValid` output:
  - `true`: Move is legal and has been applied
  - `false`: Move is illegal, board unchanged
- The validator enforces:
  - Correct piece colors
  - Valid diagonal movements
  - Mandatory jump rules
  - King promotion at end rows

### Mode 10: View Board

Inspect board state:
- Set `from` to the position to query (0-31)
- Read `colorAtTile` output (3-bit piece encoding) -->

## Testing

### Test Structure

Tests are organized in [`src/test/scala/`](src/test/scala/):

- **[`unittests/`](src/test/scala/unittests/)**: Component-level tests
  - [`movevalidatortest.scala`](src/test/scala/unittests/movevalidatortest.scala): Move validation logic
  - [`blackforcedmovestest.scala`](src/test/scala/unittests/blackforcedmovestest.scala): Jump detection for black
  - [`whiteforcedmovestest.scala`](src/test/scala/unittests/whiteforcedmovestest.scala): Jump detection for white
  - [`legalmovesforwhitetest.scala`](src/test/scala/unittests/legalmovesforwhitetest.scala): Legal move enumeration

- **[`integrationtests/`](src/test/scala/integrationtests/)**: System-level tests
  - [`IntegrationTests.scala`](src/test/scala/integrationtests/IntegrationTests.scala): Multi-mode workflows
  - [`ValidMoveTest.scala`](src/test/scala/integrationtests/ValidMoveTest.scala): Comprehensive move validation
  - [`play_test.scala`](src/test/scala/integrationtests/play_test.scala): Interactive gameplay

- **[`golden_model/`](src/test/scala/golden_model/)**: Reference implementation
  - [`CheckerRules.scala`](src/test/scala/golden_model/CheckerRules.scala): Software reference for validation
  - [`CheckerRulesTest.scala`](src/test/scala/golden_model/CheckerRulesTest.scala): Reference model tests

## Development

### Project Structure

```
Chisel-checkers/
├── src/
│   ├── main/scala/          # Hardware modules
│   │   ├── main.scala       # Top-level ChiselCheckers
│   │   ├── MoveValidator.scala
│   │   ├── MoveValidator2.scala
│   │   ├── blackforcedmoves.scala
│   │   ├── whiteforcedmoves.scala
│   │   ├── legalmovesforwhite.scala
│   │   ├── boardeval1.scala
│   │   └── iterator.scala   # (Work in progress)
│   └── test/scala/          # Test suites
│       ├── unittests/
│       ├── integrationtests/
│       └── golden_model/
├── scripts/
│   └── synth.tcl            # Vivado synthesis script
├── build.sbt                # SBT build configuration
├── .scalafmt.conf           # Code formatting rules
└── README.md
```

## Editor Setup

### Code Formatting

This project uses [Scalafmt](https://scalameta.org/scalafmt/) for consistent code formatting. The configuration is in [`.scalafmt.conf`](.scalafmt.conf).

### VS Code / Vim / Emacs / Sublime Text / Eclipse

These editors use the [Metals language server](https://scalameta.org/metals/) for Scala support:

**VS Code:**
1. Install the [Metals extension](https://marketplace.visualstudio.com/items?itemName=scalameta.metals)
2. Open the project - Metals will automatically detect `.scalafmt.conf`
3. Format: `Shift+Alt+F` (Windows/Linux) or `Shift+Option+F` (macOS)

**Other editors:** See [Metals installation guide](https://scalameta.org/metals/docs/) for setup instructions.

### IntelliJ IDEA

The Scala plugin has built-in Scalafmt support:

1. Open the project - you'll be prompted to use Scalafmt
2. Or manually: `Preferences > Editor > Code Style > Scala` → set "Formatter" to "Scalafmt"
3. Format: `Opt+Cmd+L` (macOS) or `Ctrl+Alt+L` (Windows/Linux)

**Enable format on save:** `Preferences > Editor > Code Style > Scala` → check "Reformat on file save"

<!-- ## Contributing

Contributions are welcome! Areas for improvement:

- [ ] Multi-jump sequence support
- [ ] Complete AI opponent ([`iterator.scala`](src/main/scala/iterator.scala) in progress)
- [ ] Advanced board evaluation functions
- [ ] UART/external interface for hardware testing
- [ ] Timing optimization for higher clock frequencies
- [ ] Support for different board variants

Please ensure:
1. All tests pass: `sbt test`
2. Code is formatted: `sbt scalafmtAll`
3. New features include tests -->

## 📄 License

This project is licensed under the **GNU General Public License v3.0** - see the [LICENSE](LICENSE) file for details.

### Key Points:
- ✅ Free to use, modify, and distribute
- ✅ Source code must be made available
- ✅ Derivative works must use the same license
- ⚠️ No warranty provided

For more information about GPL-3.0, visit: https://www.gnu.org/licenses/gpl-3.0.html

## References

- [Chisel Documentation](https://www.chisel-lang.org/)
- [ChiselTest Documentation](https://github.com/ucb-bar/chiseltest)
- [Checkers Rules](https://www.usacheckers.com/rules-of-checkers/)
- [Nexys 4 DDR Reference Manual](https://digilent.com/reference/programmable-logic/nexys-4-ddr/reference-manual)

## Acknowledgments

- Built with [Chisel](https://www.chisel-lang.org/), UC Berkeley's hardware construction language
- Synthesized using Xilinx Vivado
- Tested with ScalaTest and ChiselTest frameworks

---

<!-- **Status**: ✅ Core functionality complete | 🚧 AI opponent in development -->

For questions or issues, please open an issue on GitHub.
