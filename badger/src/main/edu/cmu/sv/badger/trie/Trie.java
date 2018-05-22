package edu.cmu.sv.badger.trie;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import edu.cmu.sv.badger.analysis.ExplorationHeuristic;
import gov.nasa.jpf.vm.Instruction;

/**
 * Trie data structure with the following info stored: methodName, bytecode offset, and choice.
 * 
 * @author Guowei Yang (guoweiyang@utexas.edu)
 * 
 * Modified for WCA by Yannic Noller <nolleryc@gmail.com> - YN
 */

public class Trie implements Serializable {

    private static final long serialVersionUID = 7526472295622776147L;

    public enum CostStrategy {
        MAXIMIZE, MINIMIZE;
    };

    private CostStrategy currentStrategy;

    public CostStrategy getCostStrategy() {
        return this.currentStrategy;
    }

    private AtomicInteger idGenerator = new AtomicInteger(-1);

    public int getNextId() {
        return idGenerator.incrementAndGet();
    }

    public double currentBestCostValue;

    private TrieNode root;

    /* Stores all TrieNodes of the Trie that are available for exploration in a prioritized order. */
    private PriorityQueue<TrieNode> nTopScoreNodes;

    /*
     * Stores all TrieNodes that correspond to the same Instruction in order to update them faster if there is change in
     * the choices. Instruction is stored in hashcode representation. The complete Instruction object leads to memory
     * problems.
     */
    private Map<Integer, Set<TrieNode>> instruction2NodeMapping;

    /* Stores the so far made choices for each observed Instruction. Used to determine branch new branches. */
    private Map<Integer, Set<Integer>> observedChoices;

    private Set<TrieNode> enabledNodes;

    /**
     * Adds a new observed choice, and updates all necessary data structures.
     * 
     * @param instr
     *            - Instruction object
     * @param choice
     *            - choice integer value
     * @return true if choice was new, false otherwise.
     */
    public boolean addObservedChoice(Instruction instr, int choice) {
        boolean addedNewChoice = false;
        if (instr != null) {
            int instrHashCode = instr.hashCode();
            Set<Integer> choices = observedChoices.get(instrHashCode);
            if (choices == null) {
                choices = new HashSet<>();
                observedChoices.put(instrHashCode, choices);
            }
            addedNewChoice = choices.add(choice);

            /* If new choice then update prio queue. */
            if (addedNewChoice) {
                Set<TrieNode> correspondingNodesForInstruction = instruction2NodeMapping.get(instrHashCode);
                /* Might be null in the beginning, then there is no node that needs any update. */
                if (correspondingNodesForInstruction != null) {
                    for (TrieNode node : correspondingNodesForInstruction) {
                        updateNodeInPriorityQueue(node);
                    }
                }
            }
        }
        return addedNewChoice;
    }

    /**
     * Returns all choices observed so far for this instruction, i.e. returns which branches from this condition already
     * occurred during execution.
     * 
     * @param instr
     *            - int hashcode
     * @return Set of int values.
     */
    public Set<Integer> getObservedChoices(int instructionHashCode) {
        Set<Integer> choices = observedChoices.get(instructionHashCode);
        if (choices == null) {
            return new HashSet<>();
        } else {
            return choices;
        }
    }

    public Trie(Comparator<TrieNode> nodeComparator) {
        this.nTopScoreNodes = new PriorityQueue<>(nodeComparator);
        this.observedChoices = new HashMap<>();
        this.instruction2NodeMapping = new HashMap<>();
        this.enabledNodes = new HashSet<>();

        if (nodeComparator.equals(ExplorationHeuristic.HIGHEST_COST_HIGHEST_NODE) || nodeComparator.equals(ExplorationHeuristic.HIGHEST_COST_LOWEST_NODE)) {
            this.currentStrategy = CostStrategy.MAXIMIZE;
            currentBestCostValue = Double.NEGATIVE_INFINITY;
        } else if (nodeComparator.equals(ExplorationHeuristic.LOWEST_COST_HIGHEST_NODE)
                || nodeComparator.equals(ExplorationHeuristic.LOWEST_COST_LOWEST_NODE)) {
            this.currentStrategy = CostStrategy.MINIMIZE;
            currentBestCostValue = Double.POSITIVE_INFINITY;
        } else {
            throw new RuntimeException("Unknown node priorisation heuristic: " + nodeComparator.toString());
        }

    }

    public TrieNode getRoot() {
        return root;
    }

    public void setRoot(TrieNode root) {
        this.root = root;
    }

    public void addEnabledNode(TrieNode node) {
        this.enabledNodes.add(node);
    }

    /**
     * Adds, updates or removes node in priority queue.
     * 
     * @param node
     * @return true, if added or updated.
     */
    public boolean updateNodeInPriorityQueue(TrieNode node) {

        // To update a node we have to remove and re-add it.
        this.nTopScoreNodes.remove(node);

        // Check whether this node should be added to the queue or not.
        if (!node.hasPotentialForExploration()) {
            return false;
        }

        // Update Instruction to TrieNode mapping.
        Set<TrieNode> nodes = instruction2NodeMapping.get(node.getNextInstruction());
        if (nodes == null) {
            nodes = new HashSet<>();
            instruction2NodeMapping.put(node.getNextInstruction(), nodes);
        }
        nodes.add(node);

        /*
         * Add node to priority queue if metric value is known AND if this node has potential for exploration, i.e. if
         * there are potential children for this node.
         */
        return this.nTopScoreNodes.add(node);

    }

    /**
     * Returns the current most promising node in the trie that should be chosen for the next exploration step. Priority
     * is based on the selected metric.
     * 
     * @return TrieNode
     */
    public TrieNode pollNodeWithHighestPriority() {
        if (this.nTopScoreNodes.isEmpty()) {
            return null;
        }
        TrieNode nodeWithHighestPriority = this.nTopScoreNodes.poll();

        // Mark node as completed, i.e. never added to the priority queue again.
        nodeWithHighestPriority.setCompleted();

        // Remove this node from the instruction2Node mapping, since this node does not need longer any update.
        instruction2NodeMapping.get(nodeWithHighestPriority.getNextInstruction()).remove(nodeWithHighestPriority);

        return nodeWithHighestPriority;
    }

    /**
     * Checks whether there is any node left that is available for exploration.
     * 
     * @return true for yes, otherwise false
     */
    public boolean isPriorityQueueEmpty() {
        return this.nTopScoreNodes.isEmpty();
    }

    /**
     * Compact the trie based on enabled nodes, i.e. removes all disabled nodes.
     */
    public void compact() {
        root.compact();
    }

    public void resetAnnotation() {
        for (TrieNode node : this.enabledNodes) {
            node.resetAnnotation();
        }
        enabledNodes = new HashSet<>();
    }

    public static boolean storeTrie(Trie trie, String filePath) {
        try {
            FileOutputStream fout = new FileOutputStream(filePath);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(trie);
            oos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void storeTrieAsDot(Trie trie, String filePath) {
        TriePrintToDot tp = new TriePrintToDot(trie);
        tp.print(filePath);
    }

    public static Trie loadTrie(String filePath) {
        Trie trie = null;
        try {
            FileInputStream fin = new FileInputStream(filePath);
            ObjectInputStream ois = new ObjectInputStream(fin);
            trie = (Trie) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e1) {
            return null;
        } catch (Exception e) {
            System.err.println("something wrong with trie de-serializing");
            e.printStackTrace();
        }
        return trie;
    }

    public String getStatistics() {
        long instrCount = 0;
        for (Entry<Integer, Set<TrieNode>> entry : instruction2NodeMapping.entrySet()) {
            instrCount += entry.getValue().size();
        }
        return idGenerator.get() + "," + nTopScoreNodes.size() + "," + instrCount;
    }

    public int getNumberOfPaths() {
        return countnumberOfPaths(root);

    }

    private int countnumberOfPaths(TrieNode node) {
        if (node.getType().equals(TrieNodeType.LEAF_NODE)) {
            return 1;
        } else {
            int numberOfPaths = 0;
            for (TrieNode child : node.getChildren()) {
                numberOfPaths += countnumberOfPaths(child);
            }
            return numberOfPaths;
        }
    }

}
