import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class iteratortest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "opponent"

  it should "capture a king if it can" in {
    test(new Opponent()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }
      dut.io.In(22).poke("b001".U)
      dut.io.In(17).poke("b100".U)
      dut.io.statusIn.poke(true.B)
      dut.io.hastomakespecificmove.poke(false.B)
                  dut.io.specificallyfromwhere.poke(0.U)//doesnt matter because it is false

      var counter = 0
      while (dut.io.statusOut.peek().litToBoolean == false & counter < 400) {

        if (dut.io.out_counter_index.peek().litValue == 220) {
          println(s"we are now investigating ${dut.io.out_from_for_now
              .peek()
              .litValue} to ${dut.io.out_to_for_now.peek().litValue}\n")
        }
        if (dut.io.score_now.peek().litValue != -2) {
          print(counter)
          println(s"    value of board: ${dut.io.score_now.peek().litValue} ")

        }
        dut.clock.step()
        counter += 1
      }
      dut.io.statusOut.expect(true.B)

      // print(dut.io.from,dut.io.to,dut.io.stillMoving)
      dut.io.from
        .expect(22, "should move from 17 because that it the only possibility")
      dut.io.to.expect(13, "it should capture")
      dut.io.stillMoving.expect(false.B, "there isn't more to do from here")

    }
  }

  it should "capture a king rather than a pawn and still want to move" in {
    test(new Opponent()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }
            dut.io.hastomakespecificmove.poke(false.B)
                        dut.io.specificallyfromwhere.poke(0.U)//doesnt matter because it is false


      dut.io.In(9).poke("b010".U) // white king
      dut.io.In(13).poke("b100".U) // black king
      dut.io.In(5).poke("b011".U) // black pawn
      dut.io
        .In(21)
        .poke("b011".U) // another black pawn that he will want to use after.

      dut.io.statusIn.poke(true.B)
      var counter = 0
      while (dut.io.statusOut.peek().litToBoolean == false & counter < 400) {

        dut.clock.step()
        counter += 1
      }
      dut.io.statusOut.expect(true.B)

      // print(dut.io.from,dut.io.to,dut.io.stillMoving)
      dut.io.from
        .expect(9, "should move from 17 because that it the only possibility")
      dut.io.to.expect(16, "it should capture")
      dut.io.stillMoving.expect(true.B, "still wants to capture 21")

    }
  }

  it should "not try to go backwards with a white pawn" in {
    test(new Opponent()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }
      dut.io.In(15).poke("b001".U)
      dut.io.In(18).poke("b011".U)
      dut.io.statusIn.poke(true.B)
            dut.io.hastomakespecificmove.poke(false.B)
                        dut.io.specificallyfromwhere.poke(0.U)//doesnt matter because it is false


      var counter = 0
      while (dut.io.statusOut.peek().litToBoolean == false & counter < 400) {

        dut.clock.step()
        counter += 1
      }
      dut.io.statusOut.expect(true.B)

      // print(dut.io.from,dut.io.to,dut.io.stillMoving)
      dut.io.from.expect(15, "it should go from 15 because it is at 15")
      dut.io.to
        .expect(10, "it should go over 10 because 10 is found before 11.")
      dut.io.stillMoving.expect(false.B, "there isn't more to do from here")

    }
  }

  it should "do a random move" in {
    test(new Opponent()) { dut =>
      for (i <- 0 to 31) {
        dut.io.In(i).poke("b000".U)
      }
      dut.io.In(28).poke("b010".U)

      dut.io.statusIn.poke(true.B)
            dut.io.hastomakespecificmove.poke(false.B)
            dut.io.specificallyfromwhere.poke(0.U)//doesnt matter because it is false

      var counter = 0
      while (dut.io.statusOut.peek().litToBoolean == false & counter < 400) {

        dut.clock.step()
        counter += 1
      }
      dut.io.statusOut.expect(true.B)

      // print(dut.io.from,dut.io.to,dut.io.stillMoving)
      dut.io.from
        .expect(28, "should move from 17 because that it the only possibility")
      dut.io.to.expect(24, "it should capture")
      dut.io.stillMoving.expect(false.B, "there isn't more to do from here")

    }
  }

  // do a random move.

}
