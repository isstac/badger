package edu.cmu.sv.badger.app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import edu.cmu.sv.badger.util.Statistics;

/**
 * Main class to start Badger execution.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public class BadgerRunner {

    public static void main(String[] args) {

        String configFilePath = args[0];
        BadgerInput input = BadgerInput.loadFromConfigFile(configFilePath);

        ensureAndCleanOutputDirectoriesExist(input);
        Statistics.initFiles(input);

        SymExe symExe = new SymExe(input);
        symExe.run();

    }

    private static void ensureAndCleanOutputDirectoriesExist(BadgerInput input) {

        File tmpDir = new File(input.tmpDir);
        try {
            FileUtils.forceMkdir(tmpDir);
            FileUtils.cleanDirectory(tmpDir);
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] Unable to create tmp directory: " + input.tmpDir, e);
        }

        File exportDir = new File(input.exportDir);
        try {
            FileUtils.forceMkdir(exportDir);
            FileUtils.cleanDirectory(exportDir);
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] Unable to create tmp directory: " + input.exportDir, e);
        }
    }

}
