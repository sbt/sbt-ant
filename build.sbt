organization := "de.johoop"

name := "ant4sbt"

version := "1.1.2"

sbtPlugin := true

libraryDependencies += "org.apache.ant" % "ant" % "1.9.2"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:_")
