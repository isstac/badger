import java.io.FileInputStream;
import java.io.IOException;

import gov.nasa.jpf.symbc.Debug;

/**
 * @author Rody Kersten
 * Adapted for symbolic analysis by Yannic Noller <nolleryc@gmail.com> - YN
 **/
public class QuickSortJDK15 {

    public static void main(String[] args) {
        int N = 64;
        int a[] = new int[N];
        
        if (args.length == 1) {
            
            String fileName = args[0].replace("#", ",");
            
            try (FileInputStream fis = new FileInputStream(fileName)) {
                int b;
                int i = 0;
                while (((b = fis.read()) != -1) && (i < N)) {
                    a[i] = b;
                    i++;
                }
            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }
            // Insert symbolic variables.
            for (int i = 0; i < N; i++) {
                a[i] = Debug.addSymbolicInt(a[i], "sym_" + i);
            }

        } else {
            for (int i = 0; i < N; i++) {
                a[i] = Debug.makeSymbolicInteger("sym_" + i);
            }
        }

        // System.out.println("Read int array: " + Arrays.toString(a));
        Arrays.sort(a);
        // System.out.println("Sorted: " + Arrays.toString(a));
    }

}
