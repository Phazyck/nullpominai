package dk.itu.ai.stimulus;

import com.anji.util.Properties;

import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.play.GameEngine;


/**
 * Stimulus set: 
 * 0-9: Relative block field surface contour. 
 * 		 Tallest column is 0, every column that is N lower than tallest is set to N. 
 * 10:  Highest block absolute y-coordinate 
 * 11:  Change in amount of empty cells in the field, that has a block covering above it.
 * 12:  Amount of lines cleared by that move 
 * 13:  Blocks filled in the bottom left 9x4 area.
 * 14:  Change in highest y-coordinate.
 * When using this class, set the property "stimulus.size" to 15
 * 
 * @author Oli
 */
public class StimPack implements StimulusGenerator{

	@Override
	public void init(Properties props) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] makeStimuli(GameEngine engine, Field newField, Field oldField) {
double[] result = new double[15];
		
		// Process potetial line clears
		int linesCleared = newField.checkLine();  
		if (linesCleared > 0) {
			newField.clearLine();
			newField.downFloatingBlocks();
		}
		
		// Find the Y-value of the higest row as well
		// (Y is positive going downward, just as in screen pixels
		int highestBlockY = newField.getHighestBlockY(); // Can this ever be 20? think it takes a clear field...
		
		for (int x = 0; x < 10; x++) {
			result[x] = newField.getHighestBlockY(x); // getHighestBLockY returns -1 if no blocks are present, very inconvenient
		}
		
		// assert(highestBlockY >= 0); // Apparently blocks can be outside field (y < 0), so this assertion is incorrect
		assert(highestBlockY <= 20); // But they should never be BELOW the ground... right?
		
		// Make all the heights relative, so that the tallest column is 0, and the other columns in relation to this
		for (int x = 0; x < 10; x++) {
			result[x] -= highestBlockY;
			
			assert(result[x] >= 0);
		}
		
		// Pass the total height as well
		result[10] = newField.getHighestBlockY();
		
		int blocksBefore = oldField.getHowManyBlocksCovered();
		int blocksAfter = newField.getHowManyBlocksCovered();
		result[11] = blocksBefore - blocksAfter;
		
		result[12] = linesCleared;
		
		int blockCount = 0;
		
		int w = newField.getWidth() - 1;
		int h = 4;
		
		for(int x = 0; x < w; ++x)
		{
			for(int dy = 0; dy < h; ++dy)
			{
				int y = newField.getHeight() - dy;
				
				if(!newField.getBlockEmpty(x, y))
				{
					++blockCount;
				}
			}
		}
		
		result[13] = (double)blockCount; 
		
		result[14] = oldField.getHighestBlockY() - newField.getHighestBlockY();

		
		return result;
	}

}
