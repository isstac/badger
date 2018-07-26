package com.stac;

import com.stac.learning.*;

import java.nio.file.*;
import java.util.*;

public class Main
{
    private static Path ccdir;
    private static ClusterController clusterController;
    
    static void printHelp() {
        System.out.append("Usage: \n").append("    Arguments: \n").append("       train <filename> <type>\n").append("          Adds this image to the training set.\n").append("       cluster <filename>\n").append("          Tests this file against the training set.\n").append("\n").append("    See ").append(Main.ccdir.toString()).append("/config.cfg for algorithm order configuration.\n");
    }
    
    public static void main(final String[] args) {

    	
        boolean status = true;
        switch (parseArgs(args)) {
            case TRAIN: {
                if (!setTag(args[1], args[2])) {
                    status = false;
                    break;
                }
                break;
            }
            case CLUSTER: {
                if (!clusterAgainstTrainingSet(args[1])) {
                    status = false;
                    break;
                }
                break;
            }
            default: {
                printHelp();
                status = false;
                break;
            }
        }
        System.exit(status ? 0 : -1);
    }
    
    private static boolean clusterAgainstTrainingSet(final String filename) {
        final String[] nearestTypes = Main.clusterController.cluster(Paths.get(filename, new String[0]).toFile());
        if (nearestTypes == null) {
            System.err.append("Failed to classify ").append(filename).println();
            return false;
        }
        final HashMap<String, Integer> inCluster = new HashMap<String, Integer>(nearestTypes.length);
        for (final String type : nearestTypes) {
            if (inCluster.containsKey(type)) {
                inCluster.put(type, inCluster.get(type) + 1);
            }
            inCluster.put(type, 1);
        }
        final LinkedList<Map.Entry<String, Integer>> types = new LinkedList<Map.Entry<String, Integer>>(inCluster.entrySet());
        Collections.sort(types, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(final Map.Entry<String, Integer> o1, final Map.Entry<String, Integer> o2) {
                return Objects.requireNonNull(o2.getValue()) - Objects.requireNonNull(o1.getValue());
            }
        });
        System.out.append("The image ").append(filename).append(" classifies as: ").append(types.getFirst().getKey()).println();
        return true;
    }
    
    private static boolean setTag(final String filename, final String tag) {
        return Main.clusterController.setTag(filename, tag);
    }
    
    private static Command parseArgs(final String[] args) {
        if (args.length == 2 || args.length == 3) {
            if (args[0].equalsIgnoreCase("train") && args.length == 3) {
                return Command.TRAIN;
            }
            if (args[0].equalsIgnoreCase("cluster") && args.length == 2) {
                return Command.CLUSTER;
            }
        }
        return Command.NONE;
    }
    
    static {
        Main.ccdir = Paths.get(System.getProperty("user.home"), ".imageClustering");
        Main.clusterController = new ClusterController(Main.ccdir);
    }
    
    private enum Command
    {
        TRAIN, 
        CLUSTER, 
        NONE;
    }
}
