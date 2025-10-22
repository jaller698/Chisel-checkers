import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class legalmovesforwhitetest extends AnyFlatSpec with ChiselScalatestTester {

    behavior of "legalmovesforwhite_component"

    it should "not have legal moves on empty board" in {
    test(new LegalMovesForWhite()) { dut =>
        for(i<-0 to 31){
            dut.io.In(i).poke("b000".U)
        }
//        dut.io.In:=VecInit(Seq.tabulate(32) { i =>
  //          "b000".U
    //    })
        dut.clock.step()
        for(i<-0 to 32*4-1){//-1 because scala's range is shit. 
            dut.io.whereWeCanMove(i).expect(false.B, s" $i should be 0, because the board is empty and thus no moves are possible!")
        } 
        dut.io.forcedMoves.expect(false.B,s"there shouldnt be a forced move")

    }
  }

    it should "input a few black pieces and have no legal moves" in {
    test(new LegalMovesForWhite()) { dut =>
        for(i<-0 to 31){
            if(i==5||i==7||i==17|i==22){
                dut.io.In(i).poke("b011".U)
            }
            else{
                dut.io.In(i).poke("b000".U)

            }
        }
        dut.clock.step()
        for(i<-0 to 32*4-1){//-1 because scala's range is shit. 
            dut.io.whereWeCanMove(i).expect(false.B, s" $i should be 0, because the board is empty and thus no moves are possible!")
        }
        dut.io.forcedMoves.expect(false.B,s"there shouldnt be a forced move")
    }
  }

  it should "place a piece in the middle of the board and see that it can move left and right" in {
    test(new LegalMovesForWhite()) { dut =>
        for(i<-0 to 31){
            if(i==14){
                dut.io.In(i).poke("b001".U)
            }
            else{
                dut.io.In(i).poke("b000".U)

            }
        }
        dut.clock.step()
        for(i<-0 to 32*4-1){//-1 because scala's range is shit. 
            if(i==14*4||i==14*4+2){
                dut.io.whereWeCanMove(i).expect(true.B, s" $i should be true, because the piece can jump from here")

            }else{
                dut.io.whereWeCanMove(i).expect(false.B, s" $i should be 0, because the board is empty and thus no moves are possible!")

            }
        } 
        dut.io.forcedMoves.expect(false.B,s"there shouldnt be a forced move")
    }
  }

  it should "place a white piece and black piece and be able to jump over" in {
    test(new LegalMovesForWhite()) { dut =>
        for(i<-0 to 31){
           
                dut.io.In(i).poke("b000".U)
        }

        dut.io.In(26).poke("b001".U)//placing white
        dut.io.In(23).poke("b011".U)//placing black
        dut.clock.step()
        for(i<-0 to 32*4-1){//-1 because scala's range is shit. 
            if(i==26*4+3){
                dut.io.whereWeCanMove(i).expect(true.B, s" $i should be true, because the piece can jump from here")

            }else if(i==26*4){
                dut.io.whereWeCanMove(i).expect(true.B, s" $i should be true, because the piece can go to the left from here")
            }
            
            else{
                dut.io.whereWeCanMove(i).expect(false.B, s" $i should be 0, because the board is empty and thus no moves are possible!")

            }
        } 
        dut.io.forcedMoves.expect(true.B,s"the move should be forced")
    }
  }





}
