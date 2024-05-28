package com.ossuminc.riddl.examples

import com.ossuminc.riddl.commands.CommandPlugin
import com.ossuminc.riddl.testkit.ValidatingTest
import com.ossuminc.riddl.language.CommonOptions
import com.ossuminc.riddl.utils.SysLogger
import org.scalatest.Assertion
import org.scalatest.Assertions.fail

import java.nio.file.{Files, Path}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class HugoTranslateExamplesBase extends ValidatingTest {

  val directory: String = "src/riddl/"
  val output: String = "target/hugo"

  val commonOptions: CommonOptions =  CommonOptions(
    showTimes = true,
    showWarnings = true,
    showMissingWarnings = false,
    showStyleWarnings = false
  )

  private def makeSrcDir(testName: String): Path = Path.of(directory).resolve(testName)

  def makeOutDir(testName: String): Path = Path.of(output).resolve(testName)

  private def makeConfFile(testName: String, confFile: String): Path = makeSrcDir(testName).resolve(confFile)

  private def genHugo(testName: String, confFile: String): Unit = {
    val outDir = makeOutDir(testName)
    val sourcePath = makeSrcDir(testName)
    if !Files.isDirectory(outDir) then
      Files.createDirectories(outDir)
    val options = Array(
      "from", makeConfFile(testName, confFile).toString, "hugo"
    )
    CommandPlugin.runMain(options, SysLogger())
  }

  private def runHugo(testName: String): Assertion = {
    import scala.sys.process._
    val lineBuffer: mutable.ArrayBuffer[String] = ArrayBuffer[String]()
    var hadErrorOutput: Boolean = false
    var hadWarningOutput: Boolean = false

    def fout(line: String): Unit = {
      lineBuffer.append(line)
      if !hadWarningOutput && line.contains("WARN") then
        hadWarningOutput = true
    }

    def ferr(line: String): Unit = {
      lineBuffer.append(line)
      hadErrorOutput = true
    }

    val logger = ProcessLogger(fout, ferr)
    val srcDir = makeOutDir(testName)
    assert(Files.isDirectory(srcDir), s"Not a source dir: $srcDir")
    val cwdFile = srcDir.toFile
    val proc = Process("hugo", cwd = Option(cwdFile))
    proc.!(logger) match {
      case 0 =>
        if hadErrorOutput then
          fail("hugo wrote to stderr:\n  " + lineBuffer.mkString("\n  "))
        else if hadWarningOutput then
          info("hugo issued warnings:\n  " + lineBuffer.mkString("\n  "))
          succeed
        else
          succeed
      case rc: Int =>
        fail(s"hugo run failed with rc=$rc:\n  " + lineBuffer.mkString("\n  "))
    }
  }

  def checkExample(testName: String, confFile: String): Assertion = {
    genHugo(testName, confFile)
    runHugo(testName)
  }
}
