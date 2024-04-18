import sbt.*

Global / onChangedBuildSource := ReloadOnSourceChanges

enablePlugins(OssumIncPlugin)
enablePlugins(RiddlSbtPlugin)

val examples = Root("riddl-examples", startYr=2022)
  .configure(With.typical)
  .configure(With.noPublishing)
  .enablePlugins(RiddlSbtPlugin)
  .settings(
    libraryDependencies ++= Dep.testing,
    riddlcMinVersion := "0.42.0",
    riddlcOptions := Seq("--show-times"),
    riddlcConf := file("src/riddl/ReactiveBBQ/ReactiveBBQ.conf")
  )

