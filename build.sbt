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
  .configure(With.scala3)
  .configure(With.Riddl(version = "1.0.2"))
  .settings(
    libraryDependencies ++= Dep.testing
  )
