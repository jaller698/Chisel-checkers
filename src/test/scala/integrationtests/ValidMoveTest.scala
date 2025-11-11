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

  behavior of "ChiselCheckers (current: isMoveValid always false)"

  it should "pass" in {
    test(new ChiselCheckers()) { dut =>
      dut.io.from.poke(20.U)
      dut.io.to.poke(20.U)
      dut.clock.step()
      dut.io.isMoveValid.expect(false.B)
    }
  }

  it should "initialize empty board and check that all tiles are empty" in {
    test(new ChiselCheckers()) { dut =>
      // sets up an empty board
      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(true.B)
      dut.clock.step()

      // checks that whole board is empty
      dut.io.mode.poke("b10".U)
      for (i <- 0 to 31) {

        dut.io.from.poke(
          i.U
        ) // not sure if this should happen on the same cycle.

        dut.clock.step()
        dut.io.colorAtTile.expect(0.U, s" $i should be 0, which is empty.")
      }

    }
  }

  it should "initialize start board and see that the tiles are at the right places" in {
    test(new ChiselCheckers()) { dut =>
      // sets up an empty board
      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(
        false.B
      ) // resetEmpty= false means that we set the standard board.
      dut.clock.step()

      // checks that whole board is empty
      dut.io.mode.poke("b10".U)
      for (i <- 0 to 31) {

        dut.io.from.poke(
          i.U
        ) // not sure if this should happen on the same cycle.

        dut.clock.step()
        if (i < 12)
          dut.io.colorAtTile
            .expect(3.U, s" $i should be 3, which I assume is black normal")
        else if (i < 20)
          dut.io.colorAtTile
            .expect(0.U, s" $i should be 0, which I assume is empty")
        else
          dut.io.colorAtTile
            .expect(1.U, s" $i should be 1, which I assume is white")

      }

    }
  }

  it should "Single move test" in {
    test(new ChiselCheckers()) { dut =>
      val b = initialBoard
      // val fromSet = Seq(18)

      val from = 18
      val to = 14

      dut.io.mode.poke("b00".U)
      dut.io.from.poke(18.U)
      dut.io.to.poke(14.U)

      // strictly unimportant but important to include:
      dut.io.reset.poke(false.B)
      dut.io.resetEmpty.poke(false.B)
      dut.io.placePiece.poke(0.U)
      dut.io.colorToPut.poke(false.B)
      // dut.io.colorAtTile.poke(0.U)

      dut.clock.step()

      val ref = isMoveValid(from, to, b)
      if (ref)
        dut.io.isMoveValid
          .expect(true.B, s"from $from to $to should be valid")
      else
        dut.io.isMoveValid
          .expect(false.B, s"from $from to $to should be invalid")
    }
  }

  it should "pass movement test black" in {
    test(new ChiselCheckers()) { dut =>
      val b = initialBoard
      val fromSet = Seq(
        8,
        9,
        10,
        11
      )
      dut.io.mode.poke("b00".U)
      dut.io.reset.poke(true.B)
      dut.io.resetEmpty.poke(false.B)
      dut.clock.step()
      dut.io.mode.poke("b01".U)
      for (from <- fromSet; to <- 0 until 32) {
        dut.io.from.poke(from.U)
        dut.io.to.poke(to.U)
        // dut.clock.step()
        // do not step here: isMoveValid is combinational and stepping would apply the move to the board
        // which changes the board for subsequent iterations and invalidates comparisons to `b`
        // if you want to apply moves, update `b` accordingly or reset the DUT per test case.
        val ref = isMoveValid(from, to, b)
        if (ref)
          dut.io.isMoveValid
            .expect(true.B, s"from $from to $to should be valid")
        else
          dut.io.isMoveValid
            .expect(false.B, s"from $from to $to should be invalid")
      }

    }
  }
}
