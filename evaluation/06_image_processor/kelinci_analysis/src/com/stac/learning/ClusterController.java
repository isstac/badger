package com.stac.learning;

import java.nio.file.*;
import com.stac.image.*;
import java.util.*;
import java.io.*;

public class ClusterController
{
    private final String trainingSetFilename = "trainingSet.csv";
    private final String configFilename = "config.cfg";
    protected VectorMap trainingMap;
    public final Path clusteringDir;
    public final Path trainingImages;
    public final Path trainingSet;
    public final Properties configuration;
    
    public ClusterController() {
    	this.trainingMap = new VectorMap();
    	trainingImages = null;
    	clusteringDir = null;
    	trainingSet = null;
        this.configuration = new Properties();
    }
    
    public ClusterController(final Path clusteringDir) {
        this.trainingMap = new VectorMap();
        this.trainingImages = Paths.get("/","home","rodykers","cmu","Repositories","engagement","engagement_1","challenge_problems","examples");//Paths.get("/", "var", "lib", "trainer", "images");
        this.clusteringDir = clusteringDir;
        if (!clusteringDir.toFile().exists() || !clusteringDir.toFile().isDirectory()) {
            if (clusteringDir.toFile().exists()) {
                clusteringDir.toFile().delete();
            }
            clusteringDir.toFile().mkdirs();
        }
        this.trainingSet = Paths.get(clusteringDir.toString(), "trainingSet.csv");
        if (!this.trainingSet.toFile().exists()) {
            try {
                this.createNewTrainingSetFile();
            }
            catch (IOException e) {
                throw new RuntimeException("Could not create clustering training set csv file.");
            }
        }
        final Path configurationPath = Paths.get(clusteringDir.toString(), "config.cfg");
        this.configuration = new Properties();
        try {
            if (!configurationPath.toFile().exists()) {
                this.createNewConfiguration(configurationPath);
            }
            else {
                try (final InputStream configurationInputStream = new FileInputStream(configurationPath.toFile())) {
                    this.configuration.loadFromXML(configurationInputStream);
                }
            }
        }
        catch (IOException e2) {
            throw new RuntimeException("Could not create configuration file.");
        }
        if (!this.trainingImages.toFile().exists()) {
            this.trainingImages.toFile().mkdirs();
        }
    }
    
    private void createNewConfiguration(final Path configurationPath) throws IOException {
        configurationPath.toFile().createNewFile();
        this.configuration.setProperty("Algorithms", "WhiteDetector, BlackDetector, BlueDetector, RedDetector, GreenDetector, Intensify, EdgingDetector");
        try (final OutputStream os = new FileOutputStream(configurationPath.toFile())) {
            this.configuration.storeToXML(os, "Default algorithm order");
        }
    }
    
    protected boolean readTrainingSet() {
        try (final BufferedReader reader = new BufferedReader(new FileReader(this.trainingSet.toFile()))) {
            int lineNo = 1;
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                final String[] split = line.split(", ");
                if (split.length == 2) {
                    try {
                        final File imageFromClusteringDir = this.getImageFromClusteringDir(split[0]);
                        System.out.println("Loading image '" + imageFromClusteringDir);
                        final Vector attributeVector = ImageProcessing.getAttributeVector(imageFromClusteringDir, this.configuration.getProperty("Algorithms"));
                        this.trainingMap.put(attributeVector, split[1]);
                    }
                    catch (InvalidObjectException e) {
                        System.err.println("Failed to add " + split[0] + " to the training set. Feature detection failed.");
                    }
                    catch (IOException e2) {
                        System.err.println("Failed to add " + split[0] + " to the training set. IO Error occurred.");
                    }
                }
                else if (line.length() > 0) {
                    System.err.println("Skiping line " + lineNo + " due to invalid line formation.");
                }
                ++lineNo;
            }
        }
        catch (FileNotFoundException e3) {
            System.err.println("The trainingSet file was erased before successful loading.");
            return false;
        }
        catch (IOException e4) {
            System.err.println("Could not read the trainingSet file.");
            return false;
        }
        return true;
    }
    
    public boolean setTag(final String filename, final String tag) {
        final HashMap<String, String> trainingSet = new HashMap<String, String>();
        try (final BufferedReader br = new BufferedReader(new FileReader(this.trainingSet.toFile()))) {
            String line;
            while ((line = br.readLine()) != null) {
                final String[] split = line.split("\\s*,\\s*");
                if (split.length == 2 && !split[0].equals("Filename")) {
                    trainingSet.put(split[0], split[1]);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Cannot tag this image in the training set. Training set could not be opened?");
            return false;
        }
        trainingSet.put(filename, tag);
        try (final BufferedWriter bw = new BufferedWriter(new FileWriter(this.trainingSet.toFile()))) {
            bw.write("Filename, Type\n");
            for (final Map.Entry<String, String> imageTag : trainingSet.entrySet()) {
                bw.write(imageTag.getKey() + ", " + imageTag.getValue() + '\n');
            }
        }
        catch (IOException e) {
            System.err.println("Cannot tag this image in the training set. Training set could not be written");
            return false;
        }
        return true;
    }
    
    private File getImageFromClusteringDir(final String filename) {
        return Paths.get(this.trainingImages.toString(), filename).toFile();
    }
    
    private void createNewTrainingSetFile() throws IOException {
        this.trainingSet.toFile().createNewFile();
        try (final PrintWriter bw = new PrintWriter(new FileWriter(this.trainingSet.toFile()))) {
            bw.println("Filename, Type");
        }
    }
    
    public String[] cluster(final File file) {
        return this.cluster(file, 5);
    }
    
    public String[] cluster(final File file, final int k) {
        System.out.println("Loading clustering set...");
        if (!this.readTrainingSet()) {
            return null;
        }
        System.out.println("Finished loading");
        final String[] nearestStrings = new String[k];
        final float[] nearestVals = new float[k];
        try {
            final Vector attributeVector = ImageProcessing.getAttributeVector(file, this.configuration.getProperty("Algorithms"));
            for (final Map.Entry<Vector, String> e : this.trainingMap.entrySet()) {
                final float comp = e.getKey().compareTo(attributeVector);
                for (int i = 0; i < nearestVals.length; ++i) {
                    if (comp > nearestVals[i]) {
                        nearestVals[i] = comp;
                        nearestStrings[i] = e.getValue();
                    }
                }
            }
        }
        catch (IOException e2) {
            return null;
        }
        return nearestStrings;
    }
}
