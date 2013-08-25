organization := "de.johoop"

name := "ant4sbt"

version := "1.1.1"

sbtPlugin := true

libraryDependencies += "org.apache.ant" % "ant" % "1.9.1"

resolvers += "Sonatype Release" at "https://oss.sonatype.org/content/repositories/releases"

scalaVersion := "2.9.2"

scalacOptions ++= Seq("-unchecked", "-deprecation")
