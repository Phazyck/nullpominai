package dk.itu.ai.stimulus;

import com.anji.util.Properties;

import dk.itu.ai.Util;
import dk.itu.ai.navigation.Move;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * Stimulus set: 
 * 0: Amount of lines cleared by that move 
 * 
 * When using this class, set the property "stimulus.size" to 1
 * 
 * @author Kas
 */
public class LinesCleared implements StimulusGenerator {

	@Override
	public void init(Properties props) throws Exception {
		// Nothing to initialize here

	}
	
	@Override
	public double[] makeStimuli(GameEngine engine, Move move) {
		Field field = Util.getFieldAfter(engine, move);
		
		double[] stimuli = makeStimuli(field);
		
		return(stimuli);
	}

	private double[] makeStimuli(Field field) {
		double[] result = new double[1];
		
		result[0] = field.checkLineNoFlag();
		
		return result;
	}

}
