package drivers;

import com.stac.image.algorithms.filters.Intensify;
import java.awt.image.BufferedImage;

public class MainIntensify
{
    public static int N;
    
    public static void main(final String[] args) {
    	
    	N = Integer.parseInt(args[0]);

    	int width = N;
    	int height = N;

    	// The model of BufferedImage in jpf-symbc makes an image with symbolic pixel values
    	BufferedImage bi = new BufferedImage(width,height,0);
    	
    	// test the Intensify filter
    	Intensify i = new Intensify();
    	i.filter(bi);
    }
}