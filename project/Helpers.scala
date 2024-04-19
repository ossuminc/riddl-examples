import sbt._

/** V - Dependency Versions object */
object V {
  val riddl = "0.44.0"
  val scalatest = "3.2.18"
  val scalacheck = "1.17.1"
}

object Dep {
  val scalactic = "org.scalactic" %% "scalactic" % V.scalatest
  val scalatest = "org.scalatest" %% "scalatest" % V.scalatest
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck

  val testing: Seq[ModuleID] =
    Seq(scalactic, scalatest, scalacheck)

  val riddl: Seq[ModuleID] = Seq(
    "com.ossuminc" %% "riddl-testkit" % V.riddl,
    "com.ossuminc" %% "riddl-analyses" % V.riddl,
    "com.ossuminc" %% "riddl-passes" % V.riddl,
    "com.ossuminc" %% "riddl-language" % V.riddl,
    "com.ossuminc" %% "riddl-utils" % V.riddl
  )

}
