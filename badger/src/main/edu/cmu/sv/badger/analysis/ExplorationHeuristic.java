package edu.cmu.sv.badger.analysis;

import java.util.Comparator;

import edu.cmu.sv.badger.trie.TrieNode;

/**
 * Defines all exploration heuristics for Badger.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 * @param <T>
 *            TrieNode
 */
public abstract class ExplorationHeuristic implements Comparator<TrieNode> {

    public final String ID;

    private ExplorationHeuristic(String id) {
        this.ID = id;
    }

    public static final ExplorationHeuristic HIGHEST_COST_HIGHEST_NODE = new ExplorationHeuristic(
            "highest-cost-highest-node") {

        @Override
        public int compare(TrieNode o1, TrieNode o2) {
            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            } else {
                // 2. prioritize higher metric value
                int metricComp = (int) (o2.getMetricValue() - o1.getMetricValue());
                if (metricComp != 0) {
                    return metricComp;
                } else {
                    // 3. prioritize highest nodes in the tree.
                    return o1.getDepth() - o2.getDepth();
                }
            }
        }
    };

    public static final ExplorationHeuristic HIGHEST_COST_LOWEST_NODE = new ExplorationHeuristic(
            "highest-cost-lowest-node") {

        @Override
        public int compare(TrieNode o1, TrieNode o2) {
            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            } else {
                // 2. prioritize higher metric value
                int metricComp = (int) (o2.getMetricValue() - o1.getMetricValue());
                if (metricComp != 0) {
                    return metricComp;
                } else {
                    // 3. prioritize lowest nodes in the tree.
                    return o2.getDepth() - o1.getDepth();
                }
            }
        }

    };

    public static final ExplorationHeuristic LOWEST_COST_HIGHEST_NODE = new ExplorationHeuristic(
            "lowest-cost-highest-node") {

        @Override
        public int compare(TrieNode o1, TrieNode o2) {
            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            } else {
                // 2. prioritize lower metric value
                int metricComp = (int) (o2.getMetricValue() - o1.getMetricValue()) * (-1);
                if (metricComp != 0) {
                    return metricComp;
                } else {
                    // 3. prioritize highest nodes in the tree.
                    return o1.getDepth() - o2.getDepth();
                }
            }
        }
    };

    public static final ExplorationHeuristic LOWEST_COST_LOWEST_NODE = new ExplorationHeuristic(
            "lowest-cost-lowest-node") {

        @Override
        public int compare(TrieNode o1, TrieNode o2) {
            // 1. prioritize new branch coverage.
            boolean o1ExposeNewBranches = o1.canExposeNewBranches();
            boolean o2ExposeNewBranches = o2.canExposeNewBranches();
            if (o1ExposeNewBranches && !o2ExposeNewBranches) {
                return -1;
            } else if (o2ExposeNewBranches && !o1ExposeNewBranches) {
                return +1;
            } else {
                // 2. prioritize lower metric value
                int metricComp = (int) (o2.getMetricValue() - o1.getMetricValue()) * (-1);
                if (metricComp != 0) {
                    return metricComp;
                } else {
                    // 3. prioritize lowest nodes in the tree.
                    return o2.getDepth() - o1.getDepth();
                }
            }
        }
    };

}
