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

  private def getBoardFromDut(
      dut: ChiselCheckers
  ): Vector[Piece] = {

    dut.io.ack.poke(false.B)
    dut.io.op.poke("b10".U)
    dut.clock.step()
    Vector.tabulate(32) { i =>
      dut.io.from.poke(i.U)
      dut.clock.step(1)
      while (!dut.io.valid.peek().litToBoolean) { dut.clock.step(1) }
      val color = dut.io.colorAtTile.peek().litValue.toInt
      dut.io.ack.poke(true.B)
      dut.clock.step(1)
      dut.io.ack.poke(false.B)
      color match {
        case 0 => Empty
        case 1 => White
        case 2 => WhiteKing
        case 3 => Black
        case 4 => BlackKing
      }
    }
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
      val fromSet = Seq(
        8,
        9,
        10
      ) // TODO: We need to add a check for forced moves here, but now this set should not have any.
      dut.io.op.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(false.B)
      dut.clock.step()
      while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
      dut.io.ack.poke(true.B)
      dut.clock.step()
      dut.io.ack.poke(false.B)

      for (from <- fromSet; to <- 12 until 32) {
        // Always get the current board from the DUT
        val b = getBoardFromDut(dut)
        val ref = isMoveValid(from, to, b)
        // Set up move
        dut.io.from.poke(from.U)
        dut.io.to.poke(to.U)
        dut.io.op.poke(1.U) // play op
        while (!dut.io.valid.peek().litToBoolean) { dut.clock.step() }
        if (ref) {
          dut.io.isMoveValid
            .expect(true.B, s"from $from to $to should be valid")
          // Apply move in DUT
          dut.io.ack.poke(true.B)
          dut.clock.step()
          // Wait for valid to go low (move applied)
          while (dut.io.valid.peek().litToBoolean) { dut.clock.step() }
          dut.io.ack.poke(false.B)
        } else {
          dut.io.isMoveValid
            .expect(false.B, s"from $from to $to should be invalid")
          dut.io.ack.poke(true.B)
          dut.clock.step()
          while (dut.io.valid.peek().litToBoolean) { dut.clock.step() }
          dut.io.ack.poke(false.B)
        }
      }
    }
  }
}
