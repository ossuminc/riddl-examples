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
  .settings(
    libraryDependencies ++= Dep.testing ++ Dep.riddl
    // riddlcMinVersion := "0.43.0",
    // riddlcOptions := Seq("--show-times"),
    // riddlcConf := file("src/riddl/ReactiveBBQ/ReactiveBBQ.conf")
  )
