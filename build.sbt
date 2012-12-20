organization := "de.johoop"

name := "ant4sbt"

version := "1.0.4"

sbtPlugin := true

libraryDependencies += "org.apache.ant" % "ant" % "1.8.4"

resolvers += "Sonatype Release" at "https://oss.sonatype.org/content/repositories/releases"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:_")

