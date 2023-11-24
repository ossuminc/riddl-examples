package com.reactific.riddl.examples

import com.reactific.riddl.hugo.{HugoCommand, HugoPass}
import com.reactific.riddl.testkit.ValidatingTest
import com.reactific.riddl.language.{CommonOptions, Messages}
import com.reactific.riddl.passes.PassesResult
import com.reactific.riddl.utils.SysLogger
import org.scalatest.Assertion

import java.nio.file.{Files, Path}
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class HugoTranslateExamplesBase extends ValidatingTest {

  val directory: String = "src/riddl/"
  val output: String

  def makeSrcDir(path: String): Path = {
    Path.of(output).resolve(path)
  }
  val commonOptions: CommonOptions =  CommonOptions(
    showTimes = true,
    showWarnings = true,
    showMissingWarnings = false,
    showStyleWarnings = false
  )

  def genHugo(projectName: String, source: String):
  Either[Messages.Messages, PassesResult] = {
    val outDir = Path.of(output).resolve(source)
    val outDirFile = outDir.toFile
    if (!outDirFile.isDirectory) outDirFile.mkdirs()
    val sourcePath = Path.of(directory).resolve(source)
    val htc = HugoCommand.Options(
      inputFile = Some(sourcePath),
      outputDir = Some(outDir),
      eraseOutput = true,
      projectName = Some(projectName)
    )
    val ht = HugoPass(input, state)
    ht.parseValidateTranslate(SysLogger(), commonOptions, htc)
  }

  def runHugo(source: String): Assertion = {
    import scala.sys.process._
    val lineBuffer: mutable.ArrayBuffer[String] = ArrayBuffer[String]()
    var hadErrorOutput: Boolean = false
    var hadWarningOutput: Boolean = false

    def fout(line: String): Unit = {
      lineBuffer.append(line)
      if (!hadWarningOutput && line.contains("WARN")) hadWarningOutput = true
    }

    def ferr(line: String): Unit = { lineBuffer.append(line); hadErrorOutput = true }

    val logger = ProcessLogger(fout, ferr)
    val srcDir = makeSrcDir(source)
    Files.isDirectory(srcDir)
    val cwdFile = srcDir.toFile
    val proc = Process("hugo", cwd = Option(cwdFile))
    proc.!(logger) match {
      case 0 =>
        if (hadErrorOutput) { fail("hugo wrote to stderr:\n  " + lineBuffer.mkString("\n  ")) }
        else if (hadWarningOutput) {
          fail("hugo issued warnings:\n  " + lineBuffer.mkString("\n  "))
        } else { succeed }
      case rc: Int => fail(s"hugo run failed with rc=$rc:\n  " + lineBuffer.mkString("\n  "))
    }
  }

  def checkExamples(name: String, path: String): Assertion = {
    genHugo(name, path) match {
      case Right(_) =>
        runHugo(path)
      case Left(messages) => fail(messages.format)
    }
  }
}
