/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package edu.cmu.sv.badger.analysis;

import gov.nasa.jpf.jvm.bytecode.DCMPG;
import gov.nasa.jpf.jvm.bytecode.DCMPL;
import gov.nasa.jpf.jvm.bytecode.FCMPG;
import gov.nasa.jpf.jvm.bytecode.FCMPL;
import gov.nasa.jpf.jvm.bytecode.GOTO;
import gov.nasa.jpf.jvm.bytecode.IfInstruction;
import gov.nasa.jpf.jvm.bytecode.JSR;
import gov.nasa.jpf.jvm.bytecode.LCMP;
import gov.nasa.jpf.jvm.bytecode.SwitchInstruction;
import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Yannic Noller <nolleryc@gmail.com> - YN branch instruction counting cost model
 */
public final class BranchCountState extends State {

    public final static String ID = "jumps";

    public final static class BranchBuilderFactory extends StateBuilderFactory {

        @Override
        public StateBuilder createStateBuilder() {
            return new BranchCountStateBuilder();
        }

    }

    public final static class BranchCountStateBuilder extends StateBuilderAdapter {

        public BranchCountStateBuilder() {
        }

        private BranchCountStateBuilder(double instrCount) {
            Observations.lastMeasuredMetricValue = instrCount;
        }

        @Override
        public void handleInstructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction,
                Instruction executedInstruction) {
            ChoiceGenerator<?> cg = vm.getChoiceGenerator();
            if (cg != null && cg instanceof PCChoiceGenerator) {
                if (executedInstruction.getMethodInfo().isClinit()) {
                    return;
                }
                if (executedInstruction.getMethodInfo().isInit()) {
                    return;
                }
                if (executedInstruction.getMethodInfo().getName().contains("main")) {
                    return;
                }
                if (isBranchInstruction(executedInstruction)) {
                    Observations.lastMeasuredMetricValue++;
                }
            }
        }

        private boolean isBranchInstruction(Instruction executedInstruction) {
            if (executedInstruction instanceof IfInstruction) {
                return true;
            }

            if (executedInstruction instanceof SwitchInstruction) {
                return true;
            }

            if (executedInstruction instanceof GOTO) {
                return true;
            }
            if (executedInstruction instanceof JSR) {
                return true;
            }
            if (executedInstruction instanceof LCMP) {
                return true;
            }

            if (executedInstruction instanceof LCMP) {
                return true;
            }
            if (executedInstruction instanceof FCMPL) {
                return true;
            }
            if (executedInstruction instanceof FCMPG) {
                return true;
            }
            if (executedInstruction instanceof DCMPL) {
                return true;
            }
            if (executedInstruction instanceof DCMPG) {
                return true;
            }

            return false;
        }

        @Override
        public StateBuilder copy() {
            return new BranchCountStateBuilder(Observations.lastMeasuredMetricValue);
        }

        @Override
        public State build(PathCondition resultingPC) {
            return new BranchCountState(Observations.lastMeasuredMetricValue, resultingPC);
        }

    }

    private final double instrCount;

    private BranchCountState(double instrCount, PathCondition pc) {
        super(pc);
        this.instrCount = instrCount;
    }

    @Override
    public int compareTo(State o) {
        if (!(o instanceof BranchCountState)) {
            throw new IllegalStateException("Expected state of type " + BranchCountState.class.getName());
        }
        BranchCountState other = (BranchCountState) o;
        return this.instrCount < other.instrCount ? -1 : this.instrCount > other.instrCount ? 1 : 0;
    }

    public double getInstructionCount() {
        return this.instrCount;
    }

    @Override
    public double getWC() {
        return this.getInstructionCount();
    }
}
