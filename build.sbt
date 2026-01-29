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
  .configure(With.Riddl.library(version = "1.1.2", nonJVMDependency = false))
  .settings(
    libraryDependencies ++= Dep.testing
  )
