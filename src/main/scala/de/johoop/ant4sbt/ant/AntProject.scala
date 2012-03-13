package de.johoop.ant4sbt.ant

import scala.collection.JavaConverters._
import java.io.File

import org.apache.tools.ant._
import sbt.Logger

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

  def runDefaultTarget(logger: BuildListener) = runTarget(project.getDefaultTarget, logger)

  def runTarget(target: String, logger: BuildListener) = {
    project addBuildListener logger
    try project executeTarget target
    finally project removeBuildListener logger
  }

  def targets = project.getCopyOfTargets.keySet.asScala map (_.asInstanceOf[String])
}
