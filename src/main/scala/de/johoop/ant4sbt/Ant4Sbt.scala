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
import org.apache.tools.ant.{ Project => AntProject, DefaultLogger, ProjectHelper }

object Ant4Sbt extends Plugin with Settings {

  val antSettings = Seq(
      antBuildFile := file("build.xml"),
      ant <<= (antBuildFile, streams) map antTask)

  def antTask(buildFile: File, streams: TaskStreams): Unit = {
    streams.log.debug("Executing ant task for build file '%s'" format buildFile.getAbsolutePath)

    val project = new AntProject
    project.setUserProperty("ant.file", buildFile.getAbsolutePath)

    val consoleLogger = new DefaultLogger
    consoleLogger setErrorPrintStream System.err
    consoleLogger setOutputPrintStream System.out
    consoleLogger setMessageOutputLevel AntProject.MSG_INFO
    project addBuildListener consoleLogger

    project.init

    val antProjectHelper = ProjectHelper.getProjectHelper
    project.addReference("project.helper", antProjectHelper)
    antProjectHelper.parse(project, buildFile)

    project executeTarget project.getDefaultTarget
  }
}
