// (c) 2026 Rocksavage Technology, Inc.
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o01.t000.syncfifo

import chisel3._

/** SyncFifoTb is a simple test bench wrapper to hold a single instance of
  * SyncFifo.
  *
  * @param p
  *   A customization of default parameters contained in SyncFifoParams case
  *   class.
  */

class SyncFifoTb(p: SyncFifoParams) extends Module {
  val io = IO(new Bundle {
    val in = Input(UInt(p.width.W))
    val out = Output(UInt(p.width.W))
    val enable = Input(Bool())
  })

  val dut = SyncFifo(p)
  dut.io.d := io.in
  dut.io.enable := io.enable
  io.out := dut.io.q
}
