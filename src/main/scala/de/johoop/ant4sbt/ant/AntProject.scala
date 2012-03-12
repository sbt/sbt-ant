package de.johoop.ant4sbt.ant

import scala.collection.JavaConverters._
import java.io.File

import org.apache.tools.ant._
import sbt.Logger

class AntProject(buildFile: File, baseDir: File = new File(".")) {
  private val project = initializeProject
  private var loggerAdded = false

  parseBuildFile

  private def initializeProject = {
    val project = new Project
    project setUserProperty ("ant.file", buildFile.getAbsolutePath)
    project setBaseDir baseDir
    project init

    project
  }

  private def parseBuildFile = {
    val antProjectHelper = ProjectHelper.getProjectHelper
    project.addReference("project.helper", antProjectHelper)
    antProjectHelper.parse(project, buildFile)
  }

  def addLogger(logger: Logger) = {
    if (! loggerAdded) {
        project addBuildListener new AntBuildListener(logger)
        loggerAdded = true
    }
  }

  def runDefaultTarget = runTarget(project.getDefaultTarget)

  def runTarget(target: String) = project executeTarget target

  def targets = project.getCopyOfTargets.keySet.asScala map (_.asInstanceOf[String])
}
