package vexriscv.wob

import vexriscv._
import vexriscv.plugin._
import spinal.core._

object GenWobFull extends App {
  val h = if (args.length > 0) args(0).toInt else 24
  println(s"=== Using percHistory = $h ===")
  val config = VexRiscvConfig(
    plugins = List(
      new IBusSimplePlugin(
        resetVector = 0x00000000l,
        cmdForkOnSecondStage = true,
        cmdForkPersistence = true,
        prediction = STATIC
      ),
      new DBusSimplePlugin(
        catchAddressMisaligned = false,
        catchAccessFault = false
      ),
      new DecoderSimplePlugin(
        catchIllegalInstruction = false
      ),
      new RegFilePlugin(
        regFileReadyKind = plugin.SYNC,
        zeroBoot = true
      ),
      new IntAluPlugin,
      new SrcPlugin(
        separatedAddSub = false,
        executeInsertion = true
      ),
      new LightShifterPlugin,
      new HazardSimplePlugin(
        bypassExecute = false,
        bypassMemory = false,
        bypassWriteBack = false,
        bypassWriteBackBuffer = false
      ),
      new WobPlugin(
        bimodalSize = 1024,
        ecSize      = 1024,
        percRows    = 1024,
        percHistory = h,
        percBits    = 8
      ),
      new BranchPlugin(
        earlyBranch = true,
        catchAddressMisaligned = false
      )
    )
  )

  SpinalVerilog(new VexRiscv(config))
}

