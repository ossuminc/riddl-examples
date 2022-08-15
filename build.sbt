import sbt._

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / organization := "com.reactific"
ThisBuild / organizationHomepage := Some(new URL("https://reactific.com/"))
ThisBuild / organizationName := "Ossum Inc."
ThisBuild / startYear := Some(2019)
ThisBuild / licenses +=
  ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / versionScheme := Option("semver-spec")
ThisBuild / dynverVTagPrefix := false

lazy val examples = project.in(file(".")).settings(
  name := "riddl-examples",
  Compile / packageBin / publishArtifact := false,
  Compile / packageDoc / publishArtifact := false,
  Compile / packageSrc / publishArtifact := false,
  publishTo := Option(Resolver.defaultLocal),
  libraryDependencies ++= Seq(
    "com.reactific" %% "riddlc" % "0.7.1" % "test"
  ) ++ Dep.testing
)
