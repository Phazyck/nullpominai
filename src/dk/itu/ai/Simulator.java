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
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;
import mu.nu.nullpo.gui.slick.LogSystemLog4j;
import mu.nu.nullpo.util.GeneralUtil;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

public class Simulator {

	public static void main(String[] args) {
		
		PropertyConfigurator.configure("config/etc/log_slick.cfg");
		Log.setLogSystem(new LogSystemLog4j());
		
		GameMode mode = 
			new GradeMania3Mode();
		
		String rulePath = 
			"config\\rule\\Classic3.rul";
		
		DummyAI ai = 
			new BasicAI();
		
		Simulator simulator = new Simulator(mode, rulePath, ai);
		
		simulator.runSimulations(5);
	}
	
	private GameEngine engine;

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
		GameManager gameManager = new GameManager();
		
		gameManager.mode = mode;
		
		gameManager.init();
		
		engine = gameManager.engine[0]; 
		
		// Rules
		engine.ruleopt = rules;
		
		// Randomizer
		Randomizer randomizerObject = GeneralUtil.loadRandomizer(rules.strRandomizer);
		engine.randomizer = randomizerObject;

		// Wallkick	
		Wallkick wallkickObject = GeneralUtil.loadWallkick(rules.strWallkick);
		engine.wallkick = wallkickObject;
		
		// AI
		engine.ai = ai;
		engine.aiMoveDelay = 0;
		engine.aiThinkDelay = 0;
		engine.aiUseThread = false;
		engine.aiShowHint = false;
		engine.aiPrethink = false;
		engine.aiShowState = false;
		
		gameManager.showInput = false;
	}

	/**
	 * Performs a single simulation to completion (STATE == GAMEOVER)
	 */
	public void runSimulation() 
	{
		// Called at initialization
		engine.init();
		
		// Have to spend at least 5 frames in the menu before you can start the game
		for(int i = 0; i < 5; ++i)
		{
			engine.update();
		}
		engine.ctrl.setButtonPressed(Controller.BUTTON_A); // Press A to start game
		engine.update(); 
		engine.ctrl.setButtonUnpressed(Controller.BUTTON_A);
		
		while (engine.stat != Status.GAMEOVER) {
			engine.update();
//			logGameState();
		}

		log.info("Game is over!");
		log.info("Final Level: " + engine.statistics.level);
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
		int level = engine.statistics.level;
		
		String piece = 
			engine.nowPieceObject == null 
				? " " 
				: Piece.PIECE_NAMES[engine.nowPieceObject.id];
		
		String state = engine.stat.toString();
		
		log.info(String.format("\tLevel: %3d \tPiece: %s \tState: %s", level, piece, state));
	}
}