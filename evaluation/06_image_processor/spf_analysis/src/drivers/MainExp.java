package drivers;

import com.stac.mathematics.Mathematics;
import gov.nasa.jpf.symbc.Debug;

public class MainExp {
      
    public static int N;
    
    public static void main(final String[] args) {
    	
    	N = Integer.parseInt(args[0]);
    	
    	// check complexity of Mathematics.exp()
       	int x = Debug.makeSymbolicInteger("x");
       	Mathematics.exp(x,N);
    }
}
