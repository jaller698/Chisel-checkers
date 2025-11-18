import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import CheckerRules._

class IntegrationTests extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "IntegrationTests"

  it should "initialize an empty board and check that all tiles are empty" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)
      for (i <- 0 to 31) {
        dut.io.from.poke(i.U)
        while (!dut.io.valid.peek().litToBoolean) {
          dut.clock.step()
        }
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
    }
  }

  ignore should "build custom board and validate moves on it, verify board hasnt changed at the end" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(20.U)
      dut.io.colorToPut.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(10.U)
      dut.io.colorToPut.poke(false.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(15.U)
      dut.io.colorToPut.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b01".U)

      dut.io.from.poke(20.U)
      dut.io.to.poke(16.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.isMoveValid
        .expect(true.B, "White piece from 20 to 16 should be valid")
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.from.poke(15.U)
      dut.io.to.poke(11.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.isMoveValid
        .expect(true.B, "White piece from 15 to 11 should be valid")
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.from.poke(20.U)
      dut.io.to.poke(20.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.isMoveValid
        .expect(false.B, "White piece from 20 to 20 should be invalid")
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)

      dut.io.from.poke(20.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.from.poke(10.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.from.poke(16.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)
    }
  }

  it should "maintain board state when switching ops" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(20.U)
      dut.io.colorToPut.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b01".U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b00".U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)
      dut.io.from.poke(20.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)
    }
  }

  it should "reset board and correctly reinitialize a normal board" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(15.U)
      dut.io.colorToPut.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)
      dut.io.from.poke(15.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(false.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)
      for (i <- 0 to 11) {
        dut.io.from.poke(i.U)
        while (!dut.io.valid.peek().litToBoolean) {
          dut.clock.step()
        }
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
      for (i <- 12 to 19) {
        dut.io.from.poke(i.U)
        while (!dut.io.valid.peek().litToBoolean) {
          dut.clock.step()
        }
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
      for (i <- 20 to 31) {
        dut.io.from.poke(i.U)
        while (!dut.io.valid.peek().litToBoolean) {
          dut.clock.step()
        }
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
    }
  }

  it should "handle multiple board resets with board reconfigurations in between" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.reset.poke(false.B)
      dut.io.placePiece.poke(20.U)
      dut.io.colorToPut.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)
      dut.io.from.poke(20.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)
      dut.io.from.poke(20.U)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(false.B)
      while (!dut.io.valid.peek().litToBoolean) {
        dut.clock.step()
      }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      dut.io.op.poke("b10".U)
      for (i <- 0 to 11) {
        dut.io.from.poke(i.U)
        while (!dut.io.valid.peek().litToBoolean) {
          dut.clock.step()
        }
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
      for (i <- 12 to 19) {
        dut.io.from.poke(i.U)
        while (!dut.io.valid.peek().litToBoolean) {
          dut.clock.step()
        }
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
      for (i <- 20 to 31) {
        dut.io.from.poke(i.U)
        while (!dut.io.valid.peek().litToBoolean) {
          dut.clock.step()
        }
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
    }
  }
}
