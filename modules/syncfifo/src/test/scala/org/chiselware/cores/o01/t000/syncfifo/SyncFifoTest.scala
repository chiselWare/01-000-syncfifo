// SPDX-FileCopyrightText: Rocksavage Technology, Inc.
// SPDX-License-Identifier: Apache-2.0

package org.chiselware.cores.o01.t000.syncfifo

import chisel3._
import chiseltest._
import firrtl2.options.TargetDirAnnotation
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import scala.util.Random

import SyncFifoTestUtils._

class SyncFifoTest extends AnyFlatSpec with Matchers
    with ChiselScalatestTester {

  // For debugging to show push/pop data in sequence
  val verbose = false

  // Execute the main test for each configuration
  for ((configName, config) <- SyncFifoParams.simConfigMap) {
    main(configName = configName, p = config)
  }

  val scalaCoverageDir = new File("generated/scalaCoverage")
  scalaCoverageDir.mkdir()

  // Test that illegal configuration combinations are caught
  behavior of "Configuration assertions"
  it should "throw an assertion when dataWidth is less than 4" in {
    val e = intercept[IllegalArgumentException] {
      SyncFifoParams(
        dataWidth = 1
      )
    }
    assert(e.getMessage.contains(SyncFifoParams().dataWidthMsg))
  }

  it should "throw an assertion when fifoDepth is not a power of 2" in {
    val e = intercept[IllegalArgumentException] {
      SyncFifoParams(
        fifoDepth = 7
      )
    }
    assert(e.getMessage.contains(SyncFifoParams().fifoDepthMsg))
  }

  /** Main test function executes one complete test for one configuration */
  def main(
      configName: String,
      p: SyncFifoParams
    ): Unit = {

    behavior of s"SyncFifo directed tests (config: $configName)"

    val backendAnnotations = Seq(
      WriteVcdAnnotation,
      // WriteFstAnnotation,
      VerilatorBackendAnnotation,
      // IcarusBackendAnnotation,
      // VcsBackendAnnotation,
      TargetDirAnnotation("modules/syncfifo/generated")
    )

    // Randomize amount of data and flag thresholds
    val testDataSize =
      p.fifoDepth * Random.between(minInclusive = 10, maxExclusive = 20)
    val fullThreshold =
      (p.fifoDepth * Random.between(minInclusive = 75, maxExclusive = 95) /
        100).toInt
    val emptyThreshold =
      (p.fifoDepth * Random.between(minInclusive = 3, maxExclusive = 25) /
        100).toInt

    info(s"Test data size  : $testDataSize")
    info(s"Full threshold  : $fullThreshold")
    info(s"Empty threshold : $emptyThreshold")

    it should "perform these tests successfully " in {
      test(new SyncFifoTb(p))
        .withAnnotations(annotationSeq = backendAnnotations) { dut =>
          // Initialize the inputs
          dut.io.push.poke(false.B)
          dut.io.pop.poke(false.B)
          dut.io.almostFullLevel.poke(fullThreshold.U)
          dut.io.almostEmptyLevel.poke(emptyThreshold.U)
          dut.io.dataIn.poke(0.U)

          // Reset sequence
          dut.reset.poke(true.B)
          dut.clock.step()
          dut.reset.poke(false.B)

          /* Create a buffer of randomized test data to apply in the test
           *
           * First create a random number to use in the tests. The newRand()
           * function allows for debugging by being able to pass a specific seed
           * to the test as follows:
           *
           * sbt -Dseed=123456789 "testOnly *SyncFifoTest"
           *
           * Then fill a buffer with randomized data. This is the data that
           * will be pushed into the FIFO during the tests.
           */
          val randData = newRand(verbose)
          val testDataBuffer =
            Seq.fill(testDataSize)(BigInt(
              p.dataWidth,
              randData
            ).U(p.dataWidth.W))

          // Directed tests.  Fill up the FIFO then read it back
          stack.clear()
          testDataBuffer.foreach { data =>
            if (!isFull(dut = dut)) {
              stack.push(1)
              push(dut = dut, pushData = data, verbose = verbose)
              checkFlags(
                dut = dut,
                p = p,
                emptyThreshold = emptyThreshold,
                fullThreshold = fullThreshold
              )
            }
          }

          testDataBuffer.foreach { data =>
            if (!isEmpty(dut)) {
              stack.pop()
              pop(dut = dut, expectedData = data, verbose = verbose)
              checkFlags(
                dut = dut,
                p = p,
                emptyThreshold = emptyThreshold,
                fullThreshold = fullThreshold
              )
            }
          }

          // Create a queue for tracking what data is pushed/popped
          val expectedData = scala.collection.mutable.Queue[UInt]()
          stack.clear()

          /** Random tests. Randomly push/pop data to emulate backpressure in a
            * realsystem. Slightly more priority is given to push to insure FIFO
            * fills to full at some point for flag testing.
            */
          testDataBuffer.foreach { data =>
            fork {
              if (!isFull(dut)) {
                if (math.random() < 0.75) {
                  expectedData.enqueue(data)
                  stack.push(1)
                  push(dut = dut, pushData = data, verbose = verbose)
                  checkFlags(
                    dut = dut,
                    p = p,
                    emptyThreshold = emptyThreshold,
                    fullThreshold = fullThreshold
                  )
                }
              }
            }.fork {
              if (!isEmpty(dut)) {
                if (math.random() < 0.55) {
                  val expected = expectedData.dequeue()
                  stack.pop()
                  pop(dut = dut, expectedData = expected, verbose = verbose)
                  checkFlags(
                    dut = dut,
                    p = p,
                    emptyThreshold = emptyThreshold,
                    fullThreshold = fullThreshold
                  )
                }
              }
            }.join()
          }
        }
    }
  }
}
