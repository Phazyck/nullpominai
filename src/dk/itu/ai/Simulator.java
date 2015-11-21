package dk.itu.ai;

import java.io.FileInputStream;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.event.EventReceiver;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameEngine.Status;
import mu.nu.nullpo.game.play.GameManager;
import mu.nu.nullpo.game.subsystem.ai.BasicAI;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.mode.GameMode;
import mu.nu.nullpo.game.subsystem.mode.GradeMania3Mode;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;

public class Simulator {
	
	static String filename = "config\\rule\\Classic3.rul";
	
	public static void main(String[] args) {
		
		GameEngine gEngine = makeNewGame(
				new GradeMania3Mode(), 
				filename, 
				new BasicAI());
		
		runGame(gEngine);
		
	}
	
	/**
	 * Takes a game engine and runs it to completion (STATE == GAMEOVER)
	 * @param gameEngine A freshly instantiated GameEngine object in the default beginning SETTING state.
	 */
	public static void runGame(GameEngine gameEngine) {
		// Have to spend at least 5 frames in the menu before you can start the game
		gameEngine.update();
		gameEngine.update();
		gameEngine.update();
		gameEngine.update();
		gameEngine.update();
		gameEngine.ctrl.setButtonPressed(Controller.BUTTON_A); // Press A to start game
		gameEngine.update(); 
		gameEngine.ctrl.setButtonUnpressed(Controller.BUTTON_A);
		
		while (gameEngine.stat != Status.GAMEOVER) {
			gameEngine.update();
//			System.out.printf("State: %s\tLevel: %d\tCurrent Piece: %s  %n", 
//					gameEngine.stat, 
//					gameEngine.statistics.level, 
//					(gameEngine.nowPieceObject != null ? gameEngine.nowPieceObject.toString() : "NONE"));
		}
		
		System.out.println("Game is over!");
		System.out.println("Final Level: " + gameEngine.statistics.level);
	}
	
	
	// Stolen from StateInGame:134
	
	/**
	 * Make a new GameEngine object, ready to go
	 * @param modeObj Game mode Object
	 * @param strRulePath path string to the rules file
	 * @param aiObj AI object to play (MAKE SURE IT SUPPORTS UNTHREADED !!! )
	 * @return A freshly initiated GameEngine object, in the default SETTING state.
	 */
	public static GameEngine makeNewGame(GameMode modeObj, String strRulePath, DummyAI aiObj) {
		GameManager gameManager = new GameManager(new EventReceiver());

		gameManager.mode = modeObj;
		
		gameManager.init();
		
		/* Shit stolen from GeneralUtil.java:265 */ 
		CustomProperties prop = new CustomProperties();

		try {
			FileInputStream in = new FileInputStream(strRulePath);
			prop.load(in);
			in.close();
		} catch (Exception e) {
			System.out.println("Failed to load rule from " + strRulePath);
		}

		RuleOptions ruleopt = new RuleOptions();
		ruleopt.readProperty(prop, 0);
		/*----------------------------------------*/
		
		GameEngine engine = gameManager.engine[0]; 
		
		
		// Rules
		engine.ruleopt = ruleopt;
		
		// Randomizer
		Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer);
		engine.randomizer = randomizerObject;

		// Wallkick	
		Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
		engine.wallkick = wallkickObject;
		
		// AI
		engine.ai = aiObj;
		engine.aiMoveDelay = 0;
		engine.aiThinkDelay = 0;
		engine.aiUseThread = false;
		engine.aiShowHint = false;
		engine.aiPrethink = false;
		engine.aiShowState = false;
		
		
		gameManager.showInput = false;

		// Called at initialization
		engine.init();

		return engine;
	}
}
