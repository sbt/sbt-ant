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
import de.johoop.ant4sbt.ant.AntClient
import xsbti.AppConfiguration

trait ForkedAntServer extends Settings {

  override def buildServerClasspath(javaHome: Option[File], config: AppConfiguration) =
    Seq(IO.classLocationFile(classOf[de.johoop.ant4sbt.ant.AntServer]),
        IO.classLocationFile(classOf[org.apache.tools.ant.Project]),
        IO.classLocationFile(classOf[org.apache.tools.ant.launch.AntMain]),
        config.provider.scalaProvider.libraryJar
    ) ++ toolsJar(javaHomeHeuristic(javaHome))

  private def toolsJar(maybeJavaHome: Option[File]) =
    maybeJavaHome map {
      javaHome => Seq(javaHome / "lib" / "tools.jar", javaHome / ".." / "lib" / "tools.jar")
    } getOrElse Seq()

  private def javaHomeHeuristic(maybeJavaHome: Option[File]) =
    maybeJavaHome orElse
    (sys.env get "JAVA_HOME" map file) orElse
    (sys.props get "java.home" map file)

  override def startAntServer(buildFile: File, baseDir: File, port: Int, options: String, classpath: Seq[File], streams: TaskStreams, logging: Logger => ProcessLogger) = {
    streams.log debug "Starting Ant server..."

    val process = "java %s -cp %s de.johoop.ant4sbt.ant.AntServer %s %s %d".format(
      options,
      PathFinder(classpath).absString,
      buildFile.absolutePath,
      baseDir.absolutePath,
      port) run logging(streams.log)

    if (! new AntClient(port).ping) throw new IllegalStateException("unable to ping server")
    else streams.log debug "Started successfully."

    process
  }
}