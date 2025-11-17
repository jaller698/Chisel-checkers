import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class movevalidatortest
    extends AnyFlatSpec
    with ChiselScalatestTester {

  behavior of "move validator"

  it should "say that this simple move is a valid move and check that the board is correct after" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }
      dut.io.color.poke(0.U)
      

      dut.io.from.poke(22)
      dut.io.to.poke(26)

      dut.io.ValidMove.expect(
        true.B,
        s"going from 22 to 26 with black should be valid"
      )

      for (i <- 0 to 31) {
        if (i == 26) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 26")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "not be able to go up" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 17) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

          dut.io.color.poke(0.U)


      dut.io.from.poke(17)
      // 17 is just a random place in the middle of the board. Don't think so deeply about it.
      for (i <- 0 to 15) {
        dut.io.to.poke(i)
        dut.io.ValidMove.expect(
          false.B,
          s"going from 17 upwards to $i with black should be invalid"
        )

      }

      for (i <- 0 to 31) {
        if (i == 17) {
          dut.io.newboard(i).expect("b011".U, "the piece should still be at 16")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "not be able to walk when it can jump" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else if (i == 26) {
          dut.io.board(i).poke("b001".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }

            dut.io.color.poke(0.U)


      dut.io.from.poke(22)
      dut.io.to.poke(25)
      // 17 is just a random place in the middle of the board. Don't think so deeply about it.

      dut.io.ValidMove.expect(
        false.B,
        s"going from 22 to 25 should be invalid because there is a forcedmove"
      )

      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.newboard(i).expect("b011".U, "22 should still be black.")
        } else if (i == 26) {
          dut.io.newboard(i).expect("b001".U, "26 should still be white")
        } else {
          dut.io.newboard(i).expect("b000".U, s"$i should be empty")
        }
      }

      dut.io.from.poke(22)
      dut.io.to.poke(31)

      dut.io.ValidMove.expect(true.B, "going from 22 to 31 should be allowed")

      for (i <- 0 to 31) {
        if (i == 31) {
          dut.io.newboard(i).expect("b011".U, "31 should be black")
        } else {
          dut.io.newboard(i).expect("b000".U, s"$i should be empty")
        }
      }

    }
  }

  it should "invalidate the incorrect move and not change the board" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 22) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }
            dut.io.color.poke(0.U)


      dut.io.from.poke(20)
      dut.io.to.poke(26)

      dut.io.ValidMove.expect(
        false.B,
        s"it should be invalidated"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 22) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have stayed at 22")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "put black pieces on 8,9,10,11 and try to move from 8 to 19 and fail" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
  
          dut.io.board(i).poke("b000".U)
        
      }
      dut.io.board(8).poke("b011".U)
      //dut.io.board(9).poke("b011".U)
      //dut.io.board(10).poke("b011".U)
      //dut.io.board(11).poke("b011".U)
      dut.io.color.poke(0.U)


      dut.io.color.poke(0.U)
      dut.io.from.poke(8)
      dut.io.to.poke(19)
      dut.clock.step()
      dut.io.out_difference.expect(11.S, "the difference should be 11, which is bad")
      dut.io.out_forcedmoves.expect(false.B, "we shouldnt be forced to make a move!")
      dut.clock.step()
      dut.io.out_validDifference.expect(false.B, "We shouldn't be allowed to go from 8 to 19 ever")
      dut.io.ValidMove.expect(
        false.B,
        s"it should be invalidated"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 8) {
          dut.io
            .newboard(i)
            .expect("b011".U, s" $i should still contain a black pawn because the board wasn't changed")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "not be able to go from 11 to 16 and after be able to go to 15" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 11) {
          dut.io.board(i).poke("b011".U)
        } else {
          dut.io.board(i).poke("b000".U)
        }
      }
            dut.io.color.poke(0.U)

      dut.io.from.poke(11)
      dut.io.to.poke(16)

      dut.io.ValidMove.expect(
        false.B,
        s"it should be invalidated"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 11) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have stayed at 11")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

      dut.io.from.poke(11)
      dut.io.to.poke(15)
      dut.io.ValidMove.expect(
        true.B,
        "This move should be valid!"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 15) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 11")
        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }

  it should "be able to capture a piece from 5 to 14" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 5) {
          dut.io.board(i).poke("b011".U)

        } else if (i == 9) {
          dut.io.board(i).poke("b001".U)
        } else
          dut.io.board(i).poke("b000".U)

      }

            dut.io.color.poke(0.U)


      dut.io.from.poke(5)
      dut.io.to.poke(14)

      dut.io.ValidMove.expect(
        true.B,
        s"it should be allowed to jump from 5 to 14, capturing 9"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 14) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 11")
        } else {
          dut.io
            .newboard(i)
            .expect(
              "b000".U,
              s"piece $i should be empty! 5 and 9 are especially sketchy here!"
            )
        }

      }

    }
  }

  it should "be able to capture a piece from 11 to 18" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 11) {
          dut.io.board(i).poke("b011".U)

        } else if (i == 15) {
          dut.io.board(i).poke("b001".U)
        } else
          dut.io.board(i).poke("b000".U)

      }

      dut.io.color.poke(0.U)

      dut.io.from.poke(11)
      dut.io.to.poke(18)

      dut.io.ValidMove.expect(
        true.B,
        s"it should be allowed to jump from 11 to 18, capturing 15"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 18) {
          dut.io
            .newboard(i)
            .expect("b011".U, "the piece should have moved to 18")
        } else {
          dut.io
            .newboard(i)
            .expect(
              "b000".U,
              s"piece $i should be empty! 5 and 9 are especially sketchy here!"
            )
        }

      }

    }
  }

  it should "not be able to able to capture with black white black" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        if (i == 0) {
          dut.io.board(i).poke("b011".U)

        } else if (i == 5) {
          dut.io.board(i).poke("b001".U)
        } else if (i == 9) {
          dut.io.board(i).poke("b011".U)

        } else
          dut.io.board(i).poke("b000".U)

      }

            dut.io.color.poke(0.U)


      dut.io.from.poke(0)
      dut.io.to.poke(9)

      dut.io.ValidMove.expect(
        false.B,
        s"it shouldn't be allowed to jump from 0 to 9, capturing 5. The move should fail"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 0) {
          dut.io
            .newboard(i)
            .expect("b011".U, "0 should still have a black piece")
        } else if (i == 5) {
          dut.io
            .newboard(i)
            .expect("b001".U, "5 should still have a white piece")
        } else if (i == 9) {
          dut.io
            .newboard(i)
            .expect("b011".U, "9 should still have a black piece")

        } else {
          dut.io.newboard(i).expect("b000".U, s"piece $i should be empty!")
        }

      }

    }
  }


   it should "input a few white tiles and see that white can capture" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        
          dut.io.board(i).poke("b000".U)

      }
      dut.io.board(31).poke("b001".U)//white pawn
      dut.io.board(26).poke("b100".U)//black king
      dut.io.board(5).poke("b010".U)//random other piece

      dut.io.color.poke(1.U)

      dut.io.from.poke(31)
      dut.io.to.poke(22)

      dut.io.ValidMove.expect(
        true.B,
        s"it should be allowed to jump from 31 to 22, capturing 26"
      )

      for (i <- 0 to 31) {
        // myprint()
        if (i == 22) {
          dut.io
            .newboard(i)
            .expect("b001".U, "the piece should have moved to 22")
        } else if(i==5){
            dut.io.newboard(i).expect("b010".U, "this piece should be unchanged!")
        }else {
          dut.io
            .newboard(i)
            .expect(
              "b000".U,
              s"piece $i should be empty! 31 and 26 are especially sketchy here!"
            )
        }

      }

    }
  }

  it should "validate some simple movement for a white pawn" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        
          dut.io.board(i).poke("b000".U)

      }
      dut.io.board(29).poke("b001".U)//white pawn
   
      dut.io.color.poke(1.U)

      dut.io.from.poke(29.U)
      dut.io.to.poke(24.U)

      dut.io.ValidMove.expect(
        true.B,
        s"it should be allowed to go from 29 to 24"
      )

      dut.io.to.poke(25.U)
      dut.io.ValidMove.expect(true.B, "it should be able to go from 29 to 25")

      dut.io.to.poke(30.U)
      dut.io.ValidMove.expect(false.B, "it shouldn't be allowed to go to 30 from 29")

    
    }
  }

  it should "validate some simple movement for a white king" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        
          dut.io.board(i).poke("b000".U)

      }
      dut.io.board(8).poke("b010".U)//white king
   
      dut.io.color.poke(1.U)

      dut.io.from.poke(8)
      dut.io.to.poke(4)

      dut.io.ValidMove.expect(
        true.B,
        s"it should be allowed to go from 8 to 4"
      )

      dut.io.to.poke(5)
      dut.io.ValidMove.expect(true.B, "it should be able to go from 8 to 5")

      dut.io.to.poke(12)
      dut.io.ValidMove.expect(true.B, "it shouldn't be allowed to go to 8 from 12")

      dut.io.to.poke(13)
      dut.io.ValidMove.expect(true.B, "it shouldn't be allowed to go to 8 from 13")

      dut.io.to.poke(8)
        dut.io.ValidMove.expect(false.B, "it shouldnt be allowed to go to itself")

      for(i<-14 to 28){
        dut.io.to.poke(i.U)
        dut.io.ValidMove.expect(false.B, s"you shouldn't be able to go from 8 to $i")
      }

    }
  }

  it should "invalidate a walk when a jump is available" in {
    test(new MoveValidator()) { dut =>
      for (i <- 0 to 31) {
        
          dut.io.board(i).poke("b000".U)

      }
      dut.io.color.poke(1.U)
      dut.io.board(14).poke("b001".U)//white pawn
      dut.io.board(9).poke("b100".U)

      dut.io.from.poke(14.U)
      dut.io.to.poke(10.U)

      dut.io.ValidMove.expect(false.B, "there is a jump available")

      dut.io.to.poke(5.U)
      dut.io.ValidMove.expect(true.B,"it should validate the jump!")
   
      

    }
  }

  it should "turn a white pawn into a king" in {
    test(new MoveValidator()){ dut =>
      for(i<-0 to 31){
        dut.io.board(i).poke("b000".U)
      }
      dut.io.color.poke(1.U)

      dut.io.board(6).poke("b001".U)
      dut.io.from.poke(6.U)
      dut.io.to.poke(2.U)

      dut.io.ValidMove.expect(true.B, "we should be able to go from 6 to 2")
      dut.io.newboard(2.U).expect("b010".U,"we should now have a king")
      dut.io.newboard(6.U).expect("b000".U,"the pawn should have moved!")
      
      
      
      }
  }

  it should "turn a white pawn into a king through a jump" in {
    test(new MoveValidator()){ dut =>
      for(i<-0 to 31){
        dut.io.board(i).poke("b000".U)
      }
      dut.io.color.poke(1.U)
            dut.io.board(10).poke("b001".U)
            dut.io.board(6).poke("b100".U)
      dut.io.from.poke(10.U)
      dut.io.to.poke(1.U)
      dut.io.ValidMove.expect(true.B, "should be able to jump over a piece to get from 10 to 1")
 dut.io.newboard(1.U).expect("b010".U,"we should now have a king")
      dut.io.newboard(10.U).expect("b000".U,"the pawn should have moved!")


    }
  }

  it should "not allow you to walk from a place no one is" in {
    test(new MoveValidator()){ dut =>
      for(i<-0 to 31){
        dut.io.board(i).poke("b000".U)
      }
      dut.io.color.poke(1.U)
            dut.io.board(10).poke("b001".U)
            dut.io.board(6).poke("b100".U)

        dut.io.board(7).poke("b001".U)
            dut.io.board(11).poke("b100".U)

          dut.io.board(1).poke("b001".U)
            dut.io.board(28).poke("b100".U)
      dut.io.from.poke(13)
      dut.io.ValidMove.expect(false.B, "cant move from 13 because nothing is on 13.")
 

    }
  }



}
