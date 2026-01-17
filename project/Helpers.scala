import sbt._

/** V - Dependency Versions object */
object V {
  val scalatest = "3.2.18"
  val scalacheck = "1.17.1"
}

object Dep {
  val scalactic = "org.scalactic" %% "scalactic" % V.scalatest
  val scalatest = "org.scalatest" %% "scalatest" % V.scalatest
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck

  val testing: Seq[ModuleID] =
    Seq(scalactic, scalatest, scalacheck)
}
