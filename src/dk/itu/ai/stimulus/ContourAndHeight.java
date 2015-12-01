package dk.itu.ai.stimulus;

import com.anji.util.Properties;

import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * Stimulus set: 
 * 0-9: Relative block field surface contour. 
 * 		 Tallest column is 0, every column that is N lower than tallest is set to N. 
 * 10:  Highest block absolute y-coordinate 
 * 
 * When using this class, set the property "stimulus.size" to 11
 * 
 * @author Kas
 */
public class ContourAndHeight implements StimulusGenerator {

	@Override
	public void init(Properties props) throws Exception {
		// No initialization needed here
	}
	
	@Override
	public double[] makeStimuli(GameEngine engine, Field field) {
		/**
		 * 11 inputs:
		 * 0-9: Contour of block field surface. Top row of contour is Y=0.
		 * 10: Absolute block field height of top row of contour. 
		 */
		
		double[] result = new double[11];
		
		// Find the Y-value of the higest row as well
		// (Y is positive going downward, just as in screen pixels
		int highestBlockY = field.getHighestBlockY(); // Can this ever be 20? think it takes a clear field...
		
		for (int x = 0; x < 10; x++) {
			result[x] = field.getHighestBlockY(x); // getHighestBLockY returns -1 if no blocks are present, very inconvenient
		}
		
		// assert(highestBlockY >= 0); // Apparently blocks can be outside field (y < 0), so this assertion is incorrect
		assert(highestBlockY <= 20); // But they should never be BELOW the ground... right?
		
		// Make all the heights relative, so that the tallest column is 0, and the other columns in relation to this
		for (int x = 0; x < 10; x++) {
			result[x] -= highestBlockY;
			
			assert(result[x] >= 0);
		}
		
		// Pass the total height as well
		result[10] = field.getHighestBlockY();
		
		return result;
	}

	

}
