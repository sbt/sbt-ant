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

trait Settings extends Keys {
  val antSettings = Seq[Setting[_]](
    antHome := file(System getenv "ANT_HOME"),
    antOptions := Option(System getenv "ANT_OPTS") getOrElse "",

    antServerPort := 21345,
    antBuildFile <<= baseDirectory (_ / "build.xml"),
    antBaseDir <<= baseDirectory,

    antStartServer <<= (antBuildFile, antBaseDir, antServerPort, antOptions, antServerClasspath, streams) map startAntServer,
    antStopServer <<= (antServerPort) map stopAntServer,
    antRestartServer <<= (antBuildFile, antBaseDir, antServerPort, antOptions, antServerClasspath, streams) map restartAntServer,

    antServerClasspath <<= (javaHome, antHome, update) map buildServerClasspath,

    antRun <<= inputTask { (argTask: TaskKey[Seq[String]]) =>
      (antStartServer, argTask, antServerPort, streams) map { (_, args, port, streams) =>
        args foreach (runTarget(_, port, streams.log))
      }
    },

    antProperty <<= inputTask { (argTask: TaskKey[Seq[String]]) =>
      (antStartServer, argTask, antServerPort) map { (_, args, port) => getProperty(args.head, port) }
    },

    onLoad in Global <<= antServerPort { port => (_ addExitHook (stopAntServer(port))) }
  )

  def addAntTasks(targets: String*) : Seq[Setting[_]] = {
    for (target <- targets)
    yield antTaskKey(target) <<= (antStartServer, antServerPort, streams) map { (_, port: Int, streams: TaskStreams) =>
      runTarget(target, port, streams.log)
    }
  }

  def antTaskKey(target: String) = TaskKey[Unit]("ant-run-" + target)

  def addAntProperties(properties: String*) : Seq[Setting[_]] = {
    for (property <- properties)
    yield antPropertyKey(property) <<= (antStartServer, antServerPort) map { (_, port) => getProperty(property, port) }
  }

  def antPropertyKey(property: String) = TaskKey[Option[String]]("ant-property-" + property)

  def startAntServer(buildFile: File, baseDir: File, port: Int, options: String, classpath: Seq[File], streams: TaskStreams) : Process
  def stopAntServer(port: Int) : Unit
  def restartAntServer(buildFile: File, baseDir: File, port: Int, options: String, classpath: Seq[File], streams: TaskStreams) : Process

  def buildServerClasspath(javaHome: Option[File], antHome: File, report: UpdateReport) : Seq[File]

  def runTarget(target: String, port: Int, logger: Logger) : Unit
  def getProperty(property: String, port: Int) : Option[String]
}