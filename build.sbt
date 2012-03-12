organization := "de.johoop"

name := "ant4sbt"

version := "1.0.0-SNAPSHOT"

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.easytesting" % "fest-reflect" % "1.2",
  "org.apache.ant" % "ant-launcher" % "1.7.0",
  "org.apache.ant" % "ant" % "1.7.0")

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := (
  <url>http://www.bitbucket.org/jmhofer/ant4sbt</url>
  <licenses>
    <license>
      <name>Eclipse Public License v1.0</name>
      <url>http://www.eclipse.org/legal/epl-v10.html</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>https://bitbucket.org/jmhofer/ant4sbt</url>
    <connection>scm:hg:https://bitbucket.org/jmhofer/ant4sbt</connection>
  </scm>
  <developers>
    <developer>
      <id>johofer</id>
      <name>Joachim Hofer</name>
      <url>http://jmhofer.johoop.de</url>
    </developer>
  </developers>
  <dependencies>
    <dependency>
      <groupId>org.easytesting</groupId>
      <artifactId>fest-reflect</artifactId>
      <version>1.2</version>
      <type>jar</type>
      <scope>compile</scope>
    </dependency>
  </dependencies>
)
