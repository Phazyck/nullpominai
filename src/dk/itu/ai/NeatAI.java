package dk.itu.ai;

import java.io.IOException;

import org.jgap.Chromosome;
import org.jgap.InvalidConfigurationException;

import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.integration.TranscriberException;
import com.anji.neat.NeatConfiguration;
import com.anji.persistence.FilePersistence;
import com.anji.persistence.Persistence;
import com.anji.util.Properties;

import dk.itu.ai.stimulus.StimulusGenerator;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameEngine.Status;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;

public class NeatAI extends DummyAI {
	
	/**
	 * Property key for Stimulus generation object
	 *  When changing this object, make sure that the property "stimulus.size"
	 *  in the properties file is set to the corresponding value as well.
	 *  The corresponding Stimulus class should specify this value. 
	 */
	private final static String STIMULUS_CLASS_KEY = "nullpominai.stimulusgenerator";
	
	private boolean targetObtained = false;
	private int pieceNr = 0;
	private int missCount = 0;

	StimulusGenerator stimulusGenerator;
	
	// Values for loading neural networks
	private final static String TRANSCRIBER_CLASS_KEY = "nullpominai.transcriber";
	private final static String CHROMOSONE_ID_KEY = "nullpominai.chromosone.id";

	Properties properties;


	@Override
	public String getName() {
		return "Neaty v0.1B";
	}

	// Neural Network activation object
	private Activator networkActivator;

	/**
	 * This constructor should only be called by the game
	 */
	public NeatAI() {
		this(null);
	}

	/**
	 * This constructor intended for when used as part of fitness evaluation during evolution
	 * @param ac Pre-initialized network activator
	 */
	public NeatAI(Activator ac) {
		networkActivator = ac;

		try {
			properties = new Properties( "nullpomino.properties" );
			stimulusGenerator = (StimulusGenerator) properties.newObjectProperty(STIMULUS_CLASS_KEY);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); // TODO figure out to do something smarter here? 
		}
	}

	/**
	 * This is for enabling playing with a specific chromosone 
	 */
	@Override
	public void init(GameEngine engine, int playerID) {
		super.init(engine, playerID);

		// Load and initialize a preexisting network, if no network was supplied
		if (networkActivator == null) {
			try {

				NeatConfiguration config = new NeatConfiguration( properties );

				Persistence persistence = new FilePersistence();
				persistence.init(properties);

				String chromosone_ID = properties.getProperty(CHROMOSONE_ID_KEY);

				Chromosome chromosome = persistence.loadChromosome(chromosone_ID, config);

				ActivatorTranscriber transcriber = (ActivatorTranscriber) properties.newObjectProperty( TRANSCRIBER_CLASS_KEY );

				networkActivator = transcriber.newActivator( chromosome );

			} catch (InvalidConfigurationException | TranscriberException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
		
	private int getInput(int x, int y, int rt, Controller ctrl)
	{
		int input = 0;
		
		int deltaRt = Util.getDeltaRt(rt, bestRt);
		int deltaX = Util.getDeltaX(x, bestX);
		
		//--- Rotation
		
		if(deltaRt < 0 && !ctrl.isPress(Controller.BUTTON_A))
		{
			// Rotate counter-clockwise.
			input |= Controller.BUTTON_BIT_A;
		} 
		else if(deltaRt > 0 && !ctrl.isPress(Controller.BUTTON_B))
		{
			// Rotate clockwise.
			input |= Controller.BUTTON_BIT_B;
		}
		
		//--- Movement
		
		if(deltaX < 0 && !ctrl.isPress(Controller.BUTTON_LEFT))
		{
			// Move left.
			input |= Controller.BUTTON_BIT_LEFT;			
		}
		else if(deltaX > 0 && !ctrl.isPress(Controller.BUTTON_RIGHT))
		{
			// Move right.
			input |= Controller.BUTTON_BIT_RIGHT;
		}
		
		//--- Drop/Lock
		
		if(deltaRt == 0 && deltaX == 0)
		{
			if(y != bestY 
			   && !ctrl.isPress(Controller.BUTTON_UP))
			{
				// Drop
				input |= Controller.BUTTON_BIT_UP;
			}
			else if(y == bestY
					&& !ctrl.isPress(Controller.BUTTON_DOWN))
			{
				targetObtained = true;
				// Lock
				input |= Controller.BUTTON_BIT_DOWN;
			}
		}
		
		return(input);
	}
		
	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		Piece piece = engine.nowPieceObject;
		
		int input = 0;
		
		if(piece != null && engine.stat == Status.MOVE)
		{
			int x = engine.nowPieceX;
			int y = engine.nowPieceY;
			int rt = piece.direction;
			
			//Debug.printStage(engine, x, y, rt, 'Y');
			input = getInput(x, y, rt, ctrl);
		}
		
		ctrl.setButtonBit(input);
	}
	
	/**
	 * Uses the network to score a move 
	 * @param move Move to score
	 * @param engine Game Engine Object
	 * @return the network's score for that move
	 */
	private double scoreMove(GameEngine engine, int x, int y, int rt) {
		// Make copy of field with piece placed
		Field field = new Field(engine.field);
		engine.nowPieceObject.placeToField(x, y, rt, field);

		// Get the stimuli for the network
		double[] stimuli = makeStimuli(engine, field);

		// return the activators response, since it's the score for the move.  
		double[] result = networkActivator.next(stimuli);

		// (We assume only one output node)
		assert(result.length == 1);

		return result[0]; 
	}
	
	/**
	 * Generate the input values for the neural networks.
	 * This is the tricky one! How do we properly structure this so we can rapidly iterate upon this?
	 * @param engine Game Engine object to extract values from
	 * @param field 
	 * @return array of input values. MAKE SURE THE NETWORK IS ABLE TO ACCEPT THIS COUNT OF INPUTS
	 */
	private double[] makeStimuli(GameEngine engine, Field field) {
		return stimulusGenerator.makeStimuli(engine, field);
	}
	
	@Override
	public void newPiece(GameEngine engine, int playerID) {
		
		if(pieceNr > 0)
		{
			int level = engine.statistics.level;
			
			if(targetObtained)
			{
				//System.out.printf("Piece %3d\tLevel %3d\tTarget obtained.\n", pieceNr, level);
			}
			else
			{
				missCount++;
				System.out.printf("Piece %3d\tLevel %3d\tTarget missed!\t%3d misses.\n", pieceNr, level, missCount);
			}
		}
		
		pieceNr++;
		targetObtained = false;
		
		
		Piece pieceNow = engine.nowPieceObject;
		int fromX = engine.nowPieceX;
		int fromY = engine.nowPieceY;
		int fromRt = pieceNow.direction;
		
		
		Field fld = new Field(engine.field);
		
		double bestScore = Double.NEGATIVE_INFINITY;
		
		for(int toRt = 0; toRt < Piece.DIRECTION_COUNT; toRt++) {
			
			int tmpY = fromY;
//			if(engine.statistics.level > 100)
			if(false)
			{
				fld.copy(engine.field);
				tmpY = pieceNow.getBottom(fromX, fromY, toRt, fld);
			}
			
			
			int minX = pieceNow.getMostMovableLeft(fromX, tmpY, toRt, engine.field);
			int maxX = pieceNow.getMostMovableRight(fromX, tmpY, toRt, engine.field);
			
			for(int toX = minX; toX <= maxX; toX++) {
				fld.copy(engine.field);
				int toY = pieceNow.getBottom(toX, fromY, toRt, fld);
				
				//Debug.printStage(engine, toX, toY, toRt, (char)9633);
				
				double score = scoreMove(engine, toX, toY, toRt);
				
				if(score > bestScore)
				{
					bestScore = score;
					bestHold = false;
					bestX = toX;
					bestY = toY;
					bestRt = toRt;
				}
			}
		}
		
	}
}
