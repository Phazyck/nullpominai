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
import com.anji.util.Configurable;
import com.anji.util.Properties;

public class TGMFitnessFunction implements BulkFitnessFunction, Configurable {

	private static final long serialVersionUID = 1L;

	private ActivatorTranscriber activatorFactory;

	private static Logger logger = Logger.getLogger( TargetFitnessFunction.class );
	
	@Override
	public void init(Properties props) throws Exception {
		// TODO Auto-generated method stub
		
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
				simulation = new Simulator(simulationMode, simulationRulePath, new BasicNeatAI(activator));
				simulation.runSimulation();
				int fitness = simulation.getGM3Level();
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
