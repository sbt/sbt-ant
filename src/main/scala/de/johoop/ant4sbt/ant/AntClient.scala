package de.johoop.ant4sbt.ant
import java.net.Socket
import java.io.PrintStream
import java.io.BufferedReader
import java.io.InputStreamReader
import scala.annotation.tailrec
import sbt.Logger
import java.net.ConnectException
import scala.concurrent.ops
import java.io.OutputStream
import scala.concurrent.SyncVar

class AntClient(port: Int) {
  import de.johoop.ant4sbt.util.Predef._

  def stopServer = withServer { (_, out) => out println bye }

  def property(property: String) = withServer { (in, out) =>
      out println ("property " + property)
      readLines(in).headOption
  }

  def ping = retry(10, 20L) { withServer { (in, out) =>
    out println "ping"
    readLines(in).head == "pong"
  } }

  def retry[T](times: Int, delay: Long)(op: => T) : T =
    if (times == 1) op else try op
    catch {
      case _: ConnectException => {
       Thread sleep delay
        retry(times - 1, delay)(op)
      }
    }

  def targets = withServer { (in, out) =>
      out println "targets"
      readLines(in)
  }

  @tailrec
  private def readLines(in: BufferedReader, acc: Seq[String] = Seq()) : Seq[String] = {
    val line = in.readLine
    line match {
      case `done` => acc
      case line => readLines(in, acc :+ line)
    }
  }

  def runTarget(target: String, logger: Logger) = withServer { (in, out) =>
    out println ("ant " + target)

    val running = new SyncVar[Boolean]
    running set true
    ops.spawn { pipeConsoleInputToServer(running, out) }
    logLines(running, in, logger)
  }

  @tailrec
  private def pipeConsoleInputToServer(running: SyncVar[Boolean], out: OutputStream) : Unit = {
    val inputChars = System.in.available
    if (inputChars > 0) out write System.in.read
    else Thread sleep 100

    if (running get) pipeConsoleInputToServer(running, out)
  }

  @tailrec
  private def logLines(running: SyncVar[Boolean], in: BufferedReader, logger: Logger) : Unit = {
    val line = in.readLine
    line match {
      case `done` => running set false
      case line => {
        logger info line
        logLines(running, in, logger)
      }
    }
  }

  private def withServer[T](op: (BufferedReader, PrintStream) => T) =
    withSocketStreams(new Socket("localhost", port)) { (in, out) => op(in, out) }
}
