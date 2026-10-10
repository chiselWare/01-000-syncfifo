// SPDX-FileCopyrightText: Rocksavage Technology, Inc.
// SPDX-License-Identifier: Apache-2.0

package org.chiselware.cores.o01.t000.syncfifo

import chisel3._
import chisel3.util._

/** SyncFifoTb is a test bench wrapper that holds an instantiation of the
  * SyncFifo RTL and a black-box wrapper holding the memory model of the
  * external memory which is used when the core is configured as a
  * controller-only.
  *
  * @param p
  *   A customization of default parameters contained in SyncFifoParams case
  *   class.
  */

class SyncFifoTb(p: SyncFifoParams) extends Module {
  val io = IO(new Bundle {
    val push = Input(Bool())
    val pop = Input(Bool())
    val dataIn = Input(UInt(p.dataWidth.W))
    val dataOut = Output(UInt(p.dataWidth.W))
    val empty = Output(Bool())
    val full = Output(Bool())
    val almostEmpty = Output(Bool())
    val almostFull = Output(Bool())
    val almostEmptyLevel = Input(UInt(log2Ceil(p.fifoDepth).W))
    val almostFullLevel = Input(UInt(log2Ceil(p.fifoDepth).W))
  })

  val dut = Module(new SyncFifo(p))
  dut.io.push := io.push
  dut.io.pop := io.pop
  dut.io.dataIn := io.dataIn
  dut.io.almostEmptyLevel := io.almostEmptyLevel
  dut.io.almostFullLevel := io.almostFullLevel
  io.dataOut := dut.io.dataOut
  io.empty := dut.io.empty
  io.full := dut.io.full
  io.almostEmpty := dut.io.almostEmpty
  io.almostFull := dut.io.almostFull

  val ram = Module(new SramBb(p))
  ram.io.clk := clock
  ram.io.writeEnable := dut.io.ramWriteEnable
  ram.io.writeAddress := dut.io.ramWriteAddress
  ram.io.readEnable := dut.io.ramReadEnable
  ram.io.readAddress := dut.io.ramReadAddress
  ram.io.writeData := dut.io.ramDataIn
  dut.io.ramDataOut := ram.io.readData

}
