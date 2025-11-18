import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import CheckerRules._

class ValidMoveTest extends AnyFlatSpec with ChiselScalatestTester {

  private def initialBoard: Vector[Piece] =
    Vector.tabulate(32) { i =>
      if (i < 12) Black
      else if (i >= 20) White
      else Empty
    }

  behavior of "ChiselCheckers"

  it should "pass" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      dut.io.from.poke(20.U)
      dut.io.to.poke(20.U)
      dut.clock.step()
      while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
      dut.io.isMoveValid.expect(false.B)
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)
    }
  }

  it should "initialize empty board and check that all tiles are empty" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      // sets up an empty board
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      dut.clock.step()
      while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)
      // checks that whole board is empty
      dut.io.op.poke("b10".U)
      for (i <- 0 to 31) {
        dut.io.from.poke(i.U)
        dut.clock.step()
        while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
        dut.io.colorAtTile.expect(0.U, s" $i should be 0, which is empty.")
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
    }
  }

  it should "initialize start board and see that the tiles are at the right places" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      // sets up an empty board
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(
        false.B
      ) // resetEmpty= false means that we set the standard board.
      dut.clock.step()
      while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)
      // checks that whole board is empty
      dut.io.op.poke("b10".U)
      for (i <- 0 to 31) {
        dut.io.from.poke(i.U)
        dut.clock.step()
        while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
        if (i < 12)
          dut.io.colorAtTile
            .expect(3.U, s" $i should be 3, which I assume is black normal")
        else if (i < 20)
          dut.io.colorAtTile
            .expect(0.U, s" $i should be 0, which I assume is empty")
        else
          dut.io.colorAtTile
            .expect(1.U, s" $i should be 1, which I assume is white")
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.ack.poke(false.B)
      }
    }
  }

  it should "Single move test" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.ack.poke(false.B)
      val b = initialBoard
      val from = 18
      val to = 14
      dut.io.op.poke("b00".U)
      dut.io.from.poke(18.U)
      dut.io.to.poke(14.U)
      // strictly unimportant but important to include:
      dut.io.reset.poke(false.B)
      dut.io.resetEmpty.poke(false.B)
      dut.io.placePiece.poke(0.U)
      dut.io.colorToPut.poke(false.B)
      dut.clock.step()
      while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
      val ref = isMoveValid(from, to, b)
      if (ref)
        dut.io.isMoveValid
          .expect(true.B, s"from $from to $to should be valid")
      else
        dut.io.isMoveValid
          .expect(false.B, s"from $from to $to should be invalid")
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)
    }
  }

  it should "pass movement test black" in {
    test(new ChiselCheckers()).withAnnotations(
      Seq( /* VerilatorBackendAnnotation, */ WriteVcdAnnotation)
    ) { dut =>
      dut.io.ack.poke(false.B)
      var b = initialBoard
      val fromSet = Seq(8, 9, 10, 11)
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(false.B)
      dut.clock.step()
      while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)
      dut.io.op.poke("b01".U)
      for (from <- fromSet; to <- 0 until 32) {
        dut.io.from.poke(from.U)
        dut.io.to.poke(to.U)
        dut.clock.step()
        while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
        val ref = isMoveValid(from, to, b)
        if (ref) {
          dut.io.isMoveValid
            .expect(true.B, s"from $from to $to should be valid")
          // update board if move is valid
          b = applyMove(from, to, b).get
        } else
          dut.io.isMoveValid
            .expect(false.B, s"from $from to $to should be invalid")
        dut.io.ack.poke(true.B)
        dut.clock.step()
        dut.io.valid.expect(false.B)
        dut.io.ack.poke(false.B)
      }
    }
  }
}
