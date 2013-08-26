/*
 * This file is part of ant4sbt.
 *
 * Copyright (c) 2012, 2013 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.johoop.ant4sbt

import sbt._

trait Keys {
  val antBuildFile = SettingKey[File]("ant-build-file", "Location of the Ant build file (usually named 'build.xml').")
  val antBaseDir = SettingKey[File]("ant-base-dir", "Base directory for the Ant build.")
  val antOptions = SettingKey[Seq[String]]("ant-options", "Additional JVM options for Ant (ANT_OPTS).")
  val antServerPort = SettingKey[Int]("ant-server-port", "Port the Ant server should listen at.")
  val antServerLogger = SettingKey[Logger => ProcessLogger]("ant-server-logger", "Logging strategy for the ant server.")

  val antServerClasspath = TaskKey[Seq[File]]("ant-server-classpath", "Classpath for the forked Ant server.")

  val antStartServer = TaskKey[Process]("ant-start-server", "Start the Ant server. The server will keep running even when leaving interactive mode.")
  val antStopServer = TaskKey[Unit]("ant-stop-server", "Stop the Ant server again.")
  val antRestartServer = TaskKey[Process]("ant-restart-server", "Restart the Ant server.")

  val antProperty = InputKey[Option[String]]("ant-property", "Returns the value of the given ant property.")
  val antRun = InputKey[Unit]("ant-run", "Run the Ant targets given as arguments (runs the default target with no arguments).")
}

object Keys extends Keys
