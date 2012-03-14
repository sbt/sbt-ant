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

trait Settings {
  val antBuildFile = SettingKey[File]("ant-build-file", "Location of the Ant build file (usually named 'build.xml').")
  val antBaseDir = SettingKey[File]("ant-base-dir", "Base directory for the Ant build.")
  val antHome = SettingKey[File]("ant-home", "Home directory of Ant (ANT_HOME).")
  val antOptions = SettingKey[String]("ant-options", "Additional JVM options for Ant (ANT_OPTS).")
  val antServerPort = SettingKey[Int]("ant-server-port", "Port the Ant server should listen at.")

  val antServerClasspath = TaskKey[Seq[File]]("ant-server-classpath", "Classpath for the forked Ant server.")

  val antStartServer = TaskKey[Unit]("ant-start-server", "Start the Ant server. The server will keep running even when leaving interactive mode.")
  val antStopServer = TaskKey[Unit]("ant-stop-server", "Stop the Ant server again.")
  val antRestartServer = TaskKey[Unit]("ant-restart-server", "Restart the Ant server.")

  val antProperty = InputKey[Option[String]]("ant-property", "Returns the value of the given ant property.")
  val antRun = InputKey[Unit]("ant-run", "Run the Ant targets given as arguments (runs the default target with no arguments).")

  val antSettings = Seq[Setting[_]](
    antHome := file(System getenv "ANT_HOME"),
    antOptions := Option(System getenv "ANT_OPTS") getOrElse "",

    antServerPort := 21345,
    antBuildFile <<= baseDirectory (_ / "build.xml"),
    antBaseDir <<= baseDirectory,

    antStartServer <<= (antBuildFile, antBaseDir, antServerPort, antOptions, antServerClasspath) map startAntServer,
    antStopServer <<= antServerPort map stopAntServer,
    antRestartServer <<= (antBuildFile, antBaseDir, antServerPort, antOptions, antServerClasspath) map restartAntServer,

    antServerClasspath <<= (javaHome, antHome, update) map buildServerClasspath,

    antRun <<= inputTask { (argTask: TaskKey[Seq[String]]) =>
      (antStartServer, argTask, antServerPort, streams) map { (_, args: Seq[String], port: Int, streams: TaskStreams) =>
        args foreach (runTarget(_, port, streams.log))
      }
    },

    antProperty <<= inputTask { (argTask: TaskKey[Seq[String]]) =>
      (antStartServer, argTask, antServerPort) map { (_, args: Seq[String], port: Int) => getProperty(args.head, port) }
    },

    onLoad in Global <<= antServerPort { port => (_ addExitHook (stopAntServer(port))) }
  )

  def addAntTasks(targets: String*) : Seq[Setting[_]] = {
    for (target <- targets)
    yield antTaskKey(target) <<= (antServerPort, streams) map { (port: Int, streams: TaskStreams) =>
      runTarget(target, port, streams.log)
    }
  }

  def antTaskKey(target: String) = TaskKey[Unit]("ant-run-" + target)

  def addAntProperties(properties: String*) : Seq[Setting[_]] = {
    for (property <- properties)
    yield antPropertyKey(property) <<= antServerPort map (getProperty(property, _))
  }

  def antPropertyKey(property: String) = TaskKey[Option[String]]("ant-property-" + property)

  def startAntServer(buildFile: File, baseDir: File, port: Int, options: String, classpath: Seq[File]) : Unit
  def stopAntServer(port: Int) : Unit
  def restartAntServer(buildFile: File, baseDir: File, port: Int, options: String, classpath: Seq[File]) : Unit

  def buildServerClasspath(javaHome: Option[File], antHome: File, report: UpdateReport) : Seq[File]

  def runTarget(target: String, port: Int, logger: Logger) : Unit
  def getProperty(property: String, port: Int) : Option[String]
}