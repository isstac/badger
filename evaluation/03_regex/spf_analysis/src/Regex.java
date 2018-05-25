import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.symbc.Debug;
import regexjdk8.Pattern;
import regexjdk8.PatternSyntaxException;

/**
 * Loads a file and a regex and matches the regex to the file.
 *
 * @author Rody Kersten.
 * 
 *         Adapted for symbolic analysis by Yannic Noller <nolleryc@gmail.com>.
 */
public class Regex {

    public static boolean regexSymbolic = false;
    public static boolean textSymbolic = true;

    public static final int MAX_REGEX_SIZE = 10;
    public static final int MAX_TEXT_SIZE = 100;

    private static String readInput(String inputFile, boolean insertSymbolicVariables, int maximumSize) {

        try (FileInputStream fis = new FileInputStream(inputFile)) {
            String resultString;

            if (insertSymbolicVariables) {

                // if this file will be mutated, then use bound for its size
                char[] str = new char[maximumSize];
                int dataReadCounter = 0;

                byte[] bytes = new byte[Character.BYTES];
                while (((fis.read(bytes)) != -1) && dataReadCounter < maximumSize) {
                    char value = ByteBuffer.wrap(bytes).getChar();
                    str[dataReadCounter++] = value;
                }
               
                resultString = new String(str);

                // Insert symbolic variables
                char[] str_symbolic = new char[resultString.length()];
                for (int i = 0; i < str_symbolic.length; i++) {
                    str_symbolic[i] = Debug.addSymbolicChar(resultString.charAt(i), "sym_" + i);
                }
                resultString = new String(str_symbolic);

            } else {
                List<Character> charList = new ArrayList<>();
                byte[] bytes = new byte[Character.BYTES];
                while (((fis.read(bytes)) != -1)) {
                    charList.add(ByteBuffer.wrap(bytes).getChar());
                }
                char[] str = new char[charList.size()];
                for (int i=0; i<charList.size(); i++) {
                    str[i] = charList.get(i);
                }
                resultString = new String(str);
            }

            return resultString;

        } catch (IOException e) {
            System.err.println("Error reading input");
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {

        if (regexSymbolic == textSymbolic) {
            throw new RuntimeException("not both should be symbolic or concrete at the same time");
        }

        String regex;
        String text;

        if (args.length == 2) {

            // First parameter is the fixed part, i.e. the non-symbolic part.
            String fileNameText;
            String fileNameRegex;
            if (regexSymbolic) {
                fileNameText = args[0].replace("#", ",");
                fileNameRegex = args[1].replace("#", ",");
            } else {
                fileNameRegex = args[0].replace("#", ",");
                fileNameText = args[1].replace("#", ",");
            }
            
            regex = readInput(fileNameRegex, regexSymbolic, MAX_REGEX_SIZE);
            text = readInput(fileNameText, textSymbolic, MAX_TEXT_SIZE);

        } else if (args.length == 1) {

            if (regexSymbolic) {
                String fileNameText = args[0].replace("#", ",");
                text = readInput(fileNameText, false, MAX_TEXT_SIZE);

                char[] charValues = new char[MAX_REGEX_SIZE];
                for (int i = 0; i < charValues.length; i++) {
                    charValues[i] = Debug.makeSymbolicChar("sym_" + i);
                }
                regex = new String(charValues);

            } else {
                String fileNameRegex = args[0].replace("#", ",");
                regex = readInput(fileNameRegex, false, MAX_REGEX_SIZE);

                char[] charValues = new char[MAX_TEXT_SIZE];
                for (int i = 0; i < charValues.length; i++) {
                    charValues[i] = Debug.makeSymbolicChar("sym_" + i);
                }
                text = new String(charValues);

            }

        } else {
            throw new RuntimeException("there should be at least one parameter for the fixed option");
        }

        if (regex == null || text == null)
            return;

        /* now perform the matching */
        try {
            boolean match = Pattern.matches(regex, text);
        } catch (PatternSyntaxException e) {
            System.err.println("Invalid pattern: " + regex);
        }
    }
}
