import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import CheckerRules._

class IntegrationTests extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "IntegrationTests"

  it should "initialize an empty board and check that all tiles are empty" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      dut.clock.step()

      dut.io.mode.poke("b10".U)
      for (i <- 0 to 31) {
        dut.io.from.poke(i.U)
        dut.clock.step()
      }
    }
  }

  it should "build custom board and validate moves on it, verify board hasnt changed at the end" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      dut.clock.step()

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(20.U)
      dut.io.colorToPut.poke(true.B)
      dut.clock.step()

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(10.U)
      dut.io.colorToPut.poke(false.B)
      dut.clock.step()

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(15.U)
      dut.io.colorToPut.poke(true.B)
      dut.clock.step()

      dut.io.mode.poke("b01".U)

      dut.io.from.poke(20.U)
      dut.io.to.poke(16.U)
      dut.clock.step()
      dut.io.isMoveValid
        .expect(true.B, "White piece from 20 to 16 should be valid")

      dut.io.from.poke(15.U)
      dut.io.to.poke(11.U)
      dut.clock.step()
      dut.io.isMoveValid
        .expect(true.B, "White piece from 15 to 11 should be valid")

      dut.io.from.poke(20.U)
      dut.io.to.poke(20.U)
      dut.clock.step()
      dut.io.isMoveValid
        .expect(false.B, "White piece from 20 to 20 should be invalid")

      dut.io.mode.poke("b10".U)

      dut.io.from.poke(20.U)
      dut.clock.step()

      dut.io.from.poke(10.U)
      dut.clock.step()

      dut.io.from.poke(16.U)
      dut.clock.step()

    }
  }

  it should "maintain board state when switching modes" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      dut.clock.step()

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(20.U)
      dut.io.colorToPut.poke(true.B)

      dut.io.mode.poke("b10".U)
      dut.clock.step()

      dut.io.mode.poke("b01".U)
      dut.clock.step()

      dut.io.mode.poke("b00".U)
      dut.clock.step()

      dut.io.mode.poke("b10".U)

      dut.io.from.poke(20.U)
      dut.clock.step()
    }
  }

  it should "reset board and correctly reinitialize a normal board" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      dut.clock.step()

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(15.U)
      dut.io.colorToPut.poke(true.B)
      dut.clock.step()

      dut.io.mode.poke("b10".U)
      dut.io.from.poke(15.U)
      dut.clock.step()

      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(false.B)
      dut.clock.step()

      dut.io.mode.poke("b10".U)
      for (i <- 0 to 11) {
        dut.io.from.poke(i.U)
        dut.clock.step()
      }
      for (i <- 12 to 19) {
        dut.io.from.poke(i.U)
        dut.clock.step()
      }
      for (i <- 20 to 31) {
        dut.io.from.poke(i.U)
        dut.clock.step()
      }
    }
  }

  it should "handle multiple board resets with board reconfigurations in between" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      dut.clock.step()

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(20.U)
      dut.io.colorToPut.poke(true.B)
      dut.clock.step()

      dut.io.mode.poke("b10".U)
      dut.io.from.poke(20.U)
      dut.clock.step()

      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      dut.clock.step()

      dut.io.mode.poke("b10".U)
      dut.io.from.poke(20.U)
      dut.clock.step()

      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(false.B)
      dut.clock.step()

      dut.io.mode.poke("b10".U)
      for (i <- 0 to 11) {
        dut.io.from.poke(i.U)
        dut.clock.step()
      }
      for (i <- 12 to 19) {
        dut.io.from.poke(i.U)
        dut.clock.step()
      }
      for (i <- 20 to 31) {
        dut.io.from.poke(i.U)
        dut.clock.step()
      }
    }
  }
}
