package driver;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import gov.nasa.jpf.symbc.Debug;

public class Driver {

    public static void main(String[] args) {
        int N = 250;

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            try (FileInputStream fis = new FileInputStream(fileName)) {

                // the BZIP2 compressor that contains a vulnerability
                BZip2CompressorOutputStream out = new BZip2CompressorOutputStream(new NullOutputStream());

                int b;
                int i = 0;
                while (((b = fis.read()) != -1) && (i < N)) { // read 250 bytes max
                    // out.write(b);
                    out.write(Debug.addSymbolicInt(b, "sym_" + i));
                    i++;
                }

                out.flush();
                out.finish();
                out.close();

            } catch (IOException e) {
                System.err.println("Error reading input file");
                e.printStackTrace();
            }
        } else {
            try {
                // the BZIP2 compressor that contains a vulnerability
                BZip2CompressorOutputStream out = new BZip2CompressorOutputStream(new NullOutputStream());

                for (int i = 0; i < N; i++) {
                    out.write(Debug.makeSymbolicInteger("sym_" + i));
                }
                out.flush();
                out.finish();
                out.close();
            } catch (IOException e) {
                System.err.println("Error reading input file");
                e.printStackTrace();
            }
        }

        System.out.println("Done.");
    }

    /**
     * Stream to /dev/null. Used to redirect output of target program.
     * 
     * I know something like this is also in Apache Commons IO, but if I include it here, we don't need any libs on the
     * classpath when running the Kelinci server.
     * 
     * @author rodykers
     *
     */
    private static class NullOutputStream extends ByteArrayOutputStream {

        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
        }
    }
}
