package drivers;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.stac.image.algorithms.filters.Intensify;

import gov.nasa.jpf.symbc.Debug;

public class FullImageDriver {

    public static void main(final String[] args) {
        BufferedImage bi;

        if (args.length == 1) {
            // Symcrete execution, building the trie.

            String fileName = args[0];
            
            // Read input file.
            System.out.println("Loading image: " + fileName);

            // input file format: id:width:height:imageType
            String[] splittedFileName = fileName.split(":");
            int width = Integer.valueOf(splittedFileName[1]);
            int height = Integer.valueOf(splittedFileName[2]);
            int imageType = Integer.valueOf(splittedFileName[3]);

            bi = new BufferedImage(width, height, imageType);
            try (FileInputStream fis = new FileInputStream(fileName.replace("#", ","))) {

                byte[] bytes = new byte[4];
                int pixels_read = 0;

                while ((fis.read(bytes) != -1) && (pixels_read < width * height)) {
                    int pixel = ByteBuffer.wrap(bytes).getInt();
                    int x = pixels_read % width;
                    int y = pixels_read / height;
                    System.out.println("pixel[" + x + "][" + y + "] = " + pixel);
                    bi.setRGB(x, y, pixel);
                    pixels_read++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            // Introduce symbolic variables.
            System.out.println("Inserting symbolic variables in image...");
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bi.setRGB(x, y, Debug.addSymbolicInt(bi.getRGB(x, y), "sym_" + x + "_" + y));
                }
            }

        } else {
            // symbolic execution including replay of trie TODO only works for 2x2 image

            bi = new BufferedImage(2, 2, 5);
            for (int x = 0; x < bi.getWidth(); x++) {
                for (int y = 0; y < bi.getHeight(); y++) {
                    bi.setRGB(x, y, Debug.makeSymbolicInteger("sym_" + x + "_" + y));
                }
            }

        }

        // test the Intensify filter
        System.out.println("Run actual analysis...");
        Intensify i = new Intensify();
        i.filter(bi);

    }

}
