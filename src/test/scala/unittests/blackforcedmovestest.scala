import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class blackforcedmovestest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "blackforcedmoves"

  it should "not have forced moves on an empty board" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.out.expect(
        false.B,
        "there shouldn't be forced moves on an empty board"
      )

    }
  }

  it should "have a forcedmove with a black pawn on 22 and a white pawn on 26" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.In(i).poke("b011".U)
        } else if (i == 26) {
          dut.io.In(i).poke("b001".U)
        } else {
          dut.io.In(i).poke("b000".U)
        }
      }

      dut.io.out.expect(
        true.B,
        "should be a forced move here!"
      )

    }
  }

  it should "force black king to jump up over white king" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
      dut.io.In(i).poke("b000".U)

    }
      dut.io.In(18).poke("b100".U)
      dut.io.In(14).poke("b010".U)
      dut.io.out.expect(
        true.B,
        "should be a forced move here!"
      )

  }

  }


  it should "force king to jump down over pawn" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }
      dut.io.In(10).poke("b100".U)
      dut.io.In(15).poke("b001".U)

      dut.io.In(5).poke("b010".U)//putting a white king in the game for random fun. 

      dut.io.out.expect(
        true.B,
        "should be a forced move here!"
      )

    }
  }

  it should "force pawn to kill in the right direction" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }

      dut.io.In(1).poke("b011".U)
      dut.io.In(5).poke("b010".U)

      dut.io.out.expect(
        true.B,
        "should be a forced move here!"
      )

    }
  }

  it should "not force pawn to jump in the wrong direction" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
      
          dut.io.In(i).poke("b000".U)
      }

      dut.io.In(31).poke("b011".U)
      dut.io.In(26).poke("b001".U)

      dut.io.out.expect(
        false.B,
        "should be a forced move here!"
      )

    }
  }

  it should "not force to jump when no one is affected" in {
    test(new BlackForcedMoves()) { dut =>
      for (i <- 0 to 31) {
        
          dut.io.In(i).poke("b000".U)
        
      }
      //putting a few pieces that are disconnected from eachother. 
      dut.io.In(4).poke("b010".U)
      dut.io.In(6).poke("b001".U)
      dut.io.In(18).poke("b011".U)
      dut.io.In(14).poke("b100".U)

      dut.io.out.expect(
        false.B,
        "should be a forced move here!"
      )

    }
  }

  }

