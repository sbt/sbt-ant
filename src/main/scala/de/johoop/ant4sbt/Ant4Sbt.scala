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
import de.johoop.ant4sbt.ant.AntProject

import java.net.URLClassLoader
import java.net.URL
import org.fest.reflect.core.Reflection._

object Ant4Sbt extends Plugin {
  def antSettings(buildFile: File, baseDir: File = new File(".")) : Seq[Setting[_]]= {
    lazy val project = new AntProject(buildFile, baseDir)

    project.targets map { antTarget =>
      TaskKey[Unit]("ant-" + antTarget) <<= streams map { streams =>
        project addLogger streams.log
        project runTarget antTarget
      }
    } toSeq
  }

  def ant(targetName: String) = TaskKey[Unit]("ant-" + targetName)
}
