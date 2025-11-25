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

class Opponent extends Module {

  val io = IO(new Bundle {
    val statusIn = Input(Bool())

    val statusOut = Output(Bool())

    val In = Input(Vec(32, UInt(3.W)))
    val from = Output(UInt(5.W))
    val to = Output(UInt(5.W))

    val hastomakespecificmove=Input(Bool())
    val specificallyfromwhere=Input(UInt(5.W))

    val stillMoving = Output(Bool())

    // debug:
    val score_now = Output(SInt(8.W))
    val out_counter_index = Output(UInt(9.W))
    val out_from_for_now = Output(UInt(5.W))
    val out_to_for_now = Output(UInt(5.W))

  })

  val state = RegInit(0.U(2.W))
  val next_state = WireDefault(state) // borrowned this line from ChatGPT.

  val counter_index = RegInit(0.U(9.W))
  // I'm actually just going to count from 0 to 10*32 and use that all moves are from +9,+7,+5,+4,+3 or minus.
  val max_val = RegInit(-100.S(8.W)) // Pretty sure I need less than 8 bits.
  val movevalidator = Module(new MoveValidator())
  movevalidator.io.board := io.In
  movevalidator.io.color := 1.U
  // dummy values
  movevalidator.io.to := io.to
  movevalidator.io.from := io.from

  val movevalidator_0 = Module(new MoveValidator())
  val movevalidator_1 = Module(new MoveValidator())
  val movevalidator_2 = Module(new MoveValidator())
  val movevalidator_3 = Module(new MoveValidator())
  // all dummy values...
  movevalidator_0.io.board := io.In
  movevalidator_0.io.color := 0.U
  movevalidator_0.io.from := 0.U
  movevalidator_0.io.to := 0.U
  movevalidator_1.io.board := io.In
  movevalidator_1.io.color := 0.U
  movevalidator_1.io.from := 0.U
  movevalidator_1.io.to := 0.U
  movevalidator_2.io.board := io.In
  movevalidator_2.io.color := 0.U
  movevalidator_2.io.from := 0.U
  movevalidator_2.io.to := 0.U
  movevalidator_3.io.board := io.In
  movevalidator_3.io.color := 0.U
  movevalidator_3.io.from := 0.U
  movevalidator_3.io.to := 0.U

  // dummy values

  io.statusOut := (state === "b10".U)
  val currentMax=RegInit(0.U(9.W))
  val current_from = RegInit(0.U(5.W))
  val current_to = RegInit(0.U(5.W))
  val current_still_moving = RegInit(false.B)
  io.stillMoving := current_still_moving

  val boardeval = Module(new BoardEval1())
  io.score_now := boardeval.io.score

  // These are dummy values that shouldn't be used.
  boardeval.io.In := io.In
  boardeval.io.color := 0.U

  io.to := current_to
  io.from := current_from

//used in state b01:
  val from_for_now =(counter_index / 10.U )(4,0)
  val to_for_now = WireDefault(0.U(5.W))

  val toadd = WireDefault(0.S) // I could specify the wires here if I wished.
  io.out_counter_index := counter_index
  io.out_from_for_now := from_for_now
  io.out_to_for_now := to_for_now

  // 00= Idle
  // 01=calculating
  // 10=done

  state := next_state

  switch(state) {
    is("b00".U) { // Idle.
      when(io.statusIn === 1.U) {
        next_state := "b01".U
        max_val := -100.S
        // max_val= minus infinite.
        when(io.hastomakespecificmove===true.B){
          counter_index:=io.specificallyfromwhere*10.U
          currentMax:=io.specificallyfromwhere*10.U+9.U
        }.otherwise{
          counter_index:=0.U
          currentMax:=319.U
        }

        /*
        if forcedmoves: go with one version.
        else go with the other.
        I dont actually end up doing this because I just roll without.
        I try a lot of illegal moves.
         */

      }
      // if statusIn is ready, then set next state.

    }

    is("b01".U) { // calculating.

      // from index, figure out what to check.
       printf(p"Move from $counter_index to $to_for_now, " +
         "which is a valid move ${movevalidator.io.ValidMove} \n \n")

      switch(counter_index % 10.U) {
        is(0.U) {
          toadd := -9.S
        }
        is(1.U) {
          toadd := -7.S
        }
        is(2.U) {
          toadd := -5.S
        }
        is(3.U) {
          toadd := -4.S
        }
        is(4.U) {
          toadd := -3.S
        }
        is(5.U) {
          toadd := 3.S
        }
        is(6.U) {
          toadd := 4.S
        }
        is(7.U) {
          toadd := 5.S
        }
        is(8.U) {
          toadd := 7.S
        }
        is(9.U) {
          toadd := 9.S
        }
      }
      to_for_now := (from_for_now.asSInt + toadd).asUInt

      movevalidator.io.from := from_for_now
      movevalidator.io.to := to_for_now

      boardeval.io.In := movevalidator.io.newboard

      // several things need to be figured out.
      // if this was a jump.
      // and it was valid.
      // and there are available jump.
      // then we will pipe something different into max_val
      val this_a_jump =
        (toadd === 9.S || toadd === 7.S || toadd === -7.S || toadd === -9.S)

      movevalidator_0.io.board := movevalidator.io.newboard
      movevalidator_1.io.board := movevalidator.io.newboard
      movevalidator_2.io.board := movevalidator.io.newboard
      movevalidator_3.io.board := movevalidator.io.newboard
      // I am setting them to 1 because we are checking if white can still move.
      movevalidator_0.io.color := 1.U
      movevalidator_1.io.color := 1.U
      movevalidator_2.io.color := 1.U
      movevalidator_3.io.color := 1.U

      movevalidator_0.io.from := to_for_now
      movevalidator_1.io.from := to_for_now
      movevalidator_2.io.from := to_for_now
      movevalidator_3.io.from := to_for_now

      movevalidator_0.io.to := to_for_now + 9.U
      movevalidator_1.io.to := to_for_now + 7.U
      movevalidator_2.io.to := to_for_now - 7.U
      movevalidator_3.io.to := to_for_now - 9.U

      val next_jump_valid = (movevalidator_0.io.ValidMove ||
        movevalidator_1.io.ValidMove
        || movevalidator_2.io.ValidMove
        || movevalidator_3.io.ValidMove)

      when(next_jump_valid) {
        boardeval.io.color := 0.U
      }.otherwise {
        boardeval.io.color := 1.U
      }

      // One thing I haven't figured out yet is:
      /*
      The we input the color, but I think we get the value for that color.
      This could need to change. Idk how to do this yet.

       */

      when(
        movevalidator.io.ValidMove === true.B && boardeval.io.score > max_val
      ) {
        current_from := from_for_now
        current_to := to_for_now
        current_still_moving := next_jump_valid
        max_val := boardeval.io.score
      }

      when(counter_index >= currentMax) {
        next_state := "b10".U
      }
      when(io.In(from_for_now)==="b000".U&&counter_index % 10.U===0.U){
        counter_index := counter_index + 10.U
      }.otherwise{
        counter_index := counter_index + 1.U
      }

    }
    is("b10".U) { // done.
      when(io.statusIn === false.B) {
        next_state := "b00".U
      }
      // set statusOut to 1.
      // if statusIn equals zero, set nextstep to idle.
      // at some point, we need to turn off

    }
  }

}
