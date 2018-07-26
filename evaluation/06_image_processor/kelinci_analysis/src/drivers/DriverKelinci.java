package drivers;

import com.stac.image.algorithms.filters.Intensify;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class DriverKelinci
{
    public static void main(final String[] args) {

    	if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		try {
			System.out.println("Loading image: " + args[0]);
			File imageFile = new File(args[0]);
			BufferedImage bi = ImageIO.read(imageFile);

			/*System.out.println("Pixels values:");
			for (int y = 0; y < bi.getHeight(); y++) {
				for (int x = 0; x < bi.getWidth(); x++) {
					System.out.println("bi["+y+"]["+x+"] = " + bi.getRGB(x, y));
				}
			}*/

			// RK: added this check from Engagement 2
        		/*if (bi.getWidth() * bi.getHeight() > 4) {
            			throw new RuntimeException("This image is too large. Please reduce your image size to less than 250000 pixels");
        		}*/
          // YN: only allow 2x2 images
            if (bi.getWidth() != 2 || bi.getHeight() != 2) {
            			throw new RuntimeException("it is not an 2x2 image");
        		}

	    	// test the Intensify filter
	    	Intensify i = new Intensify();
	    	i.filter(bi);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Done.");
    }
}
