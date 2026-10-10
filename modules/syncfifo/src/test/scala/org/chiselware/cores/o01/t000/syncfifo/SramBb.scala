// SPDX-FileCopyrightText: Rocksavage Technology, Inc.
// SPDX-License-Identifier: Apache-2.0

package org.chiselware.cores.o01.t000.syncfifo

import chisel3._
import chisel3.util._

/** Blackbox to hold Verilog simulation model */
class SramBb(p: SyncFifoParams)
    extends BlackBox(
      Map("ADDR_WIDTH" -> log2Ceil(p.fifoDepth), "DATA_WIDTH" -> p.dataWidth)
    )
    with HasBlackBoxResource {
  override val desiredName = "simple_dual_one_clock"
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val readEnable = Input(Bool())
    val writeEnable = Input(Bool())
    val readAddress = Input(UInt(log2Ceil(p.fifoDepth).W))
    val writeAddress = Input(UInt(log2Ceil(p.fifoDepth).W))
    val writeData = Input(UInt(p.dataWidth.W))
    val readData = Output(UInt(p.dataWidth.W))

    for (file <- p.bbFiles)
      addResource(s"vsrc/${file}")

  })
}
