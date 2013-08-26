/*
 * This file is part of ant4sbt.
 *
 * Copyright (c) 2012, 2013 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.johoop.ant4sbt.ant
import java.net.Socket
import java.io.PrintStream
import java.io.BufferedReader
import java.io.InputStreamReader
import scala.annotation.tailrec
import sbt.Logger
import java.net.ConnectException
import java.io.OutputStream
import scala.concurrent.SyncVar

class AntClient(port: Int) {
  import de.johoop.ant4sbt.util.Predef._

  def stopServer = withServer { (_, out) => out println bye }

  def property(property: String) = withServer { (in, out) =>
      out println ("property " + property)
      readLines(in).headOption
  }

  def ping = retry(20, 50L, 25L) { withServer { (in, out) =>
    out println "ping"
    readLines(in).head == "pong"
  } }

  def retry[T](times: Int, delay: Long, delayInc: Long)(op: => T) : T =
    if (times == 1) op else try op
    catch {
      case _: ConnectException => {
       Thread sleep delay
        retry(times - 1, delay + delayInc, delayInc)(op)
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
    import scala.concurrent._
    import ExecutionContext.Implicits.global

    out println ("ant " + target)

    val running = new SyncVar[Boolean]
    running put true
    future { pipeConsoleInputToServer(running, out) }
    logLines(running, in, logger)
  }

  @tailrec
  private def pipeConsoleInputToServer(running: SyncVar[Boolean], out: OutputStream) : Unit = {
    val inputChars = System.in.available
    if (inputChars > 0) out write System.in.read
    else Thread sleep 100

    if (running isSet) pipeConsoleInputToServer(running, out)
  }

  @tailrec
  private def logLines(running: SyncVar[Boolean], in: BufferedReader, logger: Logger) : Unit = {
    val line = in.readLine
    line match {
      case `done` => 
        running.take
      case line => {
        logger info line
        logLines(running, in, logger)
      }
    }
  }

  private def withServer[T](op: (BufferedReader, PrintStream) => T) =
    withSocketStreams(new Socket("localhost", port)) { (in, out) => op(in, out) }
}
