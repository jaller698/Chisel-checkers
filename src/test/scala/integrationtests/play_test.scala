import chisel3._
import chisel3.util._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Tag
import chiseltest.WriteVcdAnnotation
import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.global
object Interactive extends Tag("interactive")

class PlayerIOTest extends AnyFlatSpec with ChiselScalatestTester {
  implicit val ec: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.global

  def readLineWithTimeout(timeout: FiniteDuration): Option[String] = {
    val f = Future { scala.io.StdIn.readLine() } // blocking inside the future
    try {
      val s = Await.result(f, timeout)
      Some(s.trim.toLowerCase)
    } catch {
      case _: Throwable =>
        None
    }
  }

  def parseInput(): Int = {
    while (true) {
      val input = scala.io.StdIn.readLine()
      // match either a integer or a int x int format
      val pos = if (input.matches("""\d+""")) {
        input.toInt
      } else if (input.matches("""\d+\s*[xX]\s*\d+""")) {
        val parts = input.split("[xX]").map(_.trim.toInt)
        val row = parts(0)
        val col = parts(1)
        println(s"Parsed coordinates: row=$row, col=$col")
        if (
          row < 0 || row >= 8 || col < 0 || col >= 8 || (row + col) % 2 != 1
        ) {
          println(
            "Invalid board coordinates entered. Try again."
          )
          -1
        } else {
          row * 4 + (col / 2)
        }
      } else {
        println("Invalid input format. Try again.")
        -1
      }
      if (pos < 0 || pos >= 32) {
        println("Invalid position entered. Try again.")
        // skip to next iteration
      } else {
        return pos
      }
    }
    return -1 // should never reach here
  }

  def printBoard(c: ChiselCheckers): Unit = {
    val board = Array.fill(8, 8)(' ')
    for (i <- 0 until 32) {
      c.io.from.poke(i.U)
      c.clock.step(1)
      // Wait for valid, then pulse ack
      while (!c.io.valid.peek().litToBoolean) { c.clock.step(1) }
      val colorAtTile = c.io.colorAtTile.peek().litValue.toInt
      c.io.ack.poke(true.B)
      c.clock.step(1)
      c.io.ack.poke(false.B)

      val displayChar: Char = colorAtTile match {
        case 0 => '_' // empty dark square (use a visible dot)
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
      var firstTime = true
      // Apply reset for one cycle
      c.reset.poke(true.B)
      c.clock.step(1)
      c.reset.poke(false.B)

      // Initialize board: op = 0 => clearing memory
      println("Asserting init op (op = 0) to clear board.")
      c.io.op.poke(0.U)
      c.clock.step(1)
      // Wait for valid, then ack
      while (!c.io.valid.peek().litToBoolean) { c.clock.step(1) }
      c.io.ack.poke(true.B)
      while (c.io.valid.peek().litToBoolean) { c.clock.step(1) }
      c.io.ack.poke(false.B)
      println("Board initialized to empty.")

      // Switch to read op (op = 2)
      c.io.op.poke(2.U)
      // Wait for valid, then ack
      while (!c.io.valid.peek().litToBoolean) { c.clock.step(1) }
      c.io.ack.poke(true.B)
      c.clock.step(1)
      c.io.ack.poke(false.B)
      println("Current board state:")
      printBoard(c)

      // start play logic
      while (true) {
        println(
          "What do you want to do? (type 'q' to finish test), otherwise press enter to continue"
        )
        // Read user input with a time out to not block indefinitely (only first time)
        val input = if (firstTime) {
          firstTime = false
          readLineWithTimeout(20.seconds).getOrElse("q")
        } else {
          scala.io.StdIn.readLine().trim.toLowerCase()
        }
        if (input == "q") {
          // skip further processing and exit
          cancel("Thank you for playing!")
        } else {
          println("Please enter the from position (0-31):")
          val fromPos = parseInput()
          println("Please enter the to position (0-31):")
          val toPos = parseInput()

          // poke the from and to positions
          c.io.from.poke(fromPos.U)
          c.io.to.poke(toPos.U)
          // switch to play op
          c.io.op.poke(1.U)
          c.clock.step(1)
          // Wait for valid, then ack
          while (!c.io.valid.peek().litToBoolean) { c.clock.step(1) }
          val isValid = c.io.isMoveValid.peek().litToBoolean
          c.io.ack.poke(true.B)
          c.clock.step(1)
          c.io.ack.poke(false.B)
          if (isValid) {
            println(s"Move from $fromPos to $toPos is VALID.")
          } else {
            println(s"Move from $fromPos to $toPos is INVALID.")
          }

          // then print the board again
          c.io.op.poke(2.U) // switch back to view op
          c.clock.step(1)
          // Wait for valid, then ack
          while (!c.io.valid.peek().litToBoolean) { c.clock.step(1) }
          c.io.ack.poke(true.B)
          c.clock.step(1)
          c.io.ack.poke(false.B)
          println("Current board state after move:")
          printBoard(c)
        }

        c.clock.step(1)
      }
    }
  }
}
