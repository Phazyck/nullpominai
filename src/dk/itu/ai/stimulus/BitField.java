package dk.itu.ai.stimulus;

import com.anji.util.Properties;

import dk.itu.ai.Util;
import dk.itu.ai.navigation.Move;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * Stimulus set: 
 * 0-199: Whether a specific block is occupied or not
 * 
 * When using this class, set the property "stimulus.size" to 2
 * 
 * @author Kas
 */
public class BitField implements StimulusGenerator {

	@Override
	public void init(Properties props) throws Exception {
		// Nothing to do here
	}
	
	@Override
	public double[] makeStimuli(GameEngine engine, Move move) {
		Field field = Util.getFieldAfter(engine, move);
		
		double[] stimuli = makeStimuli(field);
		
		return(stimuli);
	}

	private double[] makeStimuli(Field field) {
		double[] result = new double[200];
		
		// Process potetial line clears
		if (field.checkLine() > 0) {
			field.clearLine();
			field.downFloatingBlocks();
		}
		
		assert(field.getWidth() == 10);
		assert(field.getHeightWithoutHurryupFloor() == 20);
		
		for (int x = 0; x < field.getWidth(); x++) {
			for (int y = 0; y < field.getHeightWithoutHurryupFloor(); y++) {
				if (!field.getBlockEmpty(x, y)) {
					result[x*field.getWidth() + y] = 1.0;
				}
			}
		}
		
		
		return result;
	}
}
