/*
 * This file is part of ant4sbt.
 *
 * Copyright (c) 2010, 2011 Joachim Hofer
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package de.johoop.ant4sbt

import sbt._
import Keys._

trait Settings extends Plugin {
  val ant = TaskKey[Unit]("ant")
  val antBuildFile = SettingKey[File]("ant-build-file")
}
