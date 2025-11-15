
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class whiteforcedmovestest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "whiteforcedmoves"

  it should "not have forced moves on an empty board" in {
    test(new WhiteForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.out.expect(
        false.B,
        "there shouldn't be forced moves on an empty board"
      )

    }
  }

  it should "force a simple jump up" in {
    test(new WhiteForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.In(27).poke("b001".U)//white pawn
      dut.io.In(23).poke("b100".U)//black king

      dut.io.out.expect(
        true.B,
        "there shouldn't be forced moves on an empty board"
      )

    }
  }

   it should "not force a simple jump down for white pawn" in {
    test(new WhiteForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.In(21).poke("b001".U)
      dut.io.In(24).poke("b011".U)


      dut.io.out.expect(
        false.B,
        "there shouldn't be forced moves on an empty board"
      )

    }
  }


   it should "not force a jump up for white king if something is on the other side" in {
    test(new WhiteForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }
      dut.io.In(17).poke("b010".U)
      dut.io.In(14).poke("b011".U)
      dut.io.In(10).poke("b001".U)

      dut.io.out.expect(
        false.B,
        "there shouldn't be forced moves on an empty board"
      )

    }
  }

   it should "force a jump down for white king" in {
    test(new WhiteForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.In(5).poke("b010".U)
      dut.io.In(8).poke("b100".U)


      dut.io.out.expect(
        true.B,
        "there shouldn't be forced moves on an empty board"
      )

    }
  }

   it should "not force anything for a bunch of random pieces in specific rows" in {
    test(new WhiteForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

    dut.io.In(0).poke("b010".U)
      dut.io.In(1).poke("b100".U)
      dut.io.In(2).poke("b010".U)
      dut.io.In(3).poke("b100".U)

    dut.io.In(28).poke("b010".U)
      dut.io.In(29).poke("b100".U)
      dut.io.In(30).poke("b010".U)
      dut.io.In(31).poke("b100".U)




      dut.io.out.expect(
        false.B,
        "there shouldn't be forced moves on an empty board"
      )

    }
  }


  
  }

