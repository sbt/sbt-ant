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
import xsbti.AppConfiguration

trait Settings extends Keys {
  val antSettings = Seq[Setting[_]](
    antOptions := sys.env getOrElse ("ANT_OPTS", ""),

    antServerPort := 21345,
    antBuildFile := baseDirectory.value / "build.xml",
    antBaseDir <<= baseDirectory,

    antStartServer <<= (antBuildFile, antBaseDir, antServerPort, antOptions, antServerClasspath, streams, antServerLogger) map startAntServer,
    antStopServer <<= antServerPort map stopAntServer,
    antRestartServer <<= (antBuildFile, antBaseDir, antServerPort, antOptions, antServerClasspath, streams, antServerLogger) map restartAntServer,

    antServerClasspath <<= (javaHome, appConfiguration) map buildServerClasspath,

    antServerLogger := { logger => new ProcessLogger {
      def buffer[T](f: ⇒ T): T = f
      def error(s: ⇒ String): Unit = logger error s
      def info(s: ⇒ String): Unit = logger info s
    }},

    antRun := {
      Def.spaceDelimited("<target>").parsed foreach (runTarget(_, antServerPort.value, streams.value.log))
    },
    antRun <<= antRun.dependsOn(antStartServer),

    antProperty := {
      getProperty(Def.spaceDelimited("<property>").parsed.head, antServerPort.value)
    },
    antProperty <<= antProperty.dependsOn(antStartServer),

    onLoad in Global <<= antServerPort { port => (_ addExitHook (stopAntServer(port))) }
  )

  def addAntTasks(targets: String*) : Seq[Setting[_]] = {
    for (target <- targets)
    yield antTaskKey(target) <<= (antStartServer, antServerPort, streams) map { (_, port: Int, streams: TaskStreams) =>
      runTarget(target, port, streams.log)
    }
  }

  def antTaskKey(target: String) = TaskKey[Unit]("ant-run-" + target)

  def addAntProperties(properties: String*) : Seq[Setting[_]] =
    for (property <- properties)
    yield antPropertyKey(property) <<= (antStartServer, antServerPort) map { (_, port) => getProperty(property, port) }


  def antPropertyKey(property: String) = TaskKey[Option[String]]("ant-property-" + property)

  def startAntServer(buildFile: File, baseDir: File, port: Int, options: String, classpath: Seq[File], streams: TaskStreams, logging: Logger => ProcessLogger) : Process
  def stopAntServer(port: Int) : Unit
  def restartAntServer(buildFile: File, baseDir: File, port: Int, options: String, classpath: Seq[File], streams: TaskStreams, logging: Logger => ProcessLogger) : Process

  def buildServerClasspath(javaHome: Option[File], config: AppConfiguration) : Seq[File]

  def runTarget(target: String, port: Int, logger: Logger) : Unit
  def getProperty(property: String, port: Int) : Option[String]
}