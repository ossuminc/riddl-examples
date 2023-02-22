import sbt._

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "com.reactific"
ThisBuild / organizationHomepage := Some(new URL("https://reactific.com/"))
ThisBuild / organizationName := "Ossum Inc."
ThisBuild / startYear := Some(2022)
ThisBuild / licenses +=
  ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))

Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / versionScheme := Option("semver-spec")
ThisBuild / dynverVTagPrefix := false

lazy val examples = project.in(file("."))
  .enablePlugins(RiddlSbtPlugin)
  .settings(
    name := "riddl-examples",
    riddlcMinVersion := "0.19.0",
    riddlcOptions := Seq("--verbose",
      "from", "src/riddl/ReactiveBBQ/ReactiveBBQ.conf", "hugo"),
    Compile / packageBin / publishArtifact := false,
    Compile / packageDoc / publishArtifact := false,
    Compile / packageSrc / publishArtifact := false,
    publishTo := Option(Resolver.defaultLocal),
    libraryDependencies ++= Dep.testing
  )
