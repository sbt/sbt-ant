package de.johoop.ant4sbt.ant

import java.io.File
import org.apache.tools.ant._
import sbt.Logger

class AntProject(buildFile: File, logger: Logger) {
  val project = initializeProject
  parseBuildFile(project)

  private def initializeProject = {
    val project = new Project
    project.setUserProperty("ant.file", buildFile.getAbsolutePath)
    project addBuildListener new AntBuildListener(logger)
    project.init
    project
  }

  private def parseBuildFile(project: Project) = {
    val antProjectHelper = ProjectHelper.getProjectHelper
    project.addReference("project.helper", antProjectHelper)
    antProjectHelper.parse(project, buildFile)
  }

  def defaultTarget = target(project.getDefaultTarget)

  def target(target: String) = project executeTarget target
}
