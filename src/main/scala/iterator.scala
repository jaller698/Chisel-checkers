import chisel3._
import chisel3.util._

/*
INTERFACE:
    how statusIn and statusOut will function.
    When nothing is happening, we statusIn=0 and statusOut=0.
    When the outer component wants the calculations to start, it sets the statusIn to true.
    When the calculation is done, the Iterator sets statusOut to true.
    Then, when the outer component sets statusOut to false, Iterator sets statusOut to false.

    I am guessing that this will output the new board as well but I am not sure about this.

 */

/* PLANS AND DOCUMENTATION:
I think I need a few states. Right now: Idle, calculating, done

    I need to add a boardeval1 and 
    a counter that does two different things 
      count differently based on forcedmoves. 
      if I fix some shit we can just do +=2 :)
    legalmovesforwhite that also churns out. 

    I need to fix that the component sees if we can do a forced move twice. 
    This is some logic. 
    ContinuedMove. 

 */

class Iterator extends Module {

  val io = IO(new Bundle {
    val statusIn = Input(Bool())

    val statusOut = Output(Bool())

    val In = Input(Vec(32, UInt(3.W)))
    val from = Output(UInt(5.W))
    val to = Output(UInt(5.W))

  })

  val state=RegInit(0.U(2.W))
  val next_state=RegInit(0.U(2.W))
  val counter_index=RegInit(0.U(7.W))//128 possible next moves
  val max_val=RegInit(0.U(16.W))//Pretty sure I need less than 16 bits. 
  
  val current_from=RegInit(0.U(2.W))
  val current_to=RegInit(0.U(2.W))

  val boardeval = Module(new BoardEval1())
  


  //00= Idle
  //01=calculating
  //10=done

  next_state:=state//This will switch on the refresh... 

  switch(state){
    is("b00".U){//Idle.
      io.statusOut:=false.B 
      //if statusIn is ready, then set next state. 
      
    }

    is("b01".U){//calculating. 
      io.statusOut:=false.B//more has to happen here. 
      //from index, figure out what to check. 
      //get the value. 
      //bam bam bam. 
      //increment counter. 
      //We only increment if there isn't still a valid move. 
      //if counter is 127, then set done and return the stuff. 

    }
    is("b10".U){//done. 
      io.statusOut:=true.B
      io.from:=current_from
      io.to:=current_to
      when(io.statusIn===false.B){
        next_state:="b00".U
      }
      //set statusOut to 1. 
      //if statusIn equals zero, set nextstep to idle. 
      //at some point, we need to turn off

    }
  }


}
