package edu.cmu.sv.badger.analysis;

import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.TrieNode;

/**
 * Analyzes the trie for the most promising
 * 
 * @author nolleryc
 *
 */
public class WCAAnalyzer extends TrieAnalyzer {

    public final static String ID = "wca";
    
    public WCAAnalyzer() {
    }

    @Override
    public TrieNode analyze(Trie trie) {
        trie.resetAnnotation();

        // Part 2: Enable only the nodes within the ntopMost range.
        TrieNode node = trie.pollNodeWithHighestPriority();
        if (node == null) {
            return null;
        }
        node.setExplorationNeeded(true);
        enablePathToNode(node, -1);
        return node;
    }

    private void enablePathToNode(TrieNode node, int nextChoice) {
        if (node == null) {
            return;
        }

        node.setEnabled();

        if (nextChoice >= 0) {
            node.setGuidedChoice(nextChoice);
        }

        enablePathToNode(node.getParent(), node.getChoice());
    }

}
