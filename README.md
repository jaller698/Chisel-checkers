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

## Documentation

- **[Hardware Modules](src/main/scala/README.md)** - Overview of source code modules
- **[Test Suite](src/test/scala/README.md)** - Overview of test organization

## Getting Started

### Prerequisites

#### 1. **Java Development Kit (JDK)**: Version 8 or higher
   ```bash
   java --version
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

This produces `ChiselCheckers.sv` in the project root.

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

**Current Resource Usage** (Artix-7 xc7a100t):
- LUTs: 1557
- Flip-Flops: 407
- No DSP blocks or Block RAM used

## Architecture

### Hardware Modules

- **[`ChiselCheckers`](src/main/scala/main.scala)**: Top-level module with three operational modes (Build, Play, View)
- **[`MoveValidator2`](src/main/scala/MoveValidator2.scala)**: Core move validation implementing Checkers rules
- **[`BlackForcedMoves`](src/main/scala/blackforcedmoves.scala)** / **[`WhiteForcedMoves`](src/main/scala/whiteforcedmoves.scala)**: Detect mandatory jumps
- **[`LegalMovesForWhite`](src/main/scala/legalmovesforwhite.scala)**: Enumerate all legal moves for white
- **[`BoardEval1`](src/main/scala/boardeval1.scala)** / **[`BoardEval2`](src/main/scala/boardeval2.scala)**: Board evaluation functions
- **[`Opponent`](src/main/scala/iterator.scala)** / **[`RandAtk`](src/main/scala/RandAtk.scala)**: AI opponent implementations

See [Hardware Modules documentation](src/main/scala/README.md) for details.

### Verification Components

- **[`CheckerRules`](src/test/scala/golden_model/CheckerRules.scala)**: Golden reference model implementing Checkers rules in software. Serves as source of truth for validating hardware behavior.
- **[`CheckerRulesTest`](src/test/scala/golden_model/CheckerRulesTest.scala)**: Tests for the golden model

See [Test Suite documentation](src/test/scala/README.md) for test organization.

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

## Testing

### Test Organization

Tests are organized in [`src/test/scala/`](src/test/scala/):

- **[`unittests/`](src/test/scala/unittests/)**: Component-level tests for individual modules
- **[`integrationtests/`](src/test/scala/integrationtests/)**: System-level tests including interactive gameplay
- **[`golden_model/`](src/test/scala/golden_model/)**: Reference implementation for validation

See [Test Suite documentation](src/test/scala/README.md) for details on running tests.

## Development

### Project Structure

```
Chisel-checkers/
├── src/
│   ├── main/scala/          # Hardware modules
│   │   ├── main.scala
│   │   ├── MoveValidator2.scala
│   │   ├── blackforcedmoves.scala
│   │   ├── whiteforcedmoves.scala
│   │   ├── legalmovesforwhite.scala
│   │   ├── boardeval1.scala
│   │   ├── boardeval2.scala
│   │   ├── iterator.scala
│   │   ├── RandAtk.scala
│   │   └── README.md
│   └── test/scala/          # Test suites
│       ├── unittests/
│       ├── integrationtests/
│       ├── golden_model/
│       ├── RandAtkTest.scala
│       └── README.md
├── scripts/
│   └── synth.tcl            # Vivado synthesis script
├── build.sbt                # SBT build configuration
├── .scalafmt.conf           # Code formatting rules
└── README.md
```

### Code Formatting

This project uses [Scalafmt](https://scalameta.org/scalafmt/) for consistent code formatting. Configuration: [`.scalafmt.conf`](.scalafmt.conf).

**VS Code / Vim / Emacs / Sublime Text / Eclipse:**
- Install the [Metals extension](https://scalameta.org/metals/)
- Format: `Shift+Alt+F` (Windows/Linux) or `Shift+Option+F` (macOS)

**IntelliJ IDEA:**
- `Preferences > Editor > Code Style > Scala` → set "Formatter" to "Scalafmt"
- Format: `Opt+Cmd+L` (macOS) or `Ctrl+Alt+L` (Windows/Linux)

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

For questions or issues, please open an issue on GitHub.
