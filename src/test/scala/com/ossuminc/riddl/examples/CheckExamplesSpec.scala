package com.ossuminc.riddl.examples

import com.ossuminc.riddl.examples.HugoTranslateExamplesBase

import java.nio.file.Files
import scala.sys.process.Process

/** Unit Tests To Check Documentation Examples */
class CheckExamplesSpec extends HugoTranslateExamplesBase {

  "Examples" should {
    "generate hugo for ReactiveBBQ" in {
      checkExample("ReactiveBBQ", "ReactiveBBQ.conf")
      val root = makeOutDir("ReactiveBBQ")
      Files.isDirectory(root) mustBe true
      val img = root.resolve("static/images/RBBQ.png")
      Files.exists(img) mustBe true
    }
    "generate hugo for ReactiveSummit" in {
      pending
      checkExample("ReactiveSummit", "ReactiveSummit.conf")
    }
    "generate hugo for dokn" in {
      checkExample("dokn", "dokn.conf")
    }
  }
}
