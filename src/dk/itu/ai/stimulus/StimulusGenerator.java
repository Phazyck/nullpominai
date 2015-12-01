package dk.itu.ai.stimulus;

import com.anji.util.Configurable;

import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.play.GameEngine;

/**
 * Interface for Stimulus generator objects, for use with the NeatAI
 * 
 * This interface extends com.anji.util.Configurable. 
 * If implementing this interface, be sure to read the requirements of Configurable interface as well.
 * 
 * @author Kas
 */
public interface StimulusGenerator extends Configurable { 
	public double[] makeStimuli(GameEngine engine, Field field);
}