package dk.itu.ai;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.newdawn.slick.util.Log;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameEngine.Status;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.ai.BasicAI;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.mode.GameMode;
import mu.nu.nullpo.game.subsystem.mode.GradeMania3Mode;
import mu.nu.nullpo.gui.slick.LogSystemLog4j;
import mu.nu.nullpo.util.GeneralUtil;

public class Simulator {

	public static void main(String[] args) {
		
		// Logger initialization.
		PropertyConfigurator.configure("config/etc/log_slick.cfg");
		Log.setLogSystem(new LogSystemLog4j());
		
		
		// NOTE(oliver): For other GameModes, look inside src/mu/nu/nullpo/game/subsystem/mode
		GameMode mode = 
			new GradeMania3Mode();
		
		
		// NOTE(oliver): For other rules, look inside config/rule.
		String rulePath = 
			"config\\rule\\Classic3.rul";
		
		
		// NOTE(oliver): For other AIs, look inside src/mu/nu/nullpo/game/subsystem/ai, or src/dk/itu/ai
		DummyAI ai = 
			new BasicAI();
		
		
		// Actual simulation.
		Simulator simulator = new Simulator(mode, rulePath, ai);
		
		simulator.runSimulations(5);
		
	}
	
	private GameManager gameManager;
	private GameEngine gameEngine;

	/**
	 * Make a new Simulator object, ready to go.
	 * @param mode Game mode Object
	 * @param rulePath path string to the rules file
	 * @param ai AI object to play (MAKE SURE IT SUPPORTS UNTHREADED !!! )
	 */
	public Simulator(GameMode mode, String rulePath, DummyAI ai)
	{
		this(mode, GeneralUtil.loadRule(rulePath), ai);
	}
	
	/**
	 * Make a new Simulator object, ready to go.
	 * @param mode Game mode Object
	 * @param rules Game rules Object
	 * @param ai AI object to play (MAKE SURE IT SUPPORTS UNTHREADED !!! )
	 */
	public Simulator(GameMode mode, RuleOptions rules, DummyAI ai)
	{
		/* 
		 * NOTE(oliver): This code is a domain-specific version of 
		 * mu.nu.nullpo.gui.slick.StateInGame.startNewGame(String strRulePath)
		 */
		
		// Manager setup
		gameManager = new GameManager();
		
		gameManager.mode = mode;
		
		gameManager.init();
		
		gameManager.showInput = false;
		
		// Engine setup
		gameEngine = gameManager.engine[0]; 
		
		// - Rules
		gameEngine.ruleopt = rules;
		
		// - Randomizer
		gameEngine.randomizer = GeneralUtil.loadRandomizer(rules.strRandomizer);

		// - Wallkick	
		gameEngine.wallkick = GeneralUtil.loadWallkick(rules.strWallkick);
		
		// - AI
		gameEngine.ai = ai;
		gameEngine.aiMoveDelay = 0;
		gameEngine.aiThinkDelay = 0;
		gameEngine.aiUseThread = false;
		gameEngine.aiShowHint = false;
		gameEngine.aiPrethink = false;
		gameEngine.aiShowState = false;
	}

	/**
	 * Performs a single simulation to completion (STATE == GAMEOVER)
	 */
	public void runSimulation() 
	{
		// Start a new game.
		gameEngine.init();
		
		// You have to spend at least 5 frames in the menu before you can start the game.
		for(int i = 0; i < 5; ++i)
		{
			gameEngine.update();
		}
		
		// Press and release A to start game.
		gameEngine.ctrl.setButtonPressed(Controller.BUTTON_A); 
		gameEngine.update(); 
		gameEngine.ctrl.setButtonUnpressed(Controller.BUTTON_A);
		
		// Run the game until Game Over.
		while (gameEngine.stat != Status.GAMEOVER) {
			gameEngine.update();
//			logGameState();
		}

		log.info("Game is over!");
		log.info("Final Level: " + gameEngine.statistics.level);
	}
	
	/**
	 * Performs multiple sequential simulations to completion (STATE == GAMEOVER)
	 */
	public void runSimulations(int count) 
	{
		for(int i = 1; i <= count; ++i)
		{
			log.info(String.format("-------- Simulation %d of %d --------", i, count));
			runSimulation();
		}
	}

	static Logger log = Logger.getLogger(Simulator.class);
	
	private void logGameState()
	{
		int level = gameEngine.statistics.level;
		
		String piece = 
			gameEngine.nowPieceObject == null 
				? " " 
				: Piece.PIECE_NAMES[gameEngine.nowPieceObject.id];
		
		String state = gameEngine.stat.toString();
		
		log.info(String.format("\tLevel: %3d \tPiece: %s \tState: %s", level, piece, state));
	}
}