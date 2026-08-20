package vexriscv.wob

import vexriscv._
import vexriscv.plugin._
import spinal.core._
import spinal.lib._

class WobPlugin(
  bimodalSize   : Int = 1024,
  ecSize        : Int = 1024,
  percRows      : Int = 1024,
  percHistory   : Int = 24,
  percBits      : Int = 8,
  mode          : String = "WOB"
) extends Plugin[VexRiscv] {

  object WOB_IS_COND        extends Stageable(Bool)
  object WOB_BIMODAL_CONF   extends Stageable(UInt(2 bits))
  object WOB_EC_ENTRY       extends Stageable(UInt(2 bits))
  object WOB_SELECTOR_FIRES extends Stageable(Bool)
  object WOB_PERC_ROW       extends Stageable(UInt(10 bits))
  object WOB_FINAL_PRED     extends Stageable(Bool)
  object WOB_PC             extends Stageable(UInt(32 bits))
  object WOB_A_PRED         extends Stageable(Bool)
  object WOB_B_PRED         extends Stageable(Bool)
  object WOB_DOT_OUTPUT     extends Stageable(SInt(32 bits))
  object WOB_HIST_SNAP      extends Stageable(Bits(historyWidth bits))
  object WOB_B_TRAIN        extends Stageable(Bool)

  val historyWidth = percHistory
  val THETA = 60

  private def satInc(v: SInt): SInt =
    Mux(v === S(127, percBits bits), S(127, percBits bits), (v + S(1)).resize(percBits))

  private def satDec(v: SInt): SInt =
    Mux(v === S(-128, percBits bits), S(-128, percBits bits), (v - S(1)).resize(percBits))

  override def setup(pipeline: VexRiscv): Unit = {}
  override def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    val branchPlugin = pipeline.plugins.collectFirst { case bp: BranchPlugin => bp }.get
    val BRANCH_DO = branchPlugin.BRANCH_DO
    val BRANCH_COND_RESULT = branchPlugin.BRANCH_COND_RESULT
    val decodePredictionBus = branchPlugin.decodePrediction

    val global = pipeline plug new Area {
      val bimodalTable = Mem(UInt(2 bits), bimodalSize).addAttribute(Verilator.public)
      val ecTable      = Mem(UInt(2 bits), ecSize).addAttribute(Verilator.public)
      val biasMem      = Mem(SInt(percBits bits), percRows).addAttribute(Verilator.public)
      val weightMems   = for (_ <- 0 until historyWidth) yield Mem(SInt(percBits bits), percRows).addAttribute(Verilator.public)
      val biasTrainMem    = Mem(SInt(percBits bits), percRows).addAttribute(Verilator.public)
      val weightTrainMems = for (_ <- 0 until historyWidth) yield Mem(SInt(percBits bits), percRows).addAttribute(Verilator.public)

      // Simulation visibility attributes are excluded by synthesis.
      val pendingValid  = RegInit(False).addAttribute(Verilator.public)
      val pendingRow    = Reg(UInt(10 bits)) init(0)
      val pendingVector = Reg(Bits((historyWidth + 1) * percBits bits)) init(0)
      pendingRow.addAttribute(Verilator.public)
      pendingVector.addAttribute(Verilator.public)

      // Explicit tag for the synchronous prediction result.  It records the
      // request sampled by the memories, rather than assuming the live decode
      // PC still identifies the registered output.
      val predictionReadRow = Reg(UInt(10 bits)) init(0)
      predictionReadRow.addAttribute(Verilator.public)

      // One-entry logical early update for the single-write A LUTRAM.
      val aPendingValid = RegInit(False).addAttribute(Verilator.public)
      val aPendingRow   = Reg(UInt(10 bits)) init(0)
      val aPendingValue = Reg(UInt(2 bits)) init(0)
      aPendingRow.addAttribute(Verilator.public)
      aPendingValue.addAttribute(Verilator.public)
    }

    val historyBits = RegInit(Vec.fill(historyWidth)(False))
    historyBits.addAttribute(Verilator.public)

    val decode_btfntWire   = Bool().setCompositeName(this, "decode_btfntWire").addAttribute(Verilator.public)
    val decode_wobPredWire = Bool().setCompositeName(this, "decode_wobPredWire").addAttribute(Verilator.public)

    val exec_isValid       = Bool().setCompositeName(this, "exec_isValid").addAttribute(Verilator.public)
    val exec_branchDo      = Bool().setCompositeName(this, "exec_branchDo").addAttribute(Verilator.public)
    val exec_branchCondRes = Bool().setCompositeName(this, "exec_branchCondRes").addAttribute(Verilator.public)
    val exec_isCond        = Bool().setCompositeName(this, "exec_isCond").addAttribute(Verilator.public)
    val exec_pc            = UInt(32 bits).setCompositeName(this, "exec_pc").addAttribute(Verilator.public)
    val exec_wobPred       = Bool().setCompositeName(this, "exec_wobPred").addAttribute(Verilator.public)
    val exec_selectorFires = Bool().setCompositeName(this, "exec_selectorFires").addAttribute(Verilator.public)

    decode plug new Area {
      import decode._
      val pc = input(PC)
      val bimodalPhysical = global.bimodalTable.readAsync(pc(11 downto 2))
      val bimodalConf = (global.aPendingValid && (pc(11 downto 2) === global.aPendingRow)) ? global.aPendingValue | bimodalPhysical
      val ecEntry     = global.ecTable.readAsync(pc(11 downto 2))
      val selectorFires = (bimodalConf === 0) && (ecEntry === 3)
      val idx = pc(11 downto 2)

      val readEnable = mode match {
        case "B_ONLY" => True
        case _        => selectorFires
      }

      when(readEnable) {
        global.predictionReadRow := idx
      }

      val biasRaw = global.biasMem.readSync(idx, readEnable)
      val weightRaw = (0 until historyWidth).map(i => global.weightMems(i).readSync(idx, readEnable))
      val rawVector = Bits((historyWidth + 1) * percBits bits)
      rawVector(percBits - 1 downto 0) := biasRaw.asBits
      for (i <- 0 until historyWidth) {
        rawVector((i + 2) * percBits - 1 downto (i + 1) * percBits) := weightRaw(i).asBits
      }
      val predictionForward = readEnable && global.pendingValid && (global.predictionReadRow === global.pendingRow)
      val logicalVector = predictionForward ? global.pendingVector | rawVector
      val biasVal = logicalVector(percBits - 1 downto 0).asSInt

      val dotProduct = (0 until historyWidth).foldLeft(biasVal) { case (acc, i) =>
        val w = logicalVector((i + 2) * percBits - 1 downto (i + 1) * percBits).asSInt
        Mux(historyBits(i), acc + w, acc - w)
      }
      val aPred = bimodalConf.msb
      val percPred = dotProduct >= S(0)

      val wobPred = mode match {
        case "A_ONLY" => aPred
        case "B_ONLY" => percPred
        case _        => selectorFires ? percPred | aPred
      }

      val isCond = (input(INSTRUCTION)(6 downto 0) === B"1100011")
      val misaligned = input(INSTRUCTION)(8)

      decode.insert(WOB_IS_COND) := isCond
      decode.insert(WOB_PC) := pc
      decode.insert(WOB_BIMODAL_CONF) := bimodalConf
      decode.insert(WOB_EC_ENTRY) := ecEntry
      decode.insert(WOB_SELECTOR_FIRES) := (mode match {
        case "A_ONLY" => False
        case "B_ONLY" => False
        case _        => selectorFires
      })
      decode.insert(WOB_PERC_ROW) := idx
      decode.insert(WOB_FINAL_PRED) := wobPred
      decode.insert(WOB_A_PRED) := aPred
      decode.insert(WOB_B_PRED) := percPred
      decode.insert(WOB_DOT_OUTPUT) := dotProduct.resize(32)
      decode.insert(WOB_HIST_SNAP) := historyBits.asBits

      decode_btfntWire   := (input(BRANCH_CTRL) === BranchCtrlEnum.JAL) || (isCond && input(INSTRUCTION)(31) && !misaligned)
      decode_wobPredWire := wobPred

      when(decode.arbitration.isValid && isCond && !misaligned) {
        decodePredictionBus.cmd.hadBranch := wobPred
      }

      if (mode != "A_ONLY") {
        when(decode.arbitration.isFiring) {
          for (i <- 0 until historyWidth - 1) {
            historyBits(i + 1) := historyBits(i)
          }
          historyBits(0) := wobPred
        }
      }
    }

    val executeArea = execute plug new Area {
      import execute._
      exec_isValid       := execute.arbitration.isValid
      exec_branchDo      := input(BRANCH_DO)
      exec_branchCondRes := input(BRANCH_COND_RESULT)
      exec_isCond        := input(WOB_IS_COND)
      exec_pc            := input(WOB_PC)
      exec_wobPred       := input(WOB_FINAL_PRED)
      exec_selectorFires := input(WOB_SELECTOR_FIRES)

      val bTrain = Bool()
      bTrain := False
      insert(WOB_B_TRAIN) := bTrain
      val trainingRow = input(WOB_PERC_ROW)
      val biasTrainRead = global.biasTrainMem.readSync(trainingRow, bTrain)
      val weightTrainReads = (0 until historyWidth).map(i => global.weightTrainMems(i).readSync(trainingRow, bTrain))
      val aEarlyValid = Bool()
      val aEarlyRow   = UInt(10 bits)
      val aEarlyValue = UInt(2 bits)
      aEarlyValid := False
      aEarlyRow := 0
      aEarlyValue := 0

      when(execute.arbitration.isValid && input(WOB_IS_COND)) {
        val actual   = input(BRANCH_COND_RESULT)
        val pred     = input(WOB_FINAL_PRED)
        val aPred    = input(WOB_A_PRED)
        val bPred    = input(WOB_B_PRED)
        val isMispred = pred =/= actual
        val consult  = input(WOB_SELECTOR_FIRES)
        val dotOut   = input(WOB_DOT_OUTPUT)
        val histSnap = input(WOB_HIST_SNAP)
        val trainCond = (dotOut <= S(THETA) && dotOut >= S(-THETA)) || isMispred
        val rowExec  = input(WOB_PERC_ROW)
        val pcIdx    = input(WOB_PC)(11 downto 2)
        bTrain := (mode match {
          case "A_ONLY" => False
          case "B_ONLY" => trainCond
          case _        => consult && trainCond
        })

        when(isMispred) {
          aEarlyValid := True
          aEarlyRow := pcIdx
          aEarlyValue := (actual ? Mux(input(WOB_BIMODAL_CONF) < 3, input(WOB_BIMODAL_CONF) + 1, input(WOB_BIMODAL_CONF))
                                 | Mux(input(WOB_BIMODAL_CONF) > 0, input(WOB_BIMODAL_CONF) - 1, input(WOB_BIMODAL_CONF)))
        }

        mode match {
          case "A_ONLY" =>

          case "B_ONLY" =>

          case _ =>
            when(aPred =/= actual) {
              global.ecTable.write(pcIdx, Mux(input(WOB_EC_ENTRY) < 3, input(WOB_EC_ENTRY) + 1, input(WOB_EC_ENTRY)))
            }
        }
      }
    }

    // A pending entry is needed for one cycle after each physical write to
    // correct the read-first outputs captured on that same edge.
    global.pendingValid := False
    global.aPendingValid := False

    memory plug new Area {
      import memory._
      val aCanonicalValid = input(WOB_IS_COND) && memory.arbitration.isValid && (mode match {
        case "B_ONLY" => input(WOB_FINAL_PRED) =/= input(BRANCH_COND_RESULT)
        case _        => True
      })
      val aCanonicalRow = input(WOB_PC)(11 downto 2)
      val aCanonicalValue = (input(BRANCH_COND_RESULT) ?
        Mux(input(WOB_BIMODAL_CONF) < 3, input(WOB_BIMODAL_CONF) + 1, input(WOB_BIMODAL_CONF)) |
        Mux(input(WOB_BIMODAL_CONF) > 0, input(WOB_BIMODAL_CONF) - 1, input(WOB_BIMODAL_CONF)))

      when(aCanonicalValid) {
        global.bimodalTable.write(aCanonicalRow, aCanonicalValue)
      }

      when(executeArea.aEarlyValid) {
        when(!(aCanonicalValid && (aCanonicalRow === executeArea.aEarlyRow))) {
          global.aPendingValid := True
          global.aPendingRow := executeArea.aEarlyRow
          global.aPendingValue := executeArea.aEarlyValue
        }
      }

      when(memory.arbitration.isValid && input(WOB_B_TRAIN)) {
        val rowMem   = input(WOB_PERC_ROW)
        val actual   = input(BRANCH_COND_RESULT)
        val histSnap = input(WOB_HIST_SNAP)

        val trainingRaw = Bits((historyWidth + 1) * percBits bits)
        trainingRaw(percBits - 1 downto 0) := executeArea.biasTrainRead.asBits
        for (i <- 0 until historyWidth) {
          trainingRaw((i + 2) * percBits - 1 downto (i + 1) * percBits) := executeArea.weightTrainReads(i).asBits
        }
        val trainingForward = global.pendingValid && (global.pendingRow === rowMem)
        val currentVector = trainingForward ? global.pendingVector | trainingRaw

        val biasOld = currentVector(percBits - 1 downto 0).asSInt
        val biasNew = actual ? satInc(biasOld) | satDec(biasOld)
        val updatedVector = Bits((historyWidth + 1) * percBits bits)
        updatedVector(percBits - 1 downto 0) := biasNew.asBits
        global.biasMem.write(rowMem, biasNew)
        global.biasTrainMem.write(rowMem, biasNew)

        for (i <- 0 until historyWidth) {
          val wOld = currentVector((i + 2) * percBits - 1 downto (i + 1) * percBits).asSInt
          val wInc = satInc(wOld)
          val wDec = satDec(wOld)
          val wNew = actual ? (histSnap(i) ? wInc | wDec) | (histSnap(i) ? wDec | wInc)
          updatedVector((i + 2) * percBits - 1 downto (i + 1) * percBits) := wNew.asBits
          global.weightMems(i).write(rowMem, wNew)
          global.weightTrainMems(i).write(rowMem, wNew)
        }

        global.pendingValid := True
        global.pendingRow := rowMem
        global.pendingVector := updatedVector
      }
    }

    pipeline plug new Area {
      val counterConsults = RegInit(U(0, 64 bits))
      val counterBypasses = RegInit(U(0, 64 bits))
      val counterTotal    = RegInit(U(0, 64 bits))
      val counterADisagree = RegInit(U(0, 64 bits))
      val counterBHelps   = RegInit(U(0, 64 bits))
      val counterBHarms   = RegInit(U(0, 64 bits))
      val counterACorrect  = RegInit(U(0, 64 bits))
      val counterAWrong    = RegInit(U(0, 64 bits))
      val counterBCorrectWhenConsulted = RegInit(U(0, 64 bits))
      val counterBWrongWhenConsulted   = RegInit(U(0, 64 bits))
      val counterBTrains   = RegInit(U(0, 64 bits))
      val counterFinalCorrect = RegInit(U(0, 64 bits))
      val counterFinalWrong   = RegInit(U(0, 64 bits))
      counterConsults.addAttribute(Verilator.public)
      counterBypasses.addAttribute(Verilator.public)
      counterTotal.addAttribute(Verilator.public)
      counterADisagree.addAttribute(Verilator.public)
      counterBHelps.addAttribute(Verilator.public)
      counterBHarms.addAttribute(Verilator.public)
      counterACorrect.addAttribute(Verilator.public)
      counterAWrong.addAttribute(Verilator.public)
      counterBCorrectWhenConsulted.addAttribute(Verilator.public)
      counterBWrongWhenConsulted.addAttribute(Verilator.public)
      counterBTrains.addAttribute(Verilator.public)
      counterFinalCorrect.addAttribute(Verilator.public)
      counterFinalWrong.addAttribute(Verilator.public)
      when(execute.arbitration.isValid && execute.input(WOB_IS_COND)) {
        counterTotal := counterTotal + 1
        when(execute.input(WOB_SELECTOR_FIRES))  { counterConsults := counterConsults + 1 }
        when(!execute.input(WOB_SELECTOR_FIRES)) { counterBypasses := counterBypasses + 1 }
        val aPred = execute.input(WOB_A_PRED)
        val bPred = execute.input(WOB_B_PRED)
        val finalPred = execute.input(WOB_FINAL_PRED)
        val actual = execute.input(BRANCH_COND_RESULT)
        val consult = execute.input(WOB_SELECTOR_FIRES)
        val effectiveConsult = mode match {
          case "A_ONLY" => False
          case "B_ONLY" => True
          case _        => consult
        }
        val dotOut = execute.input(WOB_DOT_OUTPUT)
        val trainCond = (dotOut <= S(THETA) && dotOut >= S(-THETA)) || (finalPred =/= actual)
        when(aPred === actual) { counterACorrect := counterACorrect + 1 }
        when(aPred =/= actual) { counterAWrong := counterAWrong + 1 }
        when(finalPred === actual) { counterFinalCorrect := counterFinalCorrect + 1 }
        when(finalPred =/= actual) { counterFinalWrong := counterFinalWrong + 1 }
        when(effectiveConsult && bPred === actual) { counterBCorrectWhenConsulted := counterBCorrectWhenConsulted + 1 }
        when(effectiveConsult && bPred =/= actual) { counterBWrongWhenConsulted := counterBWrongWhenConsulted + 1 }
        when(effectiveConsult && trainCond) { counterBTrains := counterBTrains + 1 }
        when(aPred =/= bPred) {
          counterADisagree := counterADisagree + 1
          when(aPred =/= actual && bPred === actual) { counterBHelps := counterBHelps + 1 }
          when(aPred === actual && bPred =/= actual) { counterBHarms := counterBHarms + 1 }
        }
      }
    }
  }
}
