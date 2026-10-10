// SPDX-FileCopyrightText: Rocksavage Technology, Inc.
// SPDX-License-Identifier: Apache-2.0

package org.chiselware.cores.o01.t000.syncfifo

import chisel3._
import chiseltest._

import scala.collection.mutable.Stack
import scala.util.Random

object SyncFifoTestUtils {

  def newRand(verbose: Boolean): Random = {
    val seed = sys.props.get(
      "seed"
    ).map(_.toLong).getOrElse(System.nanoTime())
    if (verbose) {
      println(s"Random seed: $seed")
    }
    new Random(seed)
  }

  def push(
      dut: SyncFifoTb,
      pushData: UInt,
      verbose: Boolean = false
    ): Unit = {
    dut.io.push.poke(true.B)
    dut.io.dataIn.poke(pushData)
    val pushedData = dut.io.dataIn.peek().litValue.toString
    if (verbose) { println(s"pushed $pushedData") }
    dut.clock.step()
    dut.io.push.poke(false.B)
  }

  def pop(
      dut: SyncFifoTb,
      expectedData: UInt,
      verbose: Boolean = false
    ): Unit = {
    dut.io.pop.poke(true.B)
    val poppedData = dut.io.dataOut.peek().litValue.toString
    if (verbose) { println(s"\t\t popped $poppedData") }
    dut.io.dataOut.expect(expectedData)
    dut.clock.step()
    dut.io.pop.poke(false.B)
  }

  def isEmpty(dut: SyncFifoTb): Boolean = {
    dut.io.empty.peek().litValue == 1
  }
  def isFull(dut: SyncFifoTb): Boolean = {
    dut.io.full.peek().litValue == 1
  }

  // Use Scala Stack to mirror the FIFO operations to check flags
  var stack = Stack[Int]()
  def checkFlags(
      dut: SyncFifoTb,
      p: SyncFifoParams,
      emptyThreshold: Int,
      fullThreshold: Int
    ): Unit = {
    dut.io.empty.expect(stack.size == 0)
    dut.io.almostEmpty.expect(stack.size <= emptyThreshold)
    dut.io.full.expect(stack.size == p.fifoDepth)
    dut.io.almostFull.expect(stack.size >= fullThreshold)
  }
}
