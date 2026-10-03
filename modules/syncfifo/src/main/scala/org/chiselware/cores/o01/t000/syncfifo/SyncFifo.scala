// (c) 2026 Rocksavage Technology, Inc.
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o01.t000.syncfifo

import _root_.circt.stage.ChiselStage
import chisel3._
import org.chiselware.ipf.{ IpfJsonFile, ParamCli }
import org.chiselware.syn.{ RunScriptFile, StaTclFile, YosysTclFile }

/** Synchronous FIFO with asynchronous reset
  *
  * @constructor
  *   create a new SyncFifo
  * @param width
  *   defines the data width of the SyncFifo
  * @author
  *   Warren Savage
  * @todo
  *
  * @see
  *   [[http://www.rocksavage.tech]] for more information.
  *
  * <img src="doc/images/user-guide/dff-block-diagram.png" />
  */

object SyncFifo {
  def apply(params: SyncFifoParams): SyncFifo = Module(new SyncFifo(params))
}

class SyncFifo(p: SyncFifoParams) extends Module {
  val io = IO(new Bundle {
    val d = Input(UInt(p.width.W))
    val q = Output(UInt(p.width.W))
    val enable = Input(Bool())
  })

  val q = RegInit(0.U(p.width.W))

  when(io.enable) {
    q := io.d
  }

  io.q := q
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
