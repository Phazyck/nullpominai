package dk.itu.ai;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

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
import dk.itu.ai.navigation.Move;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.WallkickResult;
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


	Stack<Move> inputMoves;
	
	@Override
	public String getName() {
		return "Neaty v0.2";
	}

	// Neural Network activation object
	private Activator networkActivator;

	private String moveStackString;

	private Move nextMove;

	private int lastInput;

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

	/*
	
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
	
	boolean AREProcessed;
	int AREinput;
		
	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		Piece piece = engine.nowPieceObject;
		
		int input = 0;
		
		// First frame in ARE
		if(!AREProcessed && engine.stat == Status.ARE) {
			AREProcessed = true;
			
			Piece nextPiece = engine.getNextObject(engine.nextPieceCount);
			
			Motion AREmotion = motions.pop();
			
			int x = engine.getSpawnPosX(engine.field, nextPiece);
			int y = engine.getSpawnPosY(nextPiece);
			int rt = nextPiece.direction;
			
			bestX =  x + AREmotion.x;
			bestRt = rt + AREmotion.rotate;
			bestY =  -10; // Don't let the getInput logic lock the piece 
			// nextPiece.getBottom(x, y, rt, engine.field);
			
			AREinput = getInput(x, y, rt, ctrl);
			
//			System.out.println("Next piece name: " + Piece.PIECE_NAMES[nextPiece.id]);
//			
//			// bestX = motions.peek()
////			System.out.println(engine.nowPieceX);
//			System.out.println(engine.getSpawnPosX(engine.field, nextPiece));
//			System.out.println(engine.getSpawnPosY(nextPiece));
		}
		// Repeat ARE
		else if (engine.stat == Status.ARE) {
			input = AREinput;
		}
		
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
	
	*/
		
	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		Piece piece = engine.nowPieceObject;
		
		int input = 0;
		
		if(piece != null && engine.stat == Status.MOVE)
		{			
			int x = engine.nowPieceX;
			int y = engine.nowPieceY;
			int rt = piece.direction;
			
			boolean lockAllowed = false;
			
			// If we reach current target position, pop next one.
			// If no more target positions, we lock the piece
			if (x == bestX &&
				y == bestY &&
				rt == bestRt) {
				if (!inputMoves.isEmpty()) {
					nextDestination();
				}
				else lockAllowed = true;
			}
			
			
			
			//Debug.printStage(engine, x, y, rt, 'Y');
			input = getInput(x, y, rt, lockAllowed, ctrl);
		}
		
		ctrl.setButtonBit(input);
	}
	
	private int getInput(int x, int y, int rt, boolean lock, Controller ctrl)
	{
		int input = 0;
				
		if (lock)
		{
			targetObtained = true;
			input |= Controller.BUTTON_BIT_DOWN;
		}
		else if(lastInput != Controller.BUTTON_BIT_UP)
		{
			// Always press up, emulate 20G
			input = Controller.BUTTON_BIT_UP;
		}
		else
		{
			switch (nextMove.dx) {
				case Move.LEFT:
					input |= Controller.BUTTON_BIT_LEFT;
					break;
				case Move.RIGHT:
					input |= Controller.BUTTON_BIT_RIGHT;
					break;
				case Move.NONE:
					break;
				default:
					assert(false);
					break;
			}
			switch (nextMove.drt) {
				case Move.CCW:
					input |= Controller.BUTTON_BIT_A;
					break;
				case Move.CW:
					input |= Controller.BUTTON_BIT_B;
					break;
				case Move.NONE:
					break;
				default:
					assert(false);
					break;
			}	
		}
		
		lastInput = input;
		
		return(input);
	}
	
	private int getInputOld(int x, int y, int rt, boolean lock, Controller ctrl)
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
					&& !ctrl.isPress(Controller.BUTTON_DOWN)
					&& lock)
			{ 
				targetObtained = true;
				// Lock
				input |= Controller.BUTTON_BIT_DOWN;
			}
		}
		
		return(input);
	}
	
	
	
	/**
	 * Uses the network to score a move 
	 * @param move Move to score
	 * @param engine Game Engine Object
	 * @return the network's score for that move
	 */
	private double scoreMove(Move move, GameEngine engine) {
		// Make copy of field with piece placed
		Field oldField = new Field(engine.field);
		Field newField = new Field(engine.field);
		engine.nowPieceObject.placeToField(move.x, move.y, move.rotation, newField);

		// Get the stimuli for the network
		double[] stimuli = makeStimuli(engine, newField, oldField);

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
	private double[] makeStimuli(GameEngine engine, Field field, Field oldField) {
		return stimulusGenerator.makeStimuli(engine, field, oldField);
	}
	
	@Override
	public void newPiece(GameEngine engine, int playerID) {
		
		double bestScore = Double.NEGATIVE_INFINITY;
		Move bestMove = null;
		
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
//				String pieceName = Piece.getPieceName(engine.nextPieceArrayObject[engine.nextPieceCount-2].id);
//				System.out.println(pieceName);
				System.out.println(moveStackString);
				System.out.println();
			}
		}
		
		pieceNr++;
		targetObtained = false;
		
		
		
		Collection<Move> moves = generatePossibleMoves(engine);
		
		for (Move move : moves) {
			double score = scoreMove(move, engine);
			
			if(score > bestScore || bestMove == null)
			{
				// TODO get rid of this
				bestScore = score;
				bestHold = false;
				bestMove = move;
			}
		}

		inputMoves = makeMoveStack(bestMove);
		moveStackString = moveStackToString(inputMoves);
			
		// Set up first destination for navigation
		nextDestination();
		
		
		// debug stuff
//		if (currentMotion != null) {
//			System.out.println("Move chosen: " + currentMotion.toString());
//			System.out.println("\tX: " + bestX + "\tY: " + bestY + "\tbestRt: " + bestRt);
//		}
//		else System.out.println("Move chosen: ");
	}

	private void nextDestination() {
		assert(!inputMoves.isEmpty());
		
//		System.out.println(moveStackToString(inputMoves));
		
		nextMove = inputMoves.pop();
		
		bestX = nextMove.x;
		bestY = nextMove.y;
		bestRt = nextMove.rotation;
	}
	
	// TODO somehow verify this generates correct set of moves every time	
	// Figured out this doesn't account for floorkicks
	/**
	 * Get a collection of different available moves
	 * @param engine Game Engine object
	 * @return collection of possible moves
	 */
	private Set<Move> generatePossibleMoves(GameEngine engine) {
		Set<Move> moves = new HashSet<>();
		Queue<Move> exploreQueue = new LinkedList<>();

		Piece nowPiece = engine.nowPieceObject;
		int nowX = engine.nowPieceX;
		int nowY = engine.nowPieceY;
		int nowRt = nowPiece.direction;

		// Root move -> the idle move, do no input and just let it fall
		Move root = new Move(
				nowX, 
				nowPiece.getBottom(nowX, nowY, engine.field), 
				nowRt, 
				0,
				0,
				0,
				null);
		
		moves.add(root);
		exploreQueue.add(root);
		
		// Keep exploring until all posibilities exhausted
		while (!exploreQueue.isEmpty()) {
			// Expand next in queue
			Collection<Move> newNeighbours = getMoveUnexploredNeighbours(
					engine,  
					exploreQueue.remove(), 
					moves);
			
			// Add new neighbours to queue and result set
			moves.addAll(newNeighbours);
			exploreQueue.addAll(newNeighbours);
		}

		return moves;
	}
	
	// TODO better collection than set for this?
	private Set<Move> getMoveUnexploredNeighbours(GameEngine engine, Move prevMove, Set<Move> exploredMoves) {
		
		Field field = engine.field;
		Piece piece = engine.nowPieceObject;
		
		Set<Move> result = new HashSet<>();
		
		int oldX = prevMove.x;
		int oldY = prevMove.y;
		int oldRt = prevMove.rotation;
		int oldKicks = prevMove.floorKicksPerformed;
		
		// For each combination of rotation and x movement
		// TODO skip rt=0,x=0 entirely 
		for (int drt : Move.ROTATIONS) {
			for (int dx : Move.TRANSLATIONS) {
				
				if (drt == 0 && dx == 0) continue;
				
				int newRt = oldRt;
				int newX = oldX;
				int newY = oldY;
				int newKicks = oldKicks;
				
				if(drt != 0)
				{
					// Apply rotation
					newRt = piece.getRotateDirection(drt, oldRt);
					
					
					// If the piece can't be rotated, try kicks
					if (piece.checkCollision(oldX, oldY, newRt, field)) 
					{
						if((engine.wallkick != null) && (engine.ruleopt.rotateWallkick)) 
						{
							boolean allowUpward = (engine.ruleopt.rotateMaxUpwardWallkick < 0) ||
												  (oldKicks < engine.ruleopt.rotateMaxUpwardWallkick);
							WallkickResult kick = engine.wallkick.executeWallkick(oldX, oldY, drt, oldRt, newRt,
												  allowUpward, piece, field, null);
							
							if(kick != null) 
							{
//								System.out.println("\n" + pieceNr);
//								System.out.printf("Kick (%d,%d)\n", kick.offsetX, kick.offsetY);

								newX += kick.offsetX;
								newY += kick.offsetY;
								if(kick.isUpward())
								{
									++newKicks;
								}
							}
							else
							{
								continue;
							}
						}
					}
				}
				
				if(dx != 0)
				{
					// Apply movement
					
					newX += dx;
					if(piece.checkCollision(newX, newY, newRt, field))
					{
						continue;
					}
				}
				
				// Simulate 20G
				newY = piece.getBottom(newX, newY, newRt, field);
				
				// Make new move
				Move newMove = new Move(
						newX,
						newY,
						newRt, 
						dx,
						drt,
						newKicks,
						prevMove);
				
				// If piece already explored earlier, ignore
				if (exploredMoves.contains(newMove)) {
					continue;
				}
				
				// This is a new neighbour, add!
				result.add(newMove);
				
				// assume new move is always valid (never collides)
				assert(!piece.checkCollision(newMove.x, newMove.y, newMove.rotation, field));
			}
		}
		
		return result;
	}
	
	private Stack<Move> makeMoveStack(Move move) {
		Stack<Move> result = new Stack<>();

		Move current = move;
		while (current != null) {
			result.push(current);
			current = current.parent;
		}
		
		return result;
	}
	
	private String moveStackToString(Stack<Move> moves) {
		/* StringBuilder sb = new StringBuilder();
		
		if (move.parent != null) {
			sb.append(moveStackToString(move.parent));
		}
		
		sb.append("\t -> ");

		sb.append(move.x + ", " + move.y + ", " + move.rotation);
		
		return sb.toString();
		*/
		
		StringBuilder sb = new StringBuilder();
		
		for (Move move : moves) {
			sb.append("\t <- ");

			sb.append(move.x + ", " + move.y + ", " + move.rotation);
		}
		
		return sb.toString();
	}
	
	
}
