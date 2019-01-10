package edu.cmu.sv.badger.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;

import edu.cmu.sv.badger.analysis.BranchCountState;
import edu.cmu.sv.badger.analysis.CoverageAnalyzer;
import edu.cmu.sv.badger.analysis.CoverageExplorationHeuristic;
import edu.cmu.sv.badger.analysis.ExplorationHeuristic;
import edu.cmu.sv.badger.analysis.WCAExplorationHeuristic;
import edu.cmu.sv.badger.analysis.InstructionCountState;
import edu.cmu.sv.badger.analysis.StateBuilderFactory;
import edu.cmu.sv.badger.analysis.TrieAnalyzer;
import edu.cmu.sv.badger.analysis.WCAAnalyzer;
import edu.cmu.sv.badger.io.ByteImageProcessorIOUtils;
import edu.cmu.sv.badger.io.ByteTextIOUtils;
import edu.cmu.sv.badger.io.CharArrayIOUtils;
import edu.cmu.sv.badger.io.CharArrayIOUtilsEngagement;
import edu.cmu.sv.badger.io.DynamicCharArrayIOUtils;
import edu.cmu.sv.badger.io.DynamicIntArrayIOUtils;
import edu.cmu.sv.badger.io.FullIntArrayIOUtils;
import edu.cmu.sv.badger.io.IOUtils;
import edu.cmu.sv.badger.io.ImageByteDoubleIOUtils;
import edu.cmu.sv.badger.io.ImageDoubleDoubleIOUtils;
import edu.cmu.sv.badger.io.ImageProcessorIOUtils;
import edu.cmu.sv.badger.io.IntArrayIOUtils;
import edu.cmu.sv.badger.io.MultipleIntArrayIOUtils;

/**
 * Parses the configuration file of Badger and is used as input data object.
 * 
 * @author Yannic Noller <nolleryc@gmail.com> - YN
 *
 */
public class BadgerInput {

    /* Directories */
    public String initialInputDir;
    public Optional<String> syncInputdir;
    public String exportDir;
    public String tmpDir;

    /* Technical Parameters */
    public int cycleWaitingSec;
    public int maximumNumberOfSymExeIterations;
    public int secUntilFirstCycle;
    public int numberOfAdditionalDecisions;

    /* JPF */
    public String jpf_classpath;
    public String jpf_targetClass;
    public String jpf_argument;

    /* SPF */
    public String spf_symbolicMethod;
    public String spf_dp;
    public Optional<String> symMaxInt;
    public Optional<String> symMinInt;
    public Optional<String> symMaxChar;
    public Optional<String> symMinChar;
    public Optional<String> symMaxByte;
    public Optional<String> symMinByte;
    public Optional<String> symMaxDouble;
    public Optional<String> symMinDouble;
    public Optional<String> symPrintDebug;
    public Optional<String> symDefaultValue;
    public Optional<String> symOptimizeChoices;
    public Optional<String> symListener;
    
    /* Analysis */
    public TrieAnalyzer trieAnalysisMethod;
    public ExplorationHeuristic explorationHeuristic;
    public Optional<StateBuilderFactory> stateBuilderFactory;
    public boolean useUserDefinedCost;

    /* Input Processing / Generation */
    public IOUtils ioUtils;
    public Integer[] inputSizes;
    public int initialId;

    /* Statistics */
    public boolean printStatistics;
    public String importStatisticsFile;
    public String generationStatisticsFile;
    public String exportStatisticsFile;
    public String trieStatisticsFile;
    public boolean printPC;
    public String pcMappingFile;

    /* Print Trie As Dot Files */
    public boolean printTrieAsDot;
    public Optional<Integer> printTrieMaxDepth;

    public BadgerInput(Properties prop) {

        /* Check property file for missing mandatory keys. */
        List<BadgerInputKeys> missingKeys = checkForMissingMandatoryProperites(prop);
        if (!missingKeys.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            missingKeys.forEach(key -> sb.append(key.name + ","));
            throw new RuntimeException("Configuration misses mandatory keys: " + sb.toString());
        }

        /* Directories */
        this.initialInputDir = prop.getProperty(BadgerInputKeys.INITIAL_INPUT_DIR.name);
        this.syncInputdir = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYNC_INPUT_DIR.name));
        this.exportDir = prop.getProperty(BadgerInputKeys.EXPORT_DIR.name);
        this.tmpDir = prop.getProperty(BadgerInputKeys.TMP_DIR.name, "./tmp");

        /* Technical Parameters */
        try {
            this.cycleWaitingSec = NumberUtils.createInteger(prop.getProperty(BadgerInputKeys.CYCLE_WAITING_SEC.name));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Value of " + BadgerInputKeys.CYCLE_WAITING_SEC.name + " is not a number!");
        }
        try {
            this.maximumNumberOfSymExeIterations = NumberUtils
                    .createInteger((prop.getProperty(BadgerInputKeys.MAX_NUMBER_SYMEXE_ITERATIONS.name)));
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Value of " + BadgerInputKeys.MAX_NUMBER_SYMEXE_ITERATIONS.name + " is not a number!");
        }
        try {
            this.secUntilFirstCycle = NumberUtils
                    .createInteger(prop.getProperty(BadgerInputKeys.SEC_UNTIL_FIRST_CYCLE.name, "0"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Value of " + BadgerInputKeys.SEC_UNTIL_FIRST_CYCLE.name + " is not a number!");
        }
        try {
            this.numberOfAdditionalDecisions = NumberUtils
                    .createInteger((prop.getProperty(BadgerInputKeys.NUMBER_OF_ADDITIONAL_STEPS.name, "0")));
        } catch (NumberFormatException e) {
            throw new RuntimeException(
                    "Value of " + BadgerInputKeys.NUMBER_OF_ADDITIONAL_STEPS.name + " is not a number!");
        }

        /* JPF */
        this.jpf_classpath = prop.getProperty(BadgerInputKeys.APPLICATION_CLASSPATH.name);
        this.jpf_targetClass = prop.getProperty(BadgerInputKeys.APPLICATION_TARGET_CLASS.name);
        this.jpf_argument = prop.getProperty(BadgerInputKeys.APPLICATION_ARGUMENT.name, "@@");
        this.jpf_argument = jpf_argument.replaceAll(",", "#");
        this.jpf_argument = jpf_argument.replaceAll(" ", ",");

        /* SPF */
        this.spf_symbolicMethod = prop.getProperty(BadgerInputKeys.SYMBOLIC_METHOD.name);
        this.spf_dp = prop.getProperty(BadgerInputKeys.DECISION_PRCEDURE.name, "z3");
        this.symMaxInt = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_INT.name));
        this.symMinInt = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_INT.name));
        this.symMaxChar = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_CHAR.name));
        this.symMinChar = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_CHAR.name));
        this.symMaxByte = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_BYTE.name));
        this.symMinByte = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_BYTE.name));
        this.symMaxDouble = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MAX_DOUBLE.name));
        this.symMinDouble = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_MIN_DOUBLE.name));
        this.symPrintDebug = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_DEBUG_PRINT.name));
        this.symDefaultValue = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_DEFAULT_DONT_CARE_VALUE.name));
        this.symOptimizeChoices = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_OPTIMIZECHOICES.name));
        this.symListener = Optional.ofNullable(prop.getProperty(BadgerInputKeys.SYM_LISTENER.name));

        /* Analysis */
        String analysisMethod = prop.getProperty(BadgerInputKeys.ANALYSIS_METHOD.name, WCAAnalyzer.ID);
        String selectedExplorationHeuristic = prop.getProperty(BadgerInputKeys.ANALYSIS_EXPLORATION_HEURISTIC.name);

        switch (analysisMethod) {
        case WCAAnalyzer.ID:
            if (selectedExplorationHeuristic.equals(WCAExplorationHeuristic.HIGHEST_COST_HIGHEST_NODE.ID)) {
                explorationHeuristic = WCAExplorationHeuristic.HIGHEST_COST_HIGHEST_NODE;
            } else if (selectedExplorationHeuristic.equals(WCAExplorationHeuristic.HIGHEST_COST_LOWEST_NODE.ID)) {
                explorationHeuristic = WCAExplorationHeuristic.HIGHEST_COST_LOWEST_NODE;
            } else if (selectedExplorationHeuristic.equals(WCAExplorationHeuristic.LOWEST_COST_HIGHEST_NODE.ID)) {
                explorationHeuristic = WCAExplorationHeuristic.LOWEST_COST_HIGHEST_NODE;
            } else if (selectedExplorationHeuristic.equals(WCAExplorationHeuristic.LOWEST_COST_LOWEST_NODE.ID)) {
                explorationHeuristic = WCAExplorationHeuristic.LOWEST_COST_LOWEST_NODE;
            } else {
                throw new RuntimeException("Unknown value for " + BadgerInputKeys.ANALYSIS_EXPLORATION_HEURISTIC.name
                        + ": " + selectedExplorationHeuristic + ". Check whether you have selected a suitable "
                        + WCAAnalyzer.ID + " heuristic.");
            }
            this.trieAnalysisMethod = new WCAAnalyzer(explorationHeuristic);
            break;
        case CoverageAnalyzer.ID:
            if (selectedExplorationHeuristic.equals(CoverageExplorationHeuristic.BRANCH_COV_HIGHEST_NODE.ID)) {
                explorationHeuristic = CoverageExplorationHeuristic.BRANCH_COV_HIGHEST_NODE;
            } else if (selectedExplorationHeuristic
                    .equals(CoverageExplorationHeuristic.BRANCH_COV_LOWEST_NODE.ID)) {
                explorationHeuristic = CoverageExplorationHeuristic.BRANCH_COV_LOWEST_NODE;
            } else if (selectedExplorationHeuristic
                    .equals(CoverageExplorationHeuristic.BRANCH_COV_HIGHEST_NODE_EXPORT_ALL.ID)) {
                explorationHeuristic = CoverageExplorationHeuristic.BRANCH_COV_HIGHEST_NODE_EXPORT_ALL;
            } else if (selectedExplorationHeuristic
                    .equals(CoverageExplorationHeuristic.BRANCH_COV_LOWEST_NODE_EXPORT_ALL.ID)) {
                explorationHeuristic = CoverageExplorationHeuristic.BRANCH_COV_LOWEST_NODE_EXPORT_ALL;
            } else {
                throw new RuntimeException("Unknown value for " + BadgerInputKeys.ANALYSIS_EXPLORATION_HEURISTIC.name
                        + ": " + selectedExplorationHeuristic + ". Check whether you have selected a suitable "
                        + CoverageAnalyzer.ID + " heuristic.");
            }
            this.trieAnalysisMethod = new CoverageAnalyzer(explorationHeuristic);
            break;
        default:
            throw new RuntimeException(
                    "Unknown value for " + BadgerInputKeys.ANALYSIS_METHOD.name + ": " + analysisMethod);
        }

        if (analysisMethod.equals(WCAAnalyzer.ID)) {
            String wcaMetric = prop.getProperty(BadgerInputKeys.ANALYSIS_WCA_METRIC.name);
            if (wcaMetric != null) {
                switch (wcaMetric) {
                case BranchCountState.ID:
                    this.stateBuilderFactory = Optional.of(new BranchCountState.BranchBuilderFactory());
                    this.useUserDefinedCost = false;
                    break;
                case InstructionCountState.ID:
                    this.stateBuilderFactory = Optional.of(new InstructionCountState.InstructionBuilderFactory());
                    this.useUserDefinedCost = false;
                    break;
                case "userdefined":
                    this.stateBuilderFactory = Optional.empty(); // metric values will be user defined
                    this.useUserDefinedCost = true;
                    break;
                case "jumps-userdefined": // we want to measure jumps and use this information in userdefined costs.
                    this.stateBuilderFactory = Optional.of(new BranchCountState.BranchBuilderFactory());
                    this.useUserDefinedCost = true;
                    break;
                default:
                    throw new RuntimeException(
                            "Unkown value for " + BadgerInputKeys.ANALYSIS_WCA_METRIC.name + ": " + wcaMetric);
                }
            } else {
                throw new RuntimeException(BadgerInputKeys.ANALYSIS_METHOD.name + "=" + WCAAnalyzer.ID
                        + " needs the existence of values for " + BadgerInputKeys.ANALYSIS_WCA_METRIC.name);
            }

        } else {
            stateBuilderFactory = Optional.empty();
            this.useUserDefinedCost = false;
        }

        /* Input Processing / Generation */
        String ioUtilsSelection = prop.getProperty(BadgerInputKeys.IO_UTILS.name);
        String inputSizesString = prop.getProperty(BadgerInputKeys.IO_INPUT_SIZES.name);
        String[] inputSizesStringSplitted = inputSizesString.split(" ");
        this.inputSizes = new Integer[inputSizesStringSplitted.length];
        for (int i = 0; i < inputSizes.length; i++) {
            try {
                this.inputSizes[i] = NumberUtils.createInteger(inputSizesStringSplitted[i]);
            } catch (NumberFormatException e) {
                throw new RuntimeException("Values of " + BadgerInputKeys.IO_INPUT_SIZES.name + " are no numbers!");
            }
        }
        switch (ioUtilsSelection) {
        case ImageProcessorIOUtils.ID:
            this.ioUtils = new ImageProcessorIOUtils();
            break;
        case ByteImageProcessorIOUtils.ID:
            this.ioUtils = new ByteImageProcessorIOUtils();
            break;
        case IntArrayIOUtils.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + IntArrayIOUtils.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new IntArrayIOUtils(inputSizes[0]);
            break;
        case CharArrayIOUtils.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + CharArrayIOUtils.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new CharArrayIOUtils(inputSizes[0]);
            break;
        case CharArrayIOUtilsEngagement.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + CharArrayIOUtilsEngagement.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new CharArrayIOUtilsEngagement(inputSizes[0]);
            break;
        case ByteTextIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + ByteTextIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new ByteTextIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case FullIntArrayIOUtils.ID:
            if (inputSizes.length != 1) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + FullIntArrayIOUtils.ID
                        + " needs one value definition for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new FullIntArrayIOUtils(inputSizes[0]);
            break;
        case DynamicIntArrayIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + DynamicIntArrayIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new DynamicIntArrayIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case MultipleIntArrayIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + MultipleIntArrayIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new MultipleIntArrayIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case DynamicCharArrayIOUtils.ID:
            if (inputSizes.length != 2) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + DynamicCharArrayIOUtils.ID
                        + " needs two value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new DynamicCharArrayIOUtils(inputSizes[0], inputSizes[1]);
            break;
        case ImageByteDoubleIOUtils.ID:
            if (inputSizes.length != 3) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + ImageByteDoubleIOUtils.ID
                        + " needs three value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new ImageByteDoubleIOUtils(inputSizes[0], inputSizes[1], inputSizes[2]);
            break;
        case ImageDoubleDoubleIOUtils.ID:
            if (inputSizes.length != 3) {
                throw new RuntimeException(BadgerInputKeys.IO_UTILS.name + "=" + ImageDoubleDoubleIOUtils.ID
                        + " needs three value definitions for " + BadgerInputKeys.IO_INPUT_SIZES.name);
            }
            this.ioUtils = new ImageDoubleDoubleIOUtils(inputSizes[0], inputSizes[1], inputSizes[2]);
            break;
            
        default:
            throw new RuntimeException("Unkown value for " + BadgerInputKeys.IO_UTILS.name + ": " + ioUtilsSelection);
        }
        try {
            this.initialId = NumberUtils.createInteger(prop.getProperty(BadgerInputKeys.IO_INITIAL_ID.name, "0"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Value of " + BadgerInputKeys.IO_INITIAL_ID.name + " is not a number!");
        }

        /* Statistics */
        this.printStatistics = Boolean.valueOf(prop.getProperty(BadgerInputKeys.PRINT_STATISTICS.name, "true"));
        this.importStatisticsFile = prop.getProperty(BadgerInputKeys.IMPORT_STATISTICS_FILE.name,
                "import-statistic.txt");
        this.generationStatisticsFile = prop.getProperty(BadgerInputKeys.GENERATION_STATISTICS_FILE.name,
                "generation-statistic.txt");
        this.exportStatisticsFile = prop.getProperty(BadgerInputKeys.EXPORT_STATISTICS_FILE.name,
                "export-statistic.txt");
        this.trieStatisticsFile = prop.getProperty(BadgerInputKeys.INTERNAL_TRIE_STATISTICS_FILE.name,
                "trie-statistic.txt");
        this.printStatistics = Boolean
                .valueOf(prop.getProperty(BadgerInputKeys.PRINT_PC_INFO.name, String.valueOf(this.printStatistics)));
        this.pcMappingFile = prop.getProperty(BadgerInputKeys.PC_MAPPING_FILE.name, "pcMap.txt");

        /* Print Trie As Dot Files */
        this.printTrieAsDot = Boolean.valueOf(prop.getProperty(BadgerInputKeys.PRINT_TRIE.name));
        this.printTrieMaxDepth = Optional.ofNullable(Integer.valueOf(prop.getProperty(BadgerInputKeys.PRINT_TRIE_MAX_DEPTH.name)));
    }

    private List<BadgerInputKeys> checkForMissingMandatoryProperites(Properties prop) {
        List<BadgerInputKeys> missingKeys = new ArrayList<>();
        for (BadgerInputKeys key : BadgerInputKeys.mandatoryKeys) {
            if (!prop.containsKey(key.name)) {
                missingKeys.add(key);
            }
        }
        return missingKeys;
    }

    public static BadgerInput loadFromConfigFile(String configFilePath) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(configFilePath);
            prop.load(input);
            return new BadgerInput(prop);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("[ERROR] Configuration file not found", e);
        } catch (IOException e) {
            throw new RuntimeException("[ERROR] Error configuraiton file", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
