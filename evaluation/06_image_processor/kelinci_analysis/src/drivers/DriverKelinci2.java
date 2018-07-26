package drivers;

import com.stac.image.algorithms.filters.Intensify;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class DriverKelinci2
{    
	public static void main(final String[] args) {

		if (args.length != 1) {
			System.out.println("Expects file name as parameter");
			return;
		}

		System.out.println("Loading image: " + args[0]);

		// image size
		int X = 2;
		int Y = 2;

		BufferedImage bi = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);

		try (FileInputStream fis = new FileInputStream(args[0])) {

			byte[] bytes = new byte[4];
			int pixels_read = 0;

			while ( (fis.read(bytes) != -1) && (pixels_read < X*Y) )  {
				int pixel = ByteBuffer.wrap(bytes).getInt();
				int x = pixels_read % X;
				int y = pixels_read / X;
				System.out.println("pixel["+x+"]["+y+"] = " + pixel);
				bi.setRGB(x, y, pixel);
				pixels_read++;
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
