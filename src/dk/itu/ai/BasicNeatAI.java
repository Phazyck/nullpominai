package dk.itu.ai;

import java.io.FileInputStream;
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

import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.ai.BasicAI;

public class BasicNeatAI extends BasicAI {

	// Values for loading neural networks
	private final static String TRANSCRIBER_CLASS_KEY = "nullpominai.transcriber";
	private final static String CHROMOSONE_ID = "20603";

	@Override
	public String getName() {
		return "NeatAI";
	}
	
	
	// Neural Network activation object
	private Activator networkActivator;
	
	/**
	 * This constructor should only be called by the game
	 */
	public BasicNeatAI() {
		this(null);
	}
	
	public BasicNeatAI(Activator ac) {
		networkActivator = ac;
	}
	
	/**
	 * This is for enabling playing with a specific chromosone 
	 */
	@Override
	public void init(GameEngine engine, int playerID) {
		super.init(engine, playerID);

		// Load and initialize a preexisting network, if no network was supplied
		if (networkActivator == null) {
			Properties props;
			try {
				props = new Properties( "nullpomino.properties" );
				
				NeatConfiguration config = new NeatConfiguration( props );
				
				Persistence persistence = new FilePersistence();
				persistence.init(props);
				
				Chromosome chromosome = persistence.loadChromosome(CHROMOSONE_ID, config);
				
				ActivatorTranscriber transcriber = (ActivatorTranscriber) props.newObjectProperty( TRANSCRIBER_CLASS_KEY );
						
				networkActivator = transcriber.newActivator( chromosome );
				
			} catch (IOException | InvalidConfigurationException | TranscriberException e) {
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
		/**
		 * Current plan, 11 inputs:
		 * 0-9: Contour of block field surface. Top row of contour is Y=0.
		 * 10: Absolute block field height of top row of contour. 
		 */
		
		
		final int INPUT_COUNT = 11;
		
		double[] result = new double[INPUT_COUNT];
		
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
