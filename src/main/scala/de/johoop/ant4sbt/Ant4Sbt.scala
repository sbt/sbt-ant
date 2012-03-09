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
import de.johoop.ant4sbt.ant.AntBuildListener
import de.johoop.ant4sbt.ant.AntProject

object Ant4Sbt extends Plugin with Settings {

  def antSettings(buildFile: File) : Seq[Setting[_]]= {
    val project = new AntProject(buildFile)

    project.targets map { antTarget =>
      TaskKey[Unit]("ant-" + antTarget) <<= streams map { streams =>
        project addLogger streams.log
        project runTarget antTarget
      }
    } toSeq
  }

  def ant(s: String) = TaskKey[Unit]("ant-" + s)
}
