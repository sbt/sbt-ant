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
package de.johoop.ant4sbt.util

import java.net.Socket
import java.io.BufferedReader
import java.io.PrintStream
import java.io.InputStreamReader

object Predef {
  def withResource[A <: { def close() }, B](resource: => A)(op: A => B) = {
    val r = resource
    try op(r)
    finally r.close
  }

  def withSocketStreams[B](socket: => Socket)(op: (BufferedReader, PrintStream) => B) = {
    withResource(socket) { socket =>
      withResource(new BufferedReader(new InputStreamReader(socket.getInputStream))) { in =>
        withResource(new PrintStream(socket.getOutputStream, true)) (op(in, _))
      }
    }
  }

  val done = "~~~ done"
  val bye = "~~~ bye"
}
