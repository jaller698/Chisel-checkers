import chisel3._
import chisel3.util._

/* 
WE DONT ACTUALLY NEED THIS COMPONENT
WE CAN JUST PUSH AND POP VALID MOVES. 
WE USE THIS COMPONENT MAYBE ONCE IN THE END. 
WE DO NEED TO FIGURE OUT FORCED MOVES BUT THAT IS MORE OR LESS IT!
 */


class LegalMovesForWhite() extends Module {

    // Index corresponding to 32 dark squares
    private def row(i: Int) = i / 4
    private def col(i: Int): Int = {
    val r = row(i)
    val offset = if (r % 2 == 0) 1 else 0
    offset + 2 * (i % 4)
  }


    private def idx(row: Int, col: Int): Int = {
    if (row < 0 || row >= 8 || col < 0 || col >= 8) throw new IllegalArgumentException( s" $row  or $col out of range")
    if ((row + col) % 2 != 1) throw new IllegalArgumentException( s" $row , $col hit a white square")
    return row * 4 + col / 2
  }
  val io = IO(new Bundle {
    val In = Input(Vec(32, UInt (3.W)))

    val forcedMoves=Output(Bool())//THIS ONE ISN'T BEING SET AT ALL!

    val whereWeCanMoveFrom=Output(Vec(32,Bool()))
    //This one is if we can move from here. 
    val whereWeCanMove=Output(Vec(4*32,Bool()))
    /* 
    00: can go left
    01: can jump left
    10: can go right
    11: can jump right. 
     */

  })

    io.forcedMoves:=false.B
    io.whereWeCanMoveFrom := VecInit(Seq.tabulate(32) { i =>
            false.B
      })
    io.whereWeCanMove:=VecInit(Seq.tabulate(4*32) { i=>
        false.B
    })
  for (i <- 0 to 31) {
    io.whereWeCanMoveFrom(i):=(
        io.whereWeCanMove(i*4)||//can go up left
        io.whereWeCanMove(i*4+1)||//can jump over up left
        io.whereWeCanMove(i*4+2)||//can go up right
        io.whereWeCanMove(i*4+3)//can jump over up right
    )
    //Needs to take a lot into account. 
    val row_curr=row(i)
    val col_curr=col(i)

    //The code to actually set forcedMoves: 
    val jumps= new Array[chisel3.Bool](2*32)
    var index:Int=0
    for(i<-0 to 31){
        if(i%4==1||i%4==3){
            //TODO: DO AN ADD. 
            index+=1
        }
    }

    //can move up left.
    if(row_curr>=1 && col_curr>=1){
        val to_1=idx(row_curr-1,col_curr-1)
        //b001 is supposed to be white

        io.whereWeCanMove(i*4):=(io.In(i)==="b001".U && io.In(to_1)==="b000".U)
        //this one is white and the next one is empty
    }
    //can jump up left
    if(row_curr>=2&&col_curr>=2){
        val to_jump_over=idx(row_curr-1,col_curr-1)
        val to_jump_to=idx(row_curr-2,col_curr-2)
        io.whereWeCanMove(i*4+1):=(

            io.In(i)==="b001".U&&
            io.In(to_jump_over)==="b011".U&&
            io.In(to_jump_to)==="b000".U//black normal piece. Can't jump over black king yet. 
        )

    }
    //can move up right
    if(row_curr>=1 && col_curr<=7-1){
        val to_1=idx(row_curr-1,col_curr+1)
        //b001 is supposed to be white

        io.whereWeCanMove(i*4+2):=io.In(i)==="b001".U && io.In(to_1)==="b000".U
        //this one is white and the next one is empty
    }
    //can jump up over up right:
    if(row_curr>=2&&col_curr<=7-2){
        val to_jump_over=idx(row_curr-1,col_curr+1)
        val to_jump_to=idx(row_curr-2,col_curr+2)
        io.whereWeCanMove(i*4+3):=(
            io.In(i)==="b001".U&&
            io.In(to_jump_over)==="b011".U&&
            io.In(to_jump_to)==="b000".U 
        )

    }


    /* 
    if not all the way on the left    
        if can move up left:
            wherewecanmovefrom(i):=1
            wherewecanmove(i*4):=1

    if not almost all on the left
        if can jump up left:
            wherewecanmovefrom(i):=1
            wherewecanmove(i*4+1):=1

    if not all on the right
        if can move up right
            wherewecanmovefrom(i):=1
            wherewecanmove(i*4+2):=1

    if not almost all on the right
        if can jump up left
            wherewecanmovefrom(i):=1
            wherewecanmove(i*4+3):=1

     */
    
  
  }

}