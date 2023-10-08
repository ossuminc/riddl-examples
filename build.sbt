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
riddlcMinVersion := "0.27.0"
riddlcOptions := Seq("--show-times")
riddlcConf := file("src/riddl/ReactiveBBQ/ReactiveBBQ.conf")
