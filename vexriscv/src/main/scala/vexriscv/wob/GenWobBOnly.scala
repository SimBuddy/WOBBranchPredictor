package vexriscv.wob

import vexriscv._
import vexriscv.plugin._
import spinal.core._

object GenWobBOnly extends App {
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
      new BranchPlugin(
        earlyBranch = true,
        catchAddressMisaligned = false
      ),
      new WobPlugin(mode = "B_ONLY")
    )
  )

  SpinalVerilog(new VexRiscv(config))
}

