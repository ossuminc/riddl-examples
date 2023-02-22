import sbt._

/** V - Dependency Versions object */
object V {
  val riddl = "0.20.0"
  val scalatest = "3.2.9"
  val scalacheck = "1.16.0"
  val testkit = "0.7.1"
}

object Dep {
  val scalactic = "org.scalactic" %% "scalactic" % V.scalatest
  val scalatest = "org.scalatest" %% "scalatest" % V.scalatest
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck
  val hugo = "com.reactific" %% "riddl-hugo" % V.riddl
  val testkit = "com.reactific" %% "riddl-testkit" % V.riddl

  val testing: Seq[ModuleID] = Seq(
    scalactic % "test",
    scalatest % "test",
    scalacheck % "test",
    hugo % "test",
    testkit % "test"
  )

}

