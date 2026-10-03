// (c) 2026 Rocksavage Technology, Inc.
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o01.t000.syncfifo

import org.chiselware.ipf.{ FieldType, IpfParams }

import java.io.{ File, PrintWriter }
import scala.collection.mutable.LinkedHashMap

/** Default parameter settings for SyncFifo
  *
  * @constructor
  *   default parameter settings
  * @param width
  *   specifies the width of the SyncFifo
  * @author
  *   Warren Savage
  * @see
  *   [[http://www.mycompany.com]] for more information
  */
case class SyncFifoParams(
    width: Int = 1) {
  val widthMsg = "width must be greater than or equal 1"
  require(width >= 1, widthMsg)
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
    "config8" -> SyncFifoParams(width = 8),
    "config32" -> SyncFifoParams(width = 32),
    "config64" -> SyncFifoParams(width = 64)
  )

  val synConfigMap = LinkedHashMap[String, SyncFifoParams](
    "small_1" -> SyncFifoParams(width = 1),
    "medium_64" -> SyncFifoParams(width = 64),
    "large_128" -> SyncFifoParams(width = 128)
  )

  // Extract config names into a space-separated string
  val synConfigs = SyncFifoParams.synConfigMap
    .map { case (configName, config) => s"$configName" }
    .mkString(" ")

  // Package parameter names for import to IP Factory
  def fromMap(m: Map[String, String]): SyncFifoParams = {
    val width = m.get("width").map(_.toInt).getOrElse(1)
    // add all additional synthesis parameters here
    SyncFifoParams(width = width) // extend with more params as needed
  }
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
      |set_input_delay -clock clock $inputDelay {io_d}
      |set_input_delay -clock clock $inputDelay {io_enable}
      |set_output_delay -clock clock $outputDelay {io_q}
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
    "width" ->
      IpfParams(
        fieldType = FieldType.Field,
        pickList = List.empty,
        message = Some(SyncFifoParams().widthMsg)
        // add remaining parameters as needed, here are some examples
        // "useFifo" -> IpfParams(FieldType.Toggle, List.empty, None),
        // "cacheSize" -> IpfParams(FieldType.Pick, List("32", "64", "128"), None)
      )
  )
}
