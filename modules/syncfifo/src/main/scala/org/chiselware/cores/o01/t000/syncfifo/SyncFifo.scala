// SPDX-FileCopyrightText: Rocksavage Technology, Inc.
// SPDX-License-Identifier: Apache-2.0

package org.chiselware.cores.o01.t000.syncfifo

import _root_.circt.stage.ChiselStage
import chisel3._
import chisel3.util._
import org.chiselware.ipf.{ IpfJsonFile, ParamCli }
import org.chiselware.syn.{ RunScriptFile, StaTclFile, YosysTclFile }

object SyncFifo {
  def apply(params: SyncFifoParams): SyncFifo = Module(new SyncFifo(params))
}

/** A synchronous FIFO and FIFO controller with dynamic flags
  *
  * <img src="SyncFifo-blockdiagram.png" />
  *
  * @constructor
  *   create a new FIFO or FIFO controller
  * @param externalRam
  *   creates just the FIFO controller (uses ext. SRAM)
  * @param dataWidth
  *   defines the data width of the FIFO
  * @param fifoDepth
  *   defines the depth of the FIFO (must be a power of 2)
  * @author
  *   Warren Savage
  * @see
  *   [[http://www.rocksavage.tech/]] for more information
  */

class SyncFifo(p: SyncFifoParams) extends Module {

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
    val ramWriteEnable = Output(Bool())
    val ramWriteAddress = Output(UInt(log2Ceil(p.fifoDepth).W))
    val ramDataIn = Output(UInt(p.dataWidth.W))
    val ramReadEnable = Output(Bool())
    val ramReadAddress = Output(UInt(log2Ceil(p.fifoDepth).W))
    val ramDataOut = Input(UInt(p.dataWidth.W))
  })

  val fifoMemory = RegInit(VecInit(Seq.fill(p.fifoDepth)(0.U(p.dataWidth.W))))

  val tail = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))
  val head = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))
  val count = RegInit(0.U(log2Ceil(p.fifoDepth + 1).W))

  when((io.push === 1.U) && (io.full =/= 1.U)) { count := count + 1.U }
  when((io.pop === 1.U) && (io.empty =/= 1.U)) { count := count - 1.U }
  when((io.pop === 1.U) && (io.push === 1.U)) { count := count }

  io.empty := count === 0.U
  io.full := count === p.fifoDepth.U
  io.almostEmpty := count <= io.almostEmptyLevel
  io.almostFull := count >= io.almostFullLevel

  when(io.pop && !io.empty) {
    tail := (tail + 1.U)
  }

  when(io.push && !io.full) {
    fifoMemory(head) := io.dataIn
    head := (head + 1.U)
  }

  io.dataOut := Mux(p.externalRam.B, io.ramDataOut, fifoMemory(tail))
  io.ramDataIn := io.dataIn
  io.ramReadAddress := tail
  io.ramWriteAddress := head
  io.ramReadEnable := io.pop
  io.ramWriteEnable := io.push
}

/** Generate Verilog and related collateral for each configuration to be used as
  * part of the regression framework.
  */
object Main extends App {
  val coreDir = s"modules/${SyncFifoParams.MainClassName.toLowerCase()}"

  SyncFifoParams.synConfigMap.foreach { case (configName, configParams) =>
    val myOpts = Array(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays",
      "--disable-all-randomization",
      "--strip-debug-info",
      "--split-verilog",
      s"-o=${coreDir}/generated/synTestCases/$configName"
    )
    println()
    println(s"Generating Verilog for config: $configName")
    ChiselStage.emitSystemVerilog(
      gen = new SyncFifo(p = configParams),
      firtoolOpts = myOpts
    )

    // Generate synthesis files and scripts
    SdcFile.create(
      p = configParams,
      sdcFilePath = s"${coreDir}/generated/synTestCases/$configName"
    )
    YosysTclFile.create(
      mainClassName = SyncFifoParams.MainClassName,
      synTestDir = s"${coreDir}/generated/synTestCases/$configName"
    )
    StaTclFile.create(
      mainClassName = SyncFifoParams.MainClassName,
      synTestDir = s"${coreDir}/generated/synTestCases/$configName"
    )
    RunScriptFile.create(
      mainClassName = SyncFifoParams.MainClassName,
      configs = SyncFifoParams.synConfigs,
      runDir = s"${coreDir}/generated/synTestCases"
    )
  }
}

/** Generate artifacts for the IP Factory to download upon a user request.
  * ```
  * This includes
  * - A single Verilog configuration consisting of a filelist.f & Verilog files
  * - An sdc file
  * - The core's User Guide (in PDF)
  * - A JSON file
  *
  * This is executed by the following SBT command line:
  *
  * sbt "project core" "runMain org.chiselware.cores.o00.t000.dff.GenVerWithParamCli -- --params='(width=8)'"
  * ```
  */
object GenVerWithParamCli extends App {
  val p = ParamCli.parseParams(args)
  val params = SyncFifoParams.fromMap(p)

  ChiselStage.emitSystemVerilog(
    new SyncFifo(params),
    firtoolOpts = Array(
      "--lowering-options=disallowLocalVariables,disallowPackedArrays",
      "--disable-all-randomization",
      "--strip-debug-info",
      "--split-verilog",
      s"-o=.ipf"
    )
  )

  SdcFile.create(
    p = params,
    sdcFilePath = ".ipf"
  )

  IpfJsonFile.create(
    p = IpfParamsMap.params,
    jsonFilePath = s".ipf/${SyncFifoParams.MainClassName}.json"
  )

}
