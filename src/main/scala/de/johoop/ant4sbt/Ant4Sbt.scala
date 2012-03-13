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
package de.johoop.ant4sbt

import sbt._
import sbt.Keys._
import de.johoop.ant4sbt.ant.AntServer
import de.johoop.ant4sbt.ant.AntClient

object Ant4Sbt extends Plugin with ForkedAntServer {

  override def restartAnt(buildFile: File, baseDir: File, port: Int, classpath: Seq[File]) = {
    stopAnt(port)
    startAnt(buildFile, baseDir, port, classpath)
  }

  override def stopAnt(port: Int) = new AntClient(port).stopServer

  override def runTarget(target: String, port: Int, logger: Logger) =
    new AntClient(port) runTarget (target, logger)
}
