package edu.cmu.sv.badger.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.jpf.symbc.Observations;
import gov.nasa.jpf.symbc.numeric.PathCondition;

public class DynamicCharArrayIOUtils extends IOUtils {

    public static final String ID = "dynamic-char-byte-array";

    public int numberOfVars;
    public int maxStringLength;

    public DynamicCharArrayIOUtils(int N, int M) {
        this.numberOfVars = N;
        this.maxStringLength = M;
    }

    @Override
    public Map<String, String> processInput(List<String> inputFiles) {
        // here we don't need to process anything
        Map<String, String> res = new HashMap<>();
        for (String inputfile : inputFiles) {
            res.put(inputfile, inputfile);
        }
        return res;
    }

    @Override
    public void generateInputFiles(PathCondition pc, Map<String, Object> solution, String outputFile) {

        int m = Math.min(Observations.lastObservedInputSize, maxStringLength);

        byte[] data = new byte[numberOfVars * m * Character.BYTES];
        int dataCounter = 0;

        for (int i = 0; i < numberOfVars; i++) {
            for (int j = 0; j < m; j++) {

                // Get char value from solution model.
                Object value = solution.get("sym_" + i + "_" + j);
                char charValue;
                if (value == null) {
                    charValue = 0;
                } else {
                    int t = Math.toIntExact((long) value);
                    charValue = (char) t;
                }

                // Transform char in byte[].
                ByteBuffer bb = ByteBuffer.allocate(Character.BYTES);
                bb.putChar(charValue);
                byte[] byteArray = bb.array();

                // Save byte values in data[].
                data[dataCounter++] = byteArray[0];
                data[dataCounter++] = byteArray[1];
            }
        }

        try {
            Files.write(Paths.get(outputFile), data);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
