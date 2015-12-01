package dk.itu.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.ai.BasicAI;

public class BasicNeatAI extends BasicAI {

	/**
	 * Property key for Stimulus generation object
	 *  When changing this object, make sure that the property "stimulus.size"
	 *  in the properties file is set to the corresponding value as well.
	 *  The corresponding Stimulus class should specify this value. 
	 */
	private final static String STIMULUS_CLASS_KEY = "nullpominai.stimulusgenerator";

	StimulusGenerator stimulusGenerator;

	// Values for loading neural networks
	private final static String TRANSCRIBER_CLASS_KEY = "nullpominai.transcriber";
	private final static String CHROMOSONE_ID_KEY = "nullpominai.chromosone.id";

	Properties properties;


	@Override
	public String getName() {
		return "Neaty v0.1A";
	}

	// Neural Network activation object
	private Activator networkActivator;

	/**
	 * This constructor should only be called by the game
	 */
	public BasicNeatAI() {
		this(null);
	}

	/**
	 * This constructor intended for when used as part of fitness evaluation during evolution
	 * @param ac Pre-initialized network activator
	 */
	public BasicNeatAI(Activator ac) {
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

	@Override
	public void newPiece(GameEngine engine, int playerID) {

		// Get the available moves
		List<Move> moves = generatePossibleMoves(engine);

		// Score these moves by using the network activator object
		for (Move move : moves) {
			move.score = scoreMove(move, engine);
		}

		// Find and Execute move with highest score
		Move maxMove = null;
		double maxScore = Double.NEGATIVE_INFINITY;
		for (Move move : moves) {
			if (move.score > maxScore) {
				maxMove = move;
				maxScore = move.score;
			}
		}
		assert(maxMove != null);

		makeMove(maxMove);
	}


	/**
	 * Get a collection of different available moves
	 * @param engine Game Engine object
	 * @return collection of possible moves
	 */
	private List<Move> generatePossibleMoves(GameEngine engine) {
		List<Move> result = new ArrayList<>();

		Piece pieceNow = engine.nowPieceObject;
		int nowX = engine.nowPieceX;
		int nowY = engine.nowPieceY;

		for(int rt = 0; rt < Piece.DIRECTION_COUNT; rt++) {
			int minX = pieceNow.getMostMovableLeft(nowX, nowY, rt, engine.field);
			int maxX = pieceNow.getMostMovableRight(nowX, nowY, rt, engine.field);

			for(int x = minX; x <= maxX; x++) {
				int y = pieceNow.getBottom(x, nowY, rt, engine.field);

				result.add(new Move(x, y, rt));
			}
		}

		return result;
	}


	/**
	 * Uses the network to score a move 
	 * @param move Move to score
	 * @param engine Game Engine Object
	 * @return the network's score for that move
	 */
	private double scoreMove(Move move, GameEngine engine) {
		// Make copy of field with piece placed
		Field field = new Field(engine.field);
		engine.nowPieceObject.placeToField(move.x, move.y, move.rotation, field);

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

	/**
	 * Set fields to let the BasicAI functions navigate to that position
	 * @param move Selected best move object
	 */
	private void makeMove(Move move) {
		bestHold = false;
		bestX = move.x;
		bestY = move.y;
		bestRt = move.rotation;
		bestXSub = move.x;
		bestYSub = move.y;
		bestRtSub = -1;
		bestPts = (int) move.score; // Is this one even neccesary?
	}


	/**
	 * Move representation class
	 * @author Kas
	 */
	class Move
	{
		int x;
		int y;
		int rotation;
		double score;

		public Move(int x, int y, int rotation) {
			super();
			this.x = x;
			this.y = y;
			this.rotation = rotation;
		}

		public void setScore(double score) {
			this.score = score;
		}
	}
}
