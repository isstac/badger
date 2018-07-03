package edu.cmu.sv.badger.listener;

import edu.cmu.sv.badger.analysis.StateBuilder;
import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.Trie.CostStrategy;
import edu.cmu.sv.badger.trie.TrieNode;
import edu.cmu.sv.badger.trie.TrieNodeType;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.symbc.sequences.SequenceChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;

/**
 * This listener class builds a trie during dynamic symbolic execution.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */

public class ConcreteInput2TrieListener extends ListenerAdapter {
    Trie trie;
    TrieNode cur;

    static boolean DEBUG = false;

    StateBuilder metricBuilder;
    String currentInput;
    boolean useUserDefinedCost;

    private boolean observedBetterScore = false;
    private Double observedFinalCost = null;
    private boolean exposedNewBranch = false;

    public ConcreteInput2TrieListener(Config config, JPF jpf, Trie trie, StateBuilder metricBuilder,
            String currentInput, boolean useUserDefinedCost) {
        if (DEBUG) {
            System.out.println("Building the trie ...");
        }

        this.trie = trie;
        TrieNode root = trie.getRoot();
        if (root != null) {
            cur = root;
        }

        this.metricBuilder = metricBuilder;
        this.currentInput = currentInput;
        this.useUserDefinedCost = useUserDefinedCost;
    }

    public Trie getResultingTrie() {
        return this.trie;
    }

    public Double getObservedCostForLeafNode() {
        return this.observedFinalCost;
    }

    public boolean didObserveBetterScore() { // can be highscore or lowscore depends on cost target
        return this.observedBetterScore;
    }

    public boolean didExposeNewBranch() {
        return this.exposedNewBranch;
    }

    @Override
    public void searchConstraintHit(Search search) {
        if (DEBUG) {
            System.out.print("search limit");
        }
        if (cur.getType().equals(TrieNodeType.REGULAR_NODE)) {
            cur.setType(TrieNodeType.FRONTIER_NODE); // set frontier
        }
        if (DEBUG) {
            System.out.print(" " + search.getStateId());
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        if (DEBUG) {
            System.out.println(">>> stateAdvanced");
        }
        ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
        if (DEBUG) {
            System.out.println("cg: " + cg);
        }

        // thread choice instead of pc choice
        if (cg instanceof ThreadChoiceGenerator) {
            return;
        }
        if (cg instanceof SequenceChoiceGenerator) {
            return;
        }

        if (cg instanceof PCChoiceGenerator) {
            int offset = ((PCChoiceGenerator) cg).getOffset();
            if (offset == 0) {
                return;
            }

            if (trie.getRoot() == null) { // create the root node
                TrieNode root = new TrieNode(trie, -1, -1, null, -1, null);
                trie.setRoot(root);
                cur = root;
            }

            // create node, add it as cur's child, and update cur
            int choice = ((PCChoiceGenerator) cg).getNextChoice();
            String method = ((PCChoiceGenerator) cg).getMethodName();
            Instruction currentInstruction = ((PCChoiceGenerator) cg).getInsn();
            int lineNumber = (currentInstruction != null) ? currentInstruction.getLineNumber() : -1;
            PathCondition pc = ((PCChoiceGenerator) cg).getCurrentPC();

            // check if current node already contains this choice
            TrieNode child = cur.getChild(choice);
            if (child != null) {
                cur = child;

                /*
                 * Check if metric value of this is the initial null value, then update if with the current value form
                 * the metric builder. The null value is used to initialize nodes during the symbolic exploration phase
                 * because we don't use an metric listener there. Normally this null value happens somewhere in the
                 * middle of the tree and then the null value is overridden in the backpropagation. But it also might
                 * happen that the new explored node is a leaf node in the tree. So it is better to override this value
                 * right here, and it might be overridden again during backpropagation.
                 */
                if (cur.getMetricValue() == null) {
                    if (!useUserDefinedCost && metricBuilder != null) {
                        cur.updateMetricValue(metricBuilder.build(pc).getWC());
                    } else if (useUserDefinedCost) {
                        cur.updateMetricValue(Observations.lastObservedCost);
                    }
                }

            } else {
                // create node, add it as cur's child, and update cur

                double cost;
                if (!useUserDefinedCost && metricBuilder != null) {
                    cost = metricBuilder.build(pc).getWC();
                } else if (useUserDefinedCost) {
                    cost = Observations.lastObservedCost;
                } else {
                    cost = 0.0;
                }

                TrieNode n = new TrieNode(trie, choice, offset, method, lineNumber, cur, currentInstruction, pc, cost,
                        Observations.lastObservedInputSize);
                if (trie.addObservedChoice(currentInstruction, choice)) {
                    exposedNewBranch = true;
                }

                cur = n;
            }

        }

    }

    @Override
    public void stateBacktracked(Search search) {
        if (DEBUG) {
            System.out.println(">>> stateBacktracked");
        }

        ChoiceGenerator<?> cg = search.getVM().getChoiceGenerator();
        if (DEBUG) {
            System.out.println("cg: " + cg);
        }

        if (cg != null && cg instanceof PCChoiceGenerator) {
            int offset = ((PCChoiceGenerator) cg).getOffset();
            if (offset == 0) {
                return;
            }

            if (cur == null) {
                if (DEBUG) {
                    System.err.println("backtracked from root node; no action needed for now");
                }
                return;
            }

            /* Save the cost for the lowest node and mark it if it is a new best score. */
            if (cur.getChildren().isEmpty()) {
                cur.setType(TrieNodeType.LEAF_NODE);
                observedFinalCost = cur.getMetricValue();
                if (observedFinalCost != null && (trie.getCostStrategy().equals(CostStrategy.MAXIMIZE)
                        ? observedFinalCost > trie.currentBestCostValue
                        : observedFinalCost < trie.currentBestCostValue)) {
                    trie.currentBestCostValue = observedFinalCost;
                    observedBetterScore = true;
                }

            }

            /* Backpropagate metric value */
            if (cur.getParent() != null) {
                int numberOfChildren = cur.getParent().getChildren().size();
                double newMetricValueForParent;
                if (numberOfChildren == 1) {
                    // this is the first children, then just reuse this number
                    newMetricValueForParent = cur.getMetricValue();
                } else {
                    // if there are other children, then update the average
                    // Double oldMetricValueForParent = cur.getParent().getMetricValue();
                    // newMetricValueForParent = oldMetricValueForParent
                    // + (cur.getMetricValue() - oldMetricValueForParent) / numberOfChildren;
                    double sum = 0.0;
                    int numberOfSATChildren = 0;
                    for (TrieNode child : cur.getParent().getChildren()) {
                        
                        /* Skip unsat nodes because they do not have a metric value. */
                        if (child.getType().equals(TrieNodeType.UNSAT_NODE)) {
                            continue;
                        }
                        
                        if (child.getMetricValue() == null) {
                            System.out.println();
                        }

                        sum += child.getMetricValue();
                        numberOfSATChildren++;
                    }
                    newMetricValueForParent = sum / numberOfSATChildren;
                }
                cur.getParent().updateMetricValue(newMetricValueForParent);
            }

            cur = cur.getParent();
            if (DEBUG) {
                if (cur.getParent() == null) {
                    System.out.println("backtracked to root.");
                }
            }
        }
    }
}
