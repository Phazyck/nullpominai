package dk.itu.ai;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.newdawn.slick.util.Log;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameEngine.Status;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.mode.GameMode;
import mu.nu.nullpo.game.subsystem.mode.GradeMania3Mode;
import mu.nu.nullpo.gui.slick.LogSystemLog4j;
import mu.nu.nullpo.util.GeneralUtil;

/**
 * A class for running sped-up simulations of tetris games without rendering, akin to the Ms. Pac-Man AI framework.
 * 
 * @author Oliver Phillip
 */
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
			new NeatAI();
		
		
		// Actual simulation.
		Simulator simulator = new Simulator(mode, rulePath, ai);
		
		// Custom seeding.
		
//		String customSeed = 
//			"-2fac0ecd9c988463"
//			"15478945"
//			"897494638"
//			"4697358"
//			;
		
//		simulator.setCustomSeed(customSeed);
		
//		simulator.runSimulation();
		simulator.runStatisticsSimulations(100);
	}
	
	private GameManager gameManager;
	private GameEngine gameEngine;
	private String customSeed = null;
	
	/**
	 * Get the current in-game grade from a game of Tetris The Grand Master 3: Terror Instinct.
	 * 
	 * @return The TGM3 grade.
	 */
	public int getGM3Grade(){
		GradeMania3Mode gm3m = (GradeMania3Mode) gameEngine.owner.mode;
		Field field;
		try {
			field = gm3m.getClass().getDeclaredField("grade");
			field.setAccessible(true);
			return field.getInt(gm3m);
		} catch (NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	/**
	 * Get the current in-game level.
	 * 
	 * @return The level.
	 */
	public int getLevel() {
		return gameEngine.statistics.level;
	}
	
	/**
	 * Make a new Simulator object, ready to go.
	 * 
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
		
//		System.out.println(Long.toString(gameEngine.randSeed, 16));
		if(customSeed != null)
		{
			gameEngine.randSeed = Long.parseLong(customSeed, 16);
			log.debug("Engine seed overwritten with " + customSeed);
		}
		
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

//		log.info("Game is over!");
//		log.info("Final Level: " + gameEngine.statistics.level);
	}
	
	/**
	 * Performs multiple sequential simulations to completion (STATE == GAMEOVER).
	 * 
	 * @param count The number of simulations.
	 */
	public void runSimulations(int count) 
	{
		for(int i = 1; i <= count; ++i)
		{
			log.info(String.format("-------- Simulation %d of %d --------", i, count));
			runSimulation();
		}
	}

	/**
	 * Performs multiple sequential simulations to completion (STATE == GAMEOVER),
	 * and records statistics.
	 * 
	 * @param count The number of simulations.
	 */
	public void runStatisticsSimulations(int count) 
	{
		int gm = 0;
	
		for(int i = 0; i < count; i++)
		{
			log.info(String.format("-------- Simulation %d of %d --------", i+1, count));
			runSimulation();
			if (getGM3Grade() == 32) {
				gm++;
			}
		}
		
		log.info("-------- COMPLETE --------");
		
		log.info("Total:\t" + count);
		log.info("GM grades:\t" + gm);
		
		double prop = (double) gm / (double) count;
		
		log.info("Sample Proportion:\t" + prop);
		
		double confidenceMultiplier = 1.96;
		
		double interval = confidenceMultiplier * Math.sqrt(prop * (1 - prop)/ count );
		
		log.info("Confidence interval:\t" + interval);
	}
	
	/**
	 * Sets a custom random seed for use in simulations. 
	 * 
	 * @param seed The seed.
	 */
	public void setCustomSeed(String seed) 
	{
		customSeed = seed;
	}

	static Logger log = Logger.getLogger(Simulator.class);
	
	/**
	 * Log the current game state.
	 */
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