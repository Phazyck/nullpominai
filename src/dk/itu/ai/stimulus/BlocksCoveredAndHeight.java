package dk.itu.ai.stimulus;

import com.anji.util.Properties;

import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * Stimulus set: 
 * 0: Amount of empty cells in the field, that has a block covering above it
 * 1: Highest block y coordinate 
 * 
 * When using this class, set the property "stimulus.size" to 2
 * 
 * @author Kas
 */
public class BlocksCoveredAndHeight implements StimulusGenerator {

	@Override
	public void init(Properties props) throws Exception {
		// Nothing to initialize here
	}

	@Override
	public double[] makeStimuli(GameEngine engine, Field field) {
		double[] result = new double[2];
		
		// Process potetial line clears
		if (field.checkLine() > 0) {
			field.clearLine();
			field.downFloatingBlocks();
		}
		
		result[0] = field.getHowManyBlocksCovered();
		result[1] = field.getHighestBlockY();
		
		return result;
	}

}
