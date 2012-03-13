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
  val antBuildFile = SettingKey[File]("ant-build-file")
  val antBaseDir = SettingKey[File]("ant-base-dir")
  val antHome = SettingKey[File]("ant-home")
  val antServerPort = SettingKey[Int]("ant-server-port")

  val antStart = TaskKey[Unit]("ant-start")
  val antStop = TaskKey[Unit]("ant-stop")
  val antRestart = TaskKey[Unit]("ant-restart")

  val ant = InputKey[Unit]("ant-run")

  val antSettings = Seq[Setting[_]](
    antServerPort := 21345,
    antBuildFile := file("build.xml"),
    antBaseDir := file("."),
    antHome := file(System getenv "ANT_HOME"),

    antStart <<= (antBuildFile, antBaseDir, antServerPort) map startAnt,
    antStop <<= antServerPort map stopAnt,
    antRestart <<= (antBuildFile, antBaseDir, antServerPort) map restartAnt,

    ant <<= inputTask { (argTask: TaskKey[Seq[String]]) =>
      (argTask, antServerPort, streams) map { (args: Seq[String], port: Int, streams: TaskStreams) =>
        args foreach (runTarget(_, port, streams.log))
      }
    }
  )

  def addAntTasks(targets: String*) : Seq[Setting[_]] = {
    for (target <- targets)
    yield antTaskKey(target) <<= (antServerPort, streams) map { (port: Int, streams: TaskStreams) =>
      runTarget(target, port, streams.log)
    }
  }

  def antTaskKey(target: String) = TaskKey[Unit]("ant-run-" + target)

  def startAnt(buildFile: File, baseDir: File, port: Int)
  def stopAnt(port: Int)
  def restartAnt(buildFile: File, baseDir: File, port: Int)

  def runTarget(target: String, port: Int, logger: Logger)
}