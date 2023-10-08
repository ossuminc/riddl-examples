import sbt._

/** V - Dependency Versions object */
object V {
  val scalatest = "3.2.9"
  val scalacheck = "1.16.0"
  val testkit = "0.7.1"
}

object Dep {
  val scalactic = "org.scalactic" %% "scalactic" % V.scalatest
  val scalatest = "org.scalatest" %% "scalatest" % V.scalatest
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck

  val testing: Seq[ModuleID] = Seq(
    scalactic % "test",
    scalatest % "test",
    scalacheck % "test",
  )

}

