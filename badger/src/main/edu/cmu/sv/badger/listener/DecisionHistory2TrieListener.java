package edu.cmu.sv.badger.listener;

import java.util.List;
import java.util.Stack;

import org.objectweb.asm.Opcodes;

import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.TrieNode;
import edu.cmu.sv.badger.trie.TrieNodeType;
//import edu.cmu.sv.kelinci.Decision;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.bytecode.LOOKUPSWITCH;
import gov.nasa.jpf.symbc.bytecode.TABLESWITCH;
import gov.nasa.jpf.symbc.numeric.Expression;
import gov.nasa.jpf.symbc.numeric.IntegerExpression;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.sequences.SequenceChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * This listener class builds a trie during symbolic execution guided by a given
 * decision history.
 * 
 * @author nolleryc
 * 
 */

public class DecisionHistory2TrieListener extends ListenerAdapter {
//	Trie trie;
//	TrieNode cur;
//	Stack<Decision> decisionHistory;
//
//	static boolean DEBUG = false;
//
//	public DecisionHistory2TrieListener(Config config, JPF jpf, Trie trie, List<Decision> listOfDecisions) {
//		if (DEBUG) {
//			System.out.println("Building the trie ...");
//		}
//
//		this.trie = trie;
//		TrieNode root = trie.getRoot();
//		if (root != null) {
//			cur = root;
//		}
//
//		Stack<Decision> decisionHistory = new Stack<>();
//		for (int i = listOfDecisions.size() - 1; i >= 0; i--) {
//			decisionHistory.push(listOfDecisions.get(i));
//		}
//		this.decisionHistory = decisionHistory;
//	}
//
//	public Trie getResultingTrie() {
//		return this.trie;
//	}
//
//	@Override
//	public void executeInstruction(VM vm, ThreadInfo currentThread, Instruction instructionToExecute) {
//		String methodName = instructionToExecute.getMethodInfo().getName();
//		int currentBytecodeInstruction = instructionToExecute.getByteCode();
//		if (!decisionHistory.isEmpty()) {
//			Decision nextDecision = decisionHistory.peek();
//			if (nextDecision.getMethodName().equals(methodName)
//					&& nextDecision.getByteCode() == currentBytecodeInstruction) {
//				StackFrame sf = currentThread.getModifiableTopFrame();
//				switch (currentBytecodeInstruction) {
//				case Opcodes.IFEQ:
//				case Opcodes.IFNE:
//				case Opcodes.IFLT:
//				case Opcodes.IFGE:
//				case Opcodes.IFGT:
//				case Opcodes.IFLE:
//				case Opcodes.TABLESWITCH:
//				case Opcodes.LOOKUPSWITCH:
//				case Opcodes.IFNULL:
//				case Opcodes.IFNONNULL:
//					Expression sym_v = (Expression) sf.getOperandAttr();
//					if (sym_v == null) {
//						// the condition is concrete and in our case (assuming all input parameters are
//						// symbolic) this means the decision is always the same, so ignore the decision
//						// history here and skip it
//						decisionHistory.pop();
//					}
//					break;
//
//				case Opcodes.IF_ICMPEQ:
//				case Opcodes.IF_ICMPNE:
//				case Opcodes.IF_ICMPLT:
//				case Opcodes.IF_ICMPGE:
//				case Opcodes.IF_ICMPGT:
//				case Opcodes.IF_ICMPLE:
//					IntegerExpression sym_v1 = (IntegerExpression) sf.getOperandAttr(1);
//					IntegerExpression sym_v2 = (IntegerExpression) sf.getOperandAttr(0);
//					if ((sym_v1 == null) && (sym_v2 == null)) {
//						// both conditions are concrete and in our case (assuming all input parameters
//						// are symbolic) this means the decision is always the same, so ignore the
//						// decision history here and skip it
//						decisionHistory.pop();
//					}
//					break;
//
//				case Opcodes.IF_ACMPEQ:
//				case Opcodes.IF_ACMPNE:
//					// currently not implemented in SPF
//					decisionHistory.pop();
//					break;
//
//				default:
//					// this should only happen when the implementation is incomplete
//					new RuntimeException("Bytecode instruction not implemented: " + currentBytecodeInstruction);
//				}
//			}
//		}
//	}
//
//	@Override
//	public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
//		if (currentCG instanceof PCChoiceGenerator) {
//			if (currentCG.getTotalNumberOfChoices() > 1) {
//				Instruction insn = currentCG.getInsn();
//				int currentBytecodeInstruction = insn.getByteCode();
//				if (!decisionHistory.isEmpty()) {
//					Decision nextDecision = decisionHistory.peek();
//					if (nextDecision.getByteCode() == currentBytecodeInstruction) {
//						int decisionValue = nextDecision.getDecisionValue();
//						switch (currentBytecodeInstruction) {
//
//						case Opcodes.TABLESWITCH:
//							int length = ((TABLESWITCH) insn).getTargets().length;
//							if (decisionValue >= length || decisionValue < 0) {
//								decisionValue = length;
//							}
//							break;
//
//						case Opcodes.LOOKUPSWITCH:
//							int[] matches = ((LOOKUPSWITCH) insn).getMatches();
//							int foundIndex = matches.length;
//							for (int i = 0; i < matches.length - 1; i++) {
//								if (decisionValue == matches[i]) {
//									foundIndex = i;
//									break;
//								}
//							}
//							decisionValue = foundIndex;
//							break;
//
//						default:
//							// in the default case the decisionValue can be used to select the necessary
//							// choice
//						}
//
//						currentCG.select(decisionValue);
//						decisionHistory.pop();
//					}
//				}
//			}
//		}
//	}
//
//	@Override
//	public void searchFinished(Search search) {
//		if (DEBUG) {
//			System.out.println(">>> searchFinished");
//		}
//	}
//
//	@Override
//	public void searchConstraintHit(Search search) {
//		if (DEBUG) {
//			System.out.print("search limit");
//		}
//		if (cur.getType().equals(TrieNodeType.REGULAR_NODE)) {
//			cur.setType(TrieNodeType.FRONTIER_NODE); // set frontier
//		}
//		if (DEBUG) {
//			System.out.print(" " + search.getStateId());
//		}
//	}
//
//	@Override
//	public void stateAdvanced(Search search) {
//		if (DEBUG) {
//			System.out.println(">>> stateAdvanced");
//		}
//		ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
//		if (DEBUG) {
//			System.out.println("cg: " + cg);
//		}
//
//		// thread choice instead of pc choice
//		if (cg instanceof ThreadChoiceGenerator) {
//			return;
//		}
//		if (cg instanceof SequenceChoiceGenerator) {
//			return;
//		}
//
//		if (cg instanceof PCChoiceGenerator) {
//			int offset = ((PCChoiceGenerator) cg).getOffset();
//			if (offset == 0) {
//				return;
//			}
//		}
//
//		if (trie.getRoot() == null) { // create the root node
//			TrieNode root = new TrieNode(-1, -1, null, null);
//			trie.setRoot(root);
//			cur = root;
//		}
//
//		// create node, add it as cur's child, and update cur
//		int choice = ((PCChoiceGenerator) cg).getNextChoice();
//		int offset = ((PCChoiceGenerator) cg).getOffset();
//		String method = ((PCChoiceGenerator) cg).getMethodName();
//		Instruction currentInstruction = ((PCChoiceGenerator) cg).getInsn();
//
//		// check if current node already contains this choice
//		TrieNode child = cur.getChild(choice);
//		if (child != null) {
//			cur = child;
//		} else {
//			// create node, add it as cur's child, and update cur
//			TrieNode n = new TrieNode(choice, offset, method, cur, currentInstruction);
//			cur = n;
//		}
//
//		PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();
//		if (pc == null) {
//			// unsatisfiable constraint
//			cur.setType(TrieNodeType.UNSAT_NODE);
//			if (DEBUG) {
//				System.err.println("* unsatisfiable path");
//			}
//		}
//
//		if (DEBUG) {
//			System.out.println("**** choice: " + choice);
//			System.out.println("**** offset: " + offset);
//			System.out.println("**** method: " + method);
//		}
//
//	}
//
//	@Override
//	public void stateBacktracked(Search search) {
//		if (DEBUG) {
//			System.out.println(">>> stateBacktracked");
//		}
//
//		ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
//		if (DEBUG) {
//			System.out.println("cg: " + cg);
//		}
//
//		if (cg != null && cg instanceof PCChoiceGenerator) {
//			int offset = ((PCChoiceGenerator) cg).getOffset();
//			if (offset == 0) {
//				return;
//			}
//
//			if (cur == null) {
//				if (DEBUG) {
//					System.err.println("backtracked from root node; no action needed for now");
//				}
//				return;
//			}
//			cur = cur.getParent();
//			if (DEBUG) {
//				if (cur.getParent() == null) {
//					System.out.println("backtracked to root.");
//				}
//			}
//		}
//	}
}
