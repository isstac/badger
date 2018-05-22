package edu.cmu.sv.badger.analysis;

import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.TrieNode;

/**
 * Analyzes the trie for branches that need further investigations because not
 * all possible choices were explored yet.
 * 
 * @author nolleryc
 *
 */
public class CoverageAnalyzer extends TrieAnalyzer {
    
    public final static String ID = "cov";

    @Override
    public TrieNode analyze(Trie trie) {
        traverse(trie.getRoot());
        return trie.getRoot(); // TODO fix this
    }

    private void traverse(TrieNode node) {

        if (node == null) {
            return;
        }

        // Part 1: If (number of possible choices > number of children) then enable it,
        // and mark it as "needs further exploration".
        if (node.getMaximumNumberOfChildren() > node.getChildren().size()) {
            node.setEnabled();
            node.setExplorationNeeded(true);
        }

        // Part 2: in any case, traverse children to search for more missing decisions.
        for (TrieNode child : node.getChildren()) {
            traverse(child);
        }

        // If this node is enabled (either in part 1 or implicit in part 2), then the
        // parent node of this node also need to be enabled. Note: this does not mean
        // that the parent node also gets marked as "needs further exploration".
        if (node.isEnabled()) {
            if (node.getParent() != null) {
                node.getParent().setEnabled();
            }
        }

    }

}
