package edu.cmu.sv.badger.analysis;

import edu.cmu.sv.badger.trie.Trie;
import edu.cmu.sv.badger.trie.TrieNode;

/**
 * Abstract class to provide a common interface for the analysis of trie data structures.
 * 
 * @author nolleryc
 *
 */
public abstract class TrieAnalyzer {

    /**
     * Returns most promising node for further exploration, and enables the path to this node in the given trie.
     * 
     * @param trie
     * @return TrieNode
     */
    public abstract TrieNode analyze(Trie trie);

}
