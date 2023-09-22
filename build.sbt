import sbt._

name := "riddl-examples"

ThisBuild / scalaVersion := "3.1.1"
ThisBuild / organization := "com.reactific"
ThisBuild / organizationHomepage := Some(new URL("https://reactific.com/"))
ThisBuild / organizationName := "Ossum Inc."
ThisBuild / startYear := Some(2022)
ThisBuild / licenses +=
  ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / versionScheme := Option("semver-spec")
ThisBuild / dynverVTagPrefix := false

Compile / packageBin / publishArtifact := false
Compile / packageDoc / publishArtifact := false
Compile / packageSrc / publishArtifact := false
publishTo := Option(Resolver.defaultLocal)
libraryDependencies ++= Dep.testing

enablePlugins(RiddlSbtPlugin)

lazy val riddl_version = "0.24.5"
libraryDependencies ++= Seq(
  "com.reactific" %% "riddl-testkit" % riddl_version % "test",
  "com.reactific" %% "riddl-hugo" % riddl_version % "test",
  "org.scalactic" %% "scalactic" % "3.2.9" % "test",
  "org.scalatest" %% "scalatest" % "3.2.9" % "test"
)
enablePlugins(RiddlSbtPlugin)
riddlcMinVersion := riddl_version
riddlcOptions := Seq("--verbose",
  "from", "src/riddl/ReactiveBBQ/ReactiveBBQ.conf", "hugo")
riddlcPath := file(
  // NOTE: Set this to your local path which will always have this portion
  // NOTE: of the path as a constant: riddl/riddlc/target/universal/stage/bin/riddlc
  // NOTE: You must "sbt stage" in the riddl/riddlc directory for this to work
  "/Users/reid/Code/reactific/riddl/riddlc/target/universal/stage/bin/riddlc"
)
