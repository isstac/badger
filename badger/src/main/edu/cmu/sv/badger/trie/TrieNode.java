package edu.cmu.sv.badger.trie;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.sv.badger.util.BytecodeUtils;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.vm.Instruction;

/**
 * modified by Yannic Noller <nolleryc@gmail.com> - YN
 */
public class TrieNode implements Serializable {

    private static final long serialVersionUID = 7526472295634343143L;

    private int id;
    private Trie trie;
    private TrieNode parent;
    private Map<Integer, TrieNode> children; // maps the choice to the children nodes
    private int choice; // branch choice
    private int offset; // bytecode offset
    private String methodName;
    private int lineNumber;
    private int inputSize;

    
    private TrieNodeType type;
    private int instructionHashCode = -1;
    private int nextInstructionHashCode = -1;
    private int bytecode;
    private int maximumNumberOfChildren = -1;

    // TrieNode Annotations
    private boolean enabled;
    private boolean needsExploration;
    private int guidedChoice = -1;
    public void setGuidedChoice(int choice) {
        this.guidedChoice = choice;
    }

    /**
     * Returns the next choice regarding to the defined guidance path.
     * 
     * @return -1, if no guidance is given and this node needs exploration; positive/zero value for guided choice
     */
    public int getGuidedChoice() {
        return this.guidedChoice;
    }

    private boolean isCompleted = false;

    private Double metricValue = 0.0;

    private int depth = -1;

    public int getDepth() {
        return depth;
    }

    public int getId() {
        return id;
    }

    public TrieNode(Trie trie, int choice, int offset, String methodName, int lineNumber, TrieNode parent) {
        this.trie = trie;
        this.choice = choice;
        this.offset = offset;
        this.methodName = methodName;
        this.lineNumber = lineNumber;
        this.parent = parent;
        this.type = TrieNodeType.REGULAR_NODE; // default node type
        children = new HashMap<>();
        if (parent != null) {
            this.parent.addChild(this);
            this.depth = parent.depth + 1;
        } else {
            this.depth = 0;
        }
        this.id = trie.getNextId();
        this.inputSize = -1;
    }

    public TrieNode(Trie trie, int choice, int offset, String methodName, int lineNumber, TrieNode parent, Instruction instruction,
            PathCondition pathCondition, Double metricValue, int inputSize) {
        this(trie, choice, offset, methodName, lineNumber, parent);
        if (instruction != null) {
            this.instructionHashCode = instruction.hashCode();
            this.bytecode = instruction.getByteCode();
            if (this.parent != null) {
                if (this.parent.children.size() == 1) {
                    this.parent.maximumNumberOfChildren = BytecodeUtils.getNumberOfChoices(instruction);
                }
                if (this.parent.nextInstructionHashCode == -1) {
                    this.parent.nextInstructionHashCode = this.instructionHashCode;
                }
            }
        } else {
            this.bytecode = -1;
            this.parent.maximumNumberOfChildren = 0;
        }
        if (pathCondition == null) {
            this.type = TrieNodeType.UNSAT_NODE;
        }
        this.metricValue = metricValue;
        this.inputSize = inputSize; 
    }

    public TrieNode getParent() {
        return parent;
    }

    public void setParent(TrieNode parent) {
        this.parent = parent;
    }

    public List<TrieNode> getChildren() {
        return new ArrayList<>(children.values());
    }

    public void setChildren(Map<Integer, TrieNode> children) {
        this.children = children;
    }

    public void addChild(TrieNode child) {
        this.children.put(child.choice, child);
    }

    public int getChoice() {
        return choice;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public TrieNode getChild(int choice) {
        return children.get(choice);
    }

    public TrieNodeType getType() {
        return this.type;
    }

    public void setType(TrieNodeType type) {
        this.type = type;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
    public int getLineNumber() {
        return this.lineNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled() {
        this.enabled = true;
        this.trie.addEnabledNode(this);
    }

    public String toString() {
        return id + ":" + methodName + ":" + offset + ":" + choice + ":" + bytecode + ":" + maximumNumberOfChildren
                + ":" + metricValue;
    }

    public void compact() {
        for (TrieNode n : this.getChildren()) {
            if (!n.isEnabled()) {
                n.getChildren().clear();
            } else {
                n.compact();
            }
        }
    }

    public int getBytcode() {
        return bytecode;
    }

    /**
     * @return negative number means unknown
     */
    public int getMaximumNumberOfChildren() {
        return maximumNumberOfChildren;
    }

    public boolean needsExploration() {
        return needsExploration;
    }

    public void setExplorationNeeded(boolean needsExploration) {
        this.needsExploration = needsExploration;
    }

    public void resetAnnotation() {
        this.enabled = false;
        this.needsExploration = false;
        this.guidedChoice = -1;
    }

    public void setCompleted() {
        this.isCompleted = true;
    }
    

    public void resetComplete() {
        this.isCompleted = false;
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }

    public void updateMetricValue(double newMetricValue) {
        // update metric value
        this.metricValue = newMetricValue;

        // update position in trie priority queue by adding it to the queue (if
        // required)
        trie.updateNode(this);
    }

    public Double getMetricValue() {
        return this.metricValue;
    }

    public int getInstruction() {
        return this.instructionHashCode;
    }

    public int getNextInstruction() {
        return this.nextInstructionHashCode;
    }

    public boolean canExposeNewBranches() {

        // Check whether there is a general exploration potential for this node.
        if (!hasPotentialForExploration()) {
            return false;
        }

        // Check branch coverage for the instruction associated to this node.
        if (!this.getChildren().isEmpty()) {
            if (this.trie.getAnalyzer().getObservedChoices(nextInstructionHashCode).size() >= this.getMaximumNumberOfChildren()) {
                return false;
            }
        } else {
            return false;
        }

        // Otherwise.
        return true;

    }

    /**
     * Checks whether this node should be explored at some point, i.e. if there can exist children nodes.
     * 
     * @return true for yes, otherwise false.
     */
    public boolean hasPotentialForExploration() {

        // If node was already processed, then it is marked as completed and needs no further exploration.
        if (this.isCompleted) {
            return false;
        }

        // We assume complete runs, i.e. leaf nodes are the end of an execution.
        if (this.type.equals(TrieNodeType.LEAF_NODE)) {
            return false;
        }

        // Unsatisfiable nodes need no further exploration.
        if (this.type.equals(TrieNodeType.UNSAT_NODE)) {
            return false;
        }

        // The metric value of a node will be null, if this node was explored during the guided symbolic execution step,
        // but there was no input observed so far for this node. Then wait until we have seen an input for this node and
        // we were able to measure some metric value.
        if (this.getMetricValue() == null) {
            return false;
        }

        // This node may already have more or equal children nodes as its bytecode instruction is supposed to have
        // jumps.
        if (this.getMaximumNumberOfChildren() <= this.getChildren().size()) {
            return false;
        }

        // Otherwise.
        return true;

    }

    /**
     * Is only unique for a certain trie.
     */
    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TrieNode)) {
            return false;
        }
        TrieNode other = (TrieNode) obj;
        if (other.id != this.id) {
            return false;
        }
        return true;

    }
    
    public int getInputSize() {
        return inputSize;
    }

}