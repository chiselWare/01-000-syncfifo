// SPDX-FileCopyrightText: Rocksavage Technology, Inc.
// SPDX-License-Identifier: Apache-2.0

package org.chiselware.cores.o01.t000.syncfifo

import org.chiselware.ipf.{ FieldType, IpfParams }

import java.io.{ File, PrintWriter }
import scala.collection.mutable.LinkedHashMap

/** Default parameter settings for SyncFifo
  *
  * @constructor
  *   default parameter settings
  * @param dataWidth
  *   specifies the width of the FIFO data
  * @param fifoDepth
  *   specifices the depth of the FIFO
  * @param externalRam
  *   specifies whether to use an external SRAM or generate internal FFs
  * @author
  *   Warren Savage
  *
  * @see
  *   [[http://www.rocksavage.tech]] for more information
  */

case class SyncFifoParams(
    dataWidth: Int = 8,
    fifoDepth: Int = 8,
    externalRam: Boolean = false,
    bbFiles: List[String] = List("dual_port_sync_sram.v")) {

  val dataWidthMsg = "Width must be greater than or equal 4"
  require(dataWidth >= 4, dataWidthMsg)
  val fifoDepthMsg = "Depth must be a power of 2"
  require((fifoDepth % 2) == 0, fifoDepthMsg)
}

/** Define a companion object to hold a Map of the configurations and the order
  * in which to be tested.
  *
  * Provide a meaningful configuration name for each set of parameter
  * configurations.
  * ```
  *  For sim, use lowerCamel case conventions: myConfig1, etc.
  *  For syn, use snake_case, typical for Verilog: my_config_1
  *
  *  Note that these are constants and thus follow the UpperCamelCase
  *  convention.
  * ```
  */
object SyncFifoParams {
  val MainClassName = "SyncFifo"
  val simConfigMap = LinkedHashMap[String, SyncFifoParams](
    "small_8_4_int" -> SyncFifoParams(
      dataWidth = 8,
      fifoDepth = 4,
      externalRam = false
    ),
    "medium_32_64_int" -> SyncFifoParams(
      dataWidth = 32,
      fifoDepth = 64,
      externalRam = false
    ),
    "large_128_256_ext" -> SyncFifoParams(
      dataWidth = 128,
      fifoDepth = 256,
      externalRam = true
    )
  )
  val synConfigMap = LinkedHashMap[String, SyncFifoParams](
    "small_8_4_int" -> SyncFifoParams(
      dataWidth = 8,
      fifoDepth = 4,
      externalRam = false
    ),
    "medium_32_64_int" -> SyncFifoParams(
      dataWidth = 32,
      fifoDepth = 64,
      externalRam = false
    ),
    "large_128_256_ext" -> SyncFifoParams(
      dataWidth = 128,
      fifoDepth = 256,
      externalRam = true
    )
  )

  // Extract config names into a space-separated string
  val synConfigs = SyncFifoParams.synConfigMap
    .map { case (configName, config) => s"$configName" }
    .mkString(" ")

  // Package parameter names for import to IP Factory
  def fromMap(m: Map[String, String]): SyncFifoParams =
    SyncFifoParams(
      dataWidth = m("dataWidth").toInt,
      fifoDepth = m("fifoDepth").toInt,
      externalRam = m("externalRam").toBoolean
    )
}

/** Customize this companion object with your port list and desired synthesis
  * contraints.
  */
object SdcFile {
  def create(
      p: SyncFifoParams,
      sdcFilePath: String
    ): Unit = {
    // Default constraints, tighten or loosen as necessary
    val period = 5.000 // ns
    val dutyCycle = 0.50
    val inputDelayPct = 0.2
    val outputDelayPct = 0.2

    // Calculated constraints, customize as needed in SdcFileData
    val inputDelay = period * inputDelayPct
    val outputDelay = period * outputDelayPct
    val fallingEdge = period * dutyCycle

    val sdcFileData =
      s"""
      |create_clock -period $period -waveform {0 $fallingEdge} clock
      |set_input_delay -clock clock $inputDelay {reset}
      |set_input_delay -clock clock $inputDelay {io_push}
      |set_input_delay -clock clock $inputDelay {io_pop}
      |set_input_delay -clock clock $inputDelay {io_dataIn}
      |set_output_delay -clock clock $outputDelay {io_dataOut}
      |set_output_delay -clock clock $outputDelay {io_empty}
      |set_output_delay -clock clock $outputDelay {io_full}
      |set_input_delay -clock clock $inputDelay {io_almostEmptyLevel}
      |set_input_delay -clock clock $inputDelay {io_almostFullLevel}
      |set_output_delay -clock clock $outputDelay {io_ramWriteEnable}
      |set_output_delay -clock clock $outputDelay {io_ramWriteAddress}
      |set_input_delay -clock clock $inputDelay {io_ramReadEnable}
      |set_output_delay -clock clock $outputDelay {io_ramReadAddress}
      |set_input_delay -clock clock $inputDelay {io_ramDataOut}
    """.stripMargin.trim

    println(s"Writing SDC file to $sdcFilePath")
    val sdcFileDir = new File(sdcFilePath)
    sdcFileDir.mkdirs()
    val sdcFileName = new File(s"$sdcFilePath/SyncFifo.sdc")
    val sdcFile = new PrintWriter(sdcFileName)
    sdcFile.write(s"${sdcFileData}")
    sdcFile.close()
  }

}

object IpfParamsMap {
  val params = Map(
    "datawidth" ->
      IpfParams(
        fieldType = FieldType.Field,
        pickList = List.empty,
        message = Some(SyncFifoParams().dataWidthMsg)
        // add remaining parameters as needed, here are some examples
        // "useFifo" -> IpfParams(FieldType.Toggle, List.empty, None),
        // "cacheSize" -> IpfParams(FieldType.Pick, List("32", "64", "128"), None)
      ),
    "fifoDepth" ->
      IpfParams(
        fieldType = FieldType.Field,
        pickList = List.empty,
        message = Some(SyncFifoParams().fifoDepthMsg)
        // add remaining parameters as needed, here are some examples
        // "useFifo" -> IpfParams(FieldType.Toggle, List.empty, None),
        // "cacheSize" -> IpfParams(FieldType.Pick, List("32", "64", "128"), None)
      ),
    "externalRam" ->
      IpfParams(
        fieldType = FieldType.Toggle,
        pickList = List.empty,
        message = None
        // add remaining parameters as needed, here are some examples
        // "useFifo" -> IpfParams(FieldType.Toggle, List.empty, None),
        // "cacheSize" -> IpfParams(FieldType.Pick, List("32", "64", "128"), None)
      )
  )
}
