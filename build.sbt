import sbt.*

Global / onChangedBuildSource := ReloadOnSourceChanges

enablePlugins(OssumIncPlugin)

lazy val examples = Root(
  "riddl-examples",
  startYr = 2022,
  devs = List(
    Developer(
      "reid-spencer",
      "Reid Spencer",
      "reid@ossuminc.com",
      url("https://github.com/reid-spencer")
    )
  )
).configure(With.typical)
  .configure(With.noPublishing)
  .configure(With.Scala3)
  .configure(With.Riddl.library(version = "2.0.0-rc.24-33-f4076e2c", nonJVMDependency = false))
  .settings(
    libraryDependencies ++= Dep.testing
  )
