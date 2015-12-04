package dk.itu.ai;

import java.util.List;

import mu.nu.nullpo.game.subsystem.mode.GameMode;
import mu.nu.nullpo.game.subsystem.mode.GradeMania3Mode;

import org.apache.log4j.Logger;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.integration.TargetFitnessFunction;
import com.anji.integration.TranscriberException;
import com.anji.tournament.PlayerTranscriber;
import com.anji.util.Configurable;
import com.anji.util.Properties;

public class TGMFitnessFunction implements BulkFitnessFunction, Configurable {

	private static final long serialVersionUID = 1L;
	
	private final static String TRANSCRIBER_CLASS_KEY = "nullpominai.transcriber";
	

	private ActivatorTranscriber activatorFactory;

	private static Logger logger = Logger.getLogger( TargetFitnessFunction.class );
	
	private static final String[] FITNESS_TEST_SEEDS = {"15478945", "897494638", "4697358"}; // I facerolled my numpad, sue me!  -Kas
	
	
	@Override
	public void init(Properties props) throws Exception {
		activatorFactory = (ActivatorTranscriber) props.newObjectProperty( TRANSCRIBER_CLASS_KEY );
	}

	@Override
	public void evaluate(List subjects) {
		List<Chromosome> genotypes = (List<Chromosome>) subjects;
		GameMode simulationMode = new GradeMania3Mode();
		String simulationRulePath = "config\\rule\\Classic3.rul";
		Simulator simulation;
		
		for(Chromosome chromosome : genotypes){
			Activator activator;
			try {
				activator = activatorFactory.newActivator( chromosome );
				
				// Get average fitness over all test runs
				int fitness = 0;
				
				int total = 0;
				for (String seed : FITNESS_TEST_SEEDS) {
					simulation = new Simulator(simulationMode, simulationRulePath, new NeatAI(activator));
					simulation.setCustomSeed(seed);
					simulation.runSimulation();
					total += simulation.getLevel();
				}
				
				fitness = total / FITNESS_TEST_SEEDS.length;
				
				chromosome.setFitnessValue(fitness);
			} catch (TranscriberException e) {
				logger.warn( "transcriber error: " + e.getMessage() );
				chromosome.setFitnessValue( 1 );
			}
		}
		
	}

	@Override
	public int getMaxFitnessValue() {
		// TODO Auto-generated method stub
		return 0;
	}

}
