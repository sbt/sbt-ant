/*
 * This file is part of ant4sbt.
 *
 * Copyright (c) 2012 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.johoop.ant4sbt.ant

import java.io.File
import org.apache.tools.ant.Project
import org.apache.tools.ant.ProjectHelper
import org.apache.tools.ant.DefaultLogger
import java.net.ServerSocket
import java.io.BufferedReader
import java.io.InputStreamReader
import de.johoop.ant4sbt.util.Predef._
import java.io.PrintWriter
import scala.annotation.tailrec
import java.io.OutputStream
import java.io.PrintStream

object AntServer {
  def main(args: Array[String]) : Unit = {
    val antServer = new AntServer(new File(args(0)), new File(args(1)))
    antServer serve args(2).toInt
  }
}

class AntServer(buildFile: File, baseDir: File) {
  lazy val project = new AntProject(buildFile, baseDir).configure

  def serve(port: Int) = acceptRequests(new ServerSocket(port))

  @tailrec private def acceptRequests(server: ServerSocket) {
    val continue = withSocketStreams(server.accept) { (in, out) =>
      val antTargetPattern = "ant (.*)".r
      val antPropertyPattern = "property (.*)".r
      in.readLine match {
        case "targets" => {
          project.targets foreach out.println
          out println done
          true
        }
        case antTargetPattern(target) => {
          project runTarget (target, createLoggerFor(out))
          out println done
          true
        }
        case antPropertyPattern(property) => {
          project property property map (out println _)
          out println done
          true
        }
        case `bye` => false
        case other => throw new IllegalStateException("invalid command: " + other)
      }
    }

    if (continue) acceptRequests(server) else server.close
  }

  private def createLoggerFor(out: PrintStream) = {
    val logger = new DefaultLogger
    logger setMessageOutputLevel 2
    logger setOutputPrintStream out
    logger setErrorPrintStream out
    logger
  }
}
