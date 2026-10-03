// (c) 2026 Rocksavage Technology, Inc.
// This code is licensed under the <name of license> (see LICENSE.MD)

package org.chiselware.cores.o01.t000.syncfifo

import chiseltest._
import chiseltest.formal._
import firrtl2.options.TargetDirAnnotation
import org.scalatest.Tag
import org.scalatest.flatspec.AnyFlatSpec

/** Tag to allow formal tests to be run separately from simulation tests.
  *
  * sbt "testOnly * -- -l FormalTest" (exclude formal)
  *
  * sbt "testOnly * -- -n FormalTest" (formal only)
  */
object FormalTest extends Tag("FormalTest")

/** Formal test spec for SyncFifo.
  *
  * BoundedCheck(k): BMC for k cycles after reset. Increase for deeper cores.
  *
  * Runs across all simConfigMap entries — one property set, all configs
  */
class SyncFifoFormalTest extends AnyFlatSpec with ChiselScalatestTester
    with Formal {

  val bound = BoundedCheck(10)

  behavior of "SyncFifo formal"

  for ((configName, config) <- SyncFifoParams.simConfigMap) {
    it should s"pass all formal properties ($configName)" taggedAs FormalTest in {
      verify(
        new SyncFifoFormalTb(config),
        Seq(
          bound,
          TargetDirAnnotation(s"modules/syncfifo/generated/formal/$configName")
        )
      )
    }
  }
}
