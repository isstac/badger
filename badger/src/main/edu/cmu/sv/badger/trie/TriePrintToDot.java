package edu.cmu.sv.badger.trie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Help class to print a trie to a dot graph
 * 
 * @author Guowei Yang (guoweiyang@utexas.edu)
 * 
 *         extended for new elements of TrieNode by Yannic Noller <nolleryc@gmail.com> - YN
 * 
 */

public class TriePrintToDot {
    Trie trie;

    private static boolean printInputSizeInfo = false;

    public TriePrintToDot() {

    }

    public TriePrintToDot(Trie trie) {
        this.trie = trie;
    }

    public void loadTrie(String trieName) {
        // de-serialize the stored trie from the disk
        try {
            FileInputStream fin = new FileInputStream(trieName);
            ObjectInputStream ois = new ObjectInputStream(fin);
            trie = (Trie) ois.readObject();
            ois.close();
        } catch (Exception e) {
            System.err.println("something wrong with trie de-serializing");
            e.printStackTrace();
        }
    }

    public void print(String fileName, Integer maxDepth) {
        Writer output = null;
        File file = new File(fileName);
        try {
            file.createNewFile();
            output = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            System.err.println("error while creating the file to write");
            e.printStackTrace();
        }
        try {
            output.write("digraph \"\" { \n");
            if (null != trie.getRoot()) {
                printTrieNodesAndEdges(trie.getRoot(), output, maxDepth);
            }
            output.write("}");
        } catch (IOException e) {
            System.err.println("Error while writing to the XML file");
            e.printStackTrace();
        }

        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String generateNodeStringRepresentation(TrieNode node) {
        String stringRepresentation = "";
        if (node.needsExploration()) {
            if (node.getOffset() == -1) {// root node
                stringRepresentation = (node.hashCode()
                        + "[ color=\"lightblue\" style=\"filled\" fillcolor=\"green\" label=\"id=" + node.getId()
                        + ", Root, #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            } else {
                stringRepresentation = (node.hashCode() + "[ color=\"green\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", " + node.getMethodName() + ":" + node.getLineNumber() + ", \n offset="
                        + node.getOffset() + ", choice=" + node.getChoice() + ", \nbc=" + node.getBytcode()
                        + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            }
        } else if (node.isEnabled()) {
            if (node.getOffset() == -1) {// root node
                stringRepresentation = (node.hashCode() + "[ color=\"lightblue\" style=\"filled\" label=\"id="
                        + node.getId() + ", Root, #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            } else {
                stringRepresentation = (node.hashCode() + "[ color=\"red\" label=\"" + "id=" + node.getId() + ", "
                        + node.getMethodName() + ":" + node.getLineNumber() + ", \n offset=" + node.getOffset()
                        + ", choice=" + node.getChoice() + ", \nbc=" + node.getBytcode() + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            }
        } else {
            if (node.getOffset() == -1) {// root node
                stringRepresentation = (node.hashCode() + "[ color=\"lightblue\" style=\"filled\" label=\"id="
                        + node.getId() + ", Root, #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            } else if (node.getType().equals(TrieNodeType.UNSAT_NODE)) {
                stringRepresentation = (node.hashCode() + "[ color=\"yellow\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", " + node.getMethodName() + ":" + node.getLineNumber() + ", \n offset="
                        + node.getOffset() + ", choice=" + node.getChoice() + ", \nbc=" + node.getBytcode()
                        + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            } else if (node.getType().equals(TrieNodeType.FRONTIER_NODE)) {
                stringRepresentation = (node.hashCode() + "[ color=\"pink\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", " + node.getMethodName() + ":" + node.getLineNumber() + ", \n offset="
                        + node.getOffset() + ", choice=" + node.getChoice() + ", \nbc=" + node.getBytcode()
                        + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            } else if (node.getType().equals(TrieNodeType.LEAF_NODE)) {
                stringRepresentation = (node.hashCode() + "[ color=\"gray\" style=\"filled\" label=\"" + "id="
                        + node.getId() + ", " + node.getMethodName() + ":" + node.getLineNumber() + ", \n offset="
                        + node.getOffset() + ", choice=" + node.getChoice() + ", \nbc=" + node.getBytcode()
                        + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            } else {
                stringRepresentation = (node.hashCode() + "[ label=\"" + "id=" + node.getId() + ", "
                        + node.getMethodName() + ":" + node.getLineNumber() + ", \n offset=" + node.getOffset()
                        + ", choice=" + node.getChoice() + ", \nbc=" + node.getBytcode() + ", #choices="
                        + (node.getMaximumNumberOfChildren() == -1 ? "?" : node.getMaximumNumberOfChildren())
                        + ", \n score=" + (node.getMetricValue() == null ? "?" : node.getMetricValue())
                        + ", \n newBranches=" + node.canExposeNewBranches()
                        + (printInputSizeInfo ? ", \n inputSize=" + node.getInputSize() + "\"];\n" : "\"];\n"));
            }
        }
        return stringRepresentation;
    }

    public void printTrieNodesAndEdges(TrieNode node, Writer output, Integer maxDepth) throws IOException {
        List<TrieNode> nodesToPrint = new ArrayList<>();
        nodesToPrint.add(node);
        while (!nodesToPrint.isEmpty()) {
            TrieNode currentNode = nodesToPrint.remove(0);
            
            /* Print current node. */
            output.write(generateNodeStringRepresentation(currentNode));
            
            /* Print all edges from this node to its children. */
            List<TrieNode> children = currentNode.getChildren();
            for (TrieNode child : children) {
                if (child.isEnabled()) {
                    output.write(currentNode.hashCode() + "->" + child.hashCode() + "[ color=\"red\"];\n");
                } else {
                    output.write(currentNode.hashCode() + "->" + child.hashCode() + ";\n");
                }
            }
            
            /* Add all children the be processed. */
            if (maxDepth != null && currentNode.getDepth() < maxDepth) {
                nodesToPrint.addAll(children);
            }
        }
    }

}
