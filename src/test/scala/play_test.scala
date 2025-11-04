import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Tag
import chiseltest.WriteVcdAnnotation
object Interactive extends Tag("interactive")

class PlayerIOTest extends AnyFlatSpec with ChiselScalatestTester {
  def printBoard(c: ChiselCheckers): Unit = {
    val board = Array.fill(8, 8)(' ')
    for (i <- 0 until 32) {
      c.io.from.poke(i.U)
      c.clock.step(1)
      val colorAtTile = c.io.colorAtTile.peek().litValue.toInt

      val displayChar: Char = colorAtTile match {
        case 0 => '·' // empty dark square (use a visible dot)
        case 1 => 'w' // white piece
        case 3 => 'b' // black piece
        case 2 => 'W' // example: white king (if used)
        case 4 => 'B' // example: black king (if used)
        case _ => '?' // unknown encoding
      }

      val row = i / 4
      val posInRow = i % 4
      val col = if (row % 2 == 0) 1 + 2 * posInRow else 0 + 2 * posInRow

      board(row)(col) = displayChar
    }

    // Print the board as an ASCII 8x8 grid with coordinates and separators
    println("    0 1 2 3 4 5 6 7")
    println("   -----------------")
    for (r <- 0 until 8) {
      val sb = new StringBuilder
      sb.append(s"$r | ")
      for (c <- 0 until 8) {
        // leave light squares as a space for readability
        val ch = if ((r + c) % 2 == 1) board(r)(c) else ' '
        sb.append(ch)
        sb.append(' ')
      }
      sb.append("|")
      println(sb.toString())
    }
    println("   -----------------")
  }

  "ChiselCheckers" should "be playable" taggedAs Interactive in {
    test(new ChiselCheckers()).withAnnotations(
      Seq( /* VerilatorBackendAnnotation, */ WriteVcdAnnotation)
    ) { c =>
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
          "What do you want to do? (type 'q' to finish test), otherwise press enter to continue"
        )
        val input = scala.io.StdIn.readLine()
        if (input == "q") {
          // skip further processing and exit
          cancel("Thank you for playing!")
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

        c.clock.step(1)
      }
    }
  }
}
