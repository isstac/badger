package edu.cmu.sv.badger.io;

import java.util.List;
import java.util.Map;

import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 */
public abstract class IOUtils {

    /**
     * Preprocesses the input if necessary.
     * 
     * @param inputFiles
     * @return key= original, value= processed
     */
    public abstract Map<String, String> processInput(List<String> inputFiles);

    public abstract void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile);

}
