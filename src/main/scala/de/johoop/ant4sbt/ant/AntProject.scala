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
import org.fest.reflect.core.Reflection._
import sbt.Logger
import java.net.URLClassLoader
import java.net.URL

class AntProject(buildFile: File, baseDir: File) {
  private var loggerAdded = false

  private val classLoader = new URLClassLoader(Array(
      new URL("file:/work/misc/ant.jar"),
      new URL("file:/work/misc/ant-launcher.jar"),
      new URL("file:/opt/jdk/lib/tools.jar")))

  private val projectClass = `type`("org.apache.tools.ant.Project").withClassLoader(classLoader).load
  private val projectHelperClass = `type`("org.apache.tools.ant.ProjectHelper").withClassLoader(classLoader).load
  private val buildListenerClass = `type`("org.apache.tools.ant.BuildListener").withClassLoader(classLoader).load
  private val buildEventClass = `type`("org.apache.tools.ant.BuildEvent").withClassLoader(classLoader).load

  private lazy val project = initializeProject

  private def initializeProject = {
//    val project = new Project
    val project = constructor.in(projectClass).newInstance()

//    project setUserProperty ("ant.file", buildFile.getAbsolutePath)
    method("setUserProperty").
        withParameterTypes(classOf[String], classOf[String]).
        in(project).invoke("ant.file", buildFile.getAbsolutePath)

//    project setBaseDir baseDir
    method("setBaseDir").withParameterTypes(classOf[java.io.File]).in(project).invoke(baseDir)

//    project init
    method("init").in(project).invoke()

    val projectRef = project.asInstanceOf[AnyRef]
    parseBuildFile(projectRef)

    projectRef
  }

  private def parseBuildFile(project: AnyRef) = {
//    ProjectHelper.configureProject(project, buildFile)
    staticMethod("configureProject").
        withParameterTypes(projectClass, classOf[java.io.File]).
        in(projectHelperClass).invoke(project, buildFile)
  }

  def addLogger(logger: Logger) = {

    if (! loggerAdded) {

      val handler = new BuildListenerInvocationHandler(logger, classLoader)
//    val listener = new AntBuildListener(logger)
      val listener = java.lang.reflect.Proxy.newProxyInstance(classLoader, Array(buildListenerClass), handler)

//      val listener = constructor.withParameterTypes(classOf[Logger]).in(antBuildListenerClass).newInstance(logger)

//        project addBuildListener new AntBuildListener(logger)
        method("addBuildListener").
            withParameterTypes(buildListenerClass).
            in(project).invoke(listener.asInstanceOf[AnyRef])
        loggerAdded = true
    }
  }

  def runDefaultTarget = {
//    val target = project.getDefaultTarget
    val target = method("getDefaultTarget").withReturnType(classOf[String]).in(project).invoke()
    runTarget(target)
  }

  def runTarget(target: String) = {
//    project executeTarget target
    method("executeTarget").withParameterTypes(classOf[String]).in(project).invoke(target)
  }

  def targets = {
//  val targetsMap = project.getCopyOfTargets
    val targetsMap = method("getCopyOfTargets").withReturnType(classOf[java.util.Map[_, _]]).in(project).invoke()
    targetsMap.keySet.asScala map (_.asInstanceOf[String])
  }
}
