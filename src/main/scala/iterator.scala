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
I think I need a few states. Both 

 */


class Iterator() extends Module {

    val io=IO(new Bundle{
        val statusIn=Input(Bool())


        val statusOut=Output(Bool())
        
        val In = Input(Vec(32, UInt(3.W)))
        val from=Output(UInt(5.W))
        val to=Output(UInt(5.W))



    })

}