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

trait ForkedAntServer extends Settings {

  override def buildServerClasspath(antHome: File, resolved: UpdateReport) = {
    Seq(IO.classLocationFile(classOf[de.johoop.ant4sbt.ant.AntServer])) ++
    filesOf("org.scala-lang" % "scala-library" % "2.9.1", resolved) ++
    ((antHome / "lib") * "*.jar").get :+
    (file(System getenv "JAVA_HOME") / "lib" / "tools.jar")
  }

  private def filesOf(m: ModuleID, resolved: UpdateReport): Seq[File] = resolved.select(module = (_: ModuleID) == m)

  override def startAnt(buildFile: File, baseDir: File, port: Int, classpath: Seq[File]) = {
    "java -cp %s de.johoop.ant4sbt.ant.AntServer %s %s %d".format(
        PathFinder(classpath).absString, buildFile.absolutePath, baseDir.absolutePath, port).run
  }
}