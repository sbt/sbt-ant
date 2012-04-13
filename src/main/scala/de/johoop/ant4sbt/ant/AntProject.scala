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

import scala.collection.JavaConverters._
import java.io.File
import org.apache.tools.ant._
import sbt.Logger
import java.io.InputStream
import java.io.BufferedReader

class AntProject(buildFile: File, baseDir: File) {
  private val project = initializeProject

  private def initializeProject = {
    val project = new Project
    project setUserProperty ("ant.file", buildFile.getAbsolutePath)
    project setBaseDir baseDir
    project.init

    project
  }

  def configure = {
    ProjectHelper configureProject (project, buildFile)
    this
  }

  def runDefaultTarget(in: BufferedReader, logger: BuildListener) = runTarget(project.getDefaultTarget, in, logger)

  def runTarget(target: String, in: BufferedReader, logger: BuildListener) = {
    project addBuildListener logger
    project setInputHandler new ServerInputHandler(in)
    try project executeTarget target
    finally project removeBuildListener logger
  }

  def targets = project.getCopyOfTargets.keySet.asScala map (_.asInstanceOf[String])

  def property(property: String) = Option(project getProperty property)
}
