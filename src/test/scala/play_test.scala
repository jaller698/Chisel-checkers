import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Tag
object Interactive extends Tag("interactive")

/** Interactive-ish test:
  *   - Initializes the board (mode = 0)
  *   - Prints the board content (tiles 0..31)
  *   - Then waits for a line of keyboard input before finishing
  *
  * Note: Blocking on stdin in tests is only suitable when you run the test
  * interactively (e.g. `sbt "testOnly example.PlayerIOTest"`). CI or
  * non-interactive sbt/test runners will hang.
  */
class PlayerIOTest extends AnyFlatSpec with ChiselScalatestTester {
  def printBoard(c: ChiselCheckers): Unit = {
    println("-----------------------------")
    for (i <- 0 until 32) {
      c.io.from.poke(i.U)
      c.clock.step(1) // step to ensure any sequential logic updates
      val colorAtTile = c.io.colorAtTile.peek().litValue.toInt
      val displayChar = colorAtTile.toInt match {
        case 0 => " " // empty
        case 1 => "w" // white
        case 3 => "b" // black
        case _ => "?" // unknown
      }
      println(s"Tile $i color: $displayChar")

      // Print tile with separators
      // in a pattern that resembles a checkers board, with x denoting whit

      // End of row
      // if (i % 4 == 3) {
      //   println("\n-----------------------------")
      // }
    }
  }

  "ChiselCheckers" should "initialize board, print it, then wait for keyboard input" taggedAs Interactive in {
    test(new ChiselCheckers()) { c =>
      // Apply reset for one cycle
      c.reset.poke(true.B)
      c.clock.step(1)
      c.reset.poke(false.B)

      // Initialize board: mode = 0 => clearing memory
      println("Asserting init mode (mode = 0) to clear board.")
      c.io.mode.poke(0.U)
      c.clock.step(1) // let initialization happen
      println("Board initialized to empty.")

      // Switch to read mode (mode = 2)
      c.io.mode.poke(2.U)
      c.clock.step(1) // let mode switch happen
      println("Current board state:")
      printBoard(c)

      // start play logic

      while (true) {
        println(
          "What do you want to do? (type 'exit' to finish test), otherwise press enter to continue"
        )
        val input = scala.io.StdIn.readLine()
        if (input == "exit") {
          println("Exiting test.")
        } else {
          println("Please enter the from position (0-31):")
          val fromInput = scala.io.StdIn.readLine()
          val fromPos = fromInput.toInt
          println("Please enter the to position (0-31):")
          val toInput = scala.io.StdIn.readLine()
          val toPos = toInput.toInt

          // poke the from and to positions
          c.io.from.poke(fromPos.U)
          c.io.to.poke(toPos.U)
          // switch to play mode
          c.io.mode.poke(1.U)
          c.clock.step(1) // let the move process

          // wait for ready
          while (c.io.ready.peek().litToBoolean == false) {
            c.clock.step(1)
          }
          val isValid = c.io.isMoveValid.peek().litToBoolean
          if (isValid) {
            println(s"Move from $fromPos to $toPos is VALID.")
          } else {
            println(s"Move from $fromPos to $toPos is INVALID.")
          }

          // then print the board again
          c.io.mode.poke(2.U) // switch back to view mode
          c.clock.step(1)
          println("Current board state after move:")
          printBoard(c)
        }

        // send a clock cycle to avoid busy looping
        c.clock.step(1)

        // send in
      }
    }
  }
}
