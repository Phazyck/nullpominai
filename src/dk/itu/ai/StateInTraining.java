package dk.itu.ai;

import org.apache.log4j.Logger;

import mu.nu.nullpo.game.component.RuleOptions;
import mu.nu.nullpo.game.play.GameEngine.Status;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;
import mu.nu.nullpo.game.subsystem.mode.GameMode;
import mu.nu.nullpo.game.subsystem.wallkick.Wallkick;
import mu.nu.nullpo.gui.slick.*;
import mu.nu.nullpo.util.CustomProperties;
import mu.nu.nullpo.util.GeneralUtil;
import net.omegaboshi.nullpomino.game.subsystem.randomizer.Randomizer;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 * Training game screen state (AI play)
 * 
 * prop.getProperty("option.maxfps", 60);
 * prop.setProperty("option.maxfps", maxfps);
 */
public class StateInTraining extends BasicGameState {
	
	public static final int TRAINING_ROUNDS = 10;
	
	/** This state's ID */
	public static final int ID = 421;

	/** Game main class */
	public TrainingManager gameManager = null;

	/** Log */
	static Logger log = Logger.getLogger(StateInTraining.class);

	/** Game paused flag */
	protected boolean pause = false;

	/** Hide pause menu */
	protected boolean pauseMessageHide = false;

	/** Frame step enabled flag */
	protected boolean enableframestep = false;

	/** Show background flag */
	protected boolean showbg = true;

	/** Fast forward */
	protected int fastforward = 0;

	/** Pause menu cursor position */
	protected int cursor = 0;

	/** Number of frames remaining until pause key can be used */
	protected int pauseFrame = 0;

	/** Screenshot flag */
	protected boolean ssflag = false;

	/** AppGameContainer (Used by title-bar text change) */
	protected AppGameContainer appContainer = null;

	/** Previous ingame flag (Used by title-bar text change) */
	protected boolean prevInGameFlag = false;

	/** Current game mode name */
	protected String modeName;

	/*
	 * Fetch this state's ID
	 */
	@Override
	public int getID() {
		return ID;
	}

	/*
	 * State initialization
	 */
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		appContainer = (AppGameContainer) container;
	}

	/*
	 * Called when entering this state
	 */
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		log.debug("LOG(oliver): Entering training game!");
		enableframestep = NullpoMinoSlick.propConfig.getProperty("option.enableframestep", false);
		container.setTargetFrameRate(Integer.MAX_VALUE);
		showbg = NullpoMinoSlick.propConfig.getProperty("option.showbg", true);
		fastforward = 0;
		cursor = 0;
		
		// NOTE(oliver): Removing FPS cap for training games.
		NullpoMinoSlick.altMaxFPS = 6000;
		
		prevInGameFlag = false;
		
		// NOTE(oliver): Initializing the amount of automatic retries.
		trainingRound = 0;

		container.setClearEachFrame(!showbg); // Clear each frame when there is no BG

	}

	/**
	 * Start a new game (Rule will be user-selected one)
	 */
	public void startNewGame() {
		startNewGame(null);
	}

	/**
	 * Start a new game
	 * 
	 * @param strRulePath
	 *            Rule file path (null if you want to use user-selected one)
	 */
	public void startNewGame(String strRulePath) {
		gameManager = new TrainingManager(new RendererSlick());
		pause = false;

		gameManager.receiver.setGraphics(appContainer.getGraphics());

		modeName = NullpoMinoSlick.propGlobal.getProperty("name.mode", "");
		GameMode modeObj = NullpoMinoSlick.modeManager.getMode(modeName);
		if (modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
		} else {
			gameManager.mode = modeObj;
		}

		gameManager.init();

		// Initialization for each player
		for (int i = 0; i < gameManager.getPlayers(); i++) {
			// チューニング設定
			gameManager.engine[i].owRotateButtonDefaultRight = NullpoMinoSlick.propGlobal
					.getProperty(i + ".tuning.owRotateButtonDefaultRight", -1);
			gameManager.engine[i].owSkin = NullpoMinoSlick.propGlobal.getProperty(i + ".tuning.owSkin", -1);
			gameManager.engine[i].owMinDAS = NullpoMinoSlick.propGlobal.getProperty(i + ".tuning.owMinDAS", -1);
			gameManager.engine[i].owMaxDAS = NullpoMinoSlick.propGlobal.getProperty(i + ".tuning.owMaxDAS", -1);
			gameManager.engine[i].owDasDelay = NullpoMinoSlick.propGlobal.getProperty(i + ".tuning.owDasDelay", -1);
			gameManager.engine[i].owReverseUpDown = NullpoMinoSlick.propGlobal
					.getProperty(i + ".tuning.owReverseUpDown", false);
			gameManager.engine[i].owMoveDiagonal = NullpoMinoSlick.propGlobal.getProperty(i + ".tuning.owMoveDiagonal",
					-1);
			gameManager.engine[i].owBlockOutlineType = NullpoMinoSlick.propGlobal
					.getProperty(i + ".tuning.owBlockOutlineType", -1);
			gameManager.engine[i].owBlockShowOutlineOnly = NullpoMinoSlick.propGlobal
					.getProperty(i + ".tuning.owBlockShowOutlineOnly", -1);

			// ルール
			RuleOptions ruleopt = null;
			String rulename = strRulePath;
			if (rulename == null) {
				rulename = NullpoMinoSlick.propGlobal.getProperty(i + ".rule", "");
				if (gameManager.mode.getGameStyle() > 0) {
					rulename = NullpoMinoSlick.propGlobal.getProperty(i + ".rule." + gameManager.mode.getGameStyle(),
							"");
				}
			}
			if ((rulename != null) && (rulename.length() > 0)) {
				log.info("Load rule options from " + rulename);
				ruleopt = GeneralUtil.loadRule(rulename);
			} else {
				log.info("Load rule options from setting file");
				ruleopt = new RuleOptions();
				ruleopt.readProperty(NullpoMinoSlick.propGlobal, i);
			}
			gameManager.engine[i].ruleopt = ruleopt;

			// NEXT順生成アルゴリズム
			if ((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer);
				gameManager.engine[i].randomizer = randomizerObject;
			}

			// Wallkick
			if ((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[i].wallkick = wallkickObject;
			}

			// AI
			String aiName = NullpoMinoSlick.propGlobal.getProperty(i + ".ai", "");
			if (aiName.length() > 0) {
				DummyAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[i].ai = aiObj;
				gameManager.engine[i].aiMoveDelay = NullpoMinoSlick.propGlobal.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engine[i].aiThinkDelay = NullpoMinoSlick.propGlobal.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engine[i].aiUseThread = NullpoMinoSlick.propGlobal.getProperty(i + ".aiUseThread", true);
				gameManager.engine[i].aiShowHint = NullpoMinoSlick.propGlobal.getProperty(i + ".aiShowHint", false);
				gameManager.engine[i].aiPrethink = NullpoMinoSlick.propGlobal.getProperty(i + ".aiPrethink", false);
				gameManager.engine[i].aiShowState = NullpoMinoSlick.propGlobal.getProperty(i + ".aiShowState", false);
			}
			gameManager.showInput = NullpoMinoSlick.propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engine[i].init();
		}

		updateTitleBarCaption();
	}

	/**
	 * Start Replay game
	 * 
	 * @param prop
	 *            CustomProperties with replay data in it
	 */
	public void startReplayGame(CustomProperties prop) {
		gameManager = new TrainingManager(new RendererSlick());
		gameManager.replayMode = true;
		gameManager.replayProp = prop;
		pause = false;

		gameManager.receiver.setGraphics(appContainer.getGraphics());

		// Mode
		modeName = prop.getProperty("name.mode", "");
		GameMode modeObj = NullpoMinoSlick.modeManager.getMode(modeName);
		if (modeObj == null) {
			log.error("Couldn't find mode:" + modeName);
		} else {
			gameManager.mode = modeObj;
		}

		gameManager.init();

		// Initialization for each player
		for (int i = 0; i < gameManager.getPlayers(); i++) {
			// ルール
			RuleOptions ruleopt = new RuleOptions();
			ruleopt.readProperty(prop, i);
			gameManager.engine[i].ruleopt = ruleopt;

			// NEXT順生成アルゴリズム
			if ((ruleopt.strRandomizer != null) && (ruleopt.strRandomizer.length() > 0)) {
				Randomizer randomizerObject = GeneralUtil.loadRandomizer(ruleopt.strRandomizer);
				gameManager.engine[i].randomizer = randomizerObject;
			}

			// Wallkick
			if ((ruleopt.strWallkick != null) && (ruleopt.strWallkick.length() > 0)) {
				Wallkick wallkickObject = GeneralUtil.loadWallkick(ruleopt.strWallkick);
				gameManager.engine[i].wallkick = wallkickObject;
			}

			// AI (リプレイ追記用）
			String aiName = NullpoMinoSlick.propGlobal.getProperty(i + ".ai", "");
			if (aiName.length() > 0) {
				DummyAI aiObj = GeneralUtil.loadAIPlayer(aiName);
				gameManager.engine[i].ai = aiObj;
				gameManager.engine[i].aiMoveDelay = NullpoMinoSlick.propGlobal.getProperty(i + ".aiMoveDelay", 0);
				gameManager.engine[i].aiThinkDelay = NullpoMinoSlick.propGlobal.getProperty(i + ".aiThinkDelay", 0);
				gameManager.engine[i].aiUseThread = NullpoMinoSlick.propGlobal.getProperty(i + ".aiUseThread", true);
				gameManager.engine[i].aiShowHint = NullpoMinoSlick.propGlobal.getProperty(i + ".aiShowHint", false);
				gameManager.engine[i].aiPrethink = NullpoMinoSlick.propGlobal.getProperty(i + ".aiPrethink", false);
				gameManager.engine[i].aiShowState = NullpoMinoSlick.propGlobal.getProperty(i + ".aiShowState", false);
			}
			gameManager.showInput = NullpoMinoSlick.propConfig.getProperty("option.showInput", false);

			// Called at initialization
			gameManager.engine[i].init();
		}

		updateTitleBarCaption();
	}

	/**
	 * Update title bar text
	 */
	public void updateTitleBarCaption() {
		
		String strTitle = "NullpoMino - " + modeName;

		if ((gameManager != null) && (gameManager.engine != null) && (gameManager.engine.length > 0)
				&& (gameManager.engine[0] != null)) {
			if (pause && !enableframestep)
				strTitle = "[PAUSE] NullpoMino - " + modeName;
			else if (gameManager.engine[0].isInGame && !gameManager.replayMode && !gameManager.replayRerecord)
				strTitle = "[PLAY] NullpoMino - " + modeName;
			else if (gameManager.replayMode && gameManager.replayRerecord)
				strTitle = "[RERECORD] NullpoMino - " + modeName;
			else if (gameManager.replayMode && !gameManager.replayRerecord)
				strTitle = "[REPLAY] NullpoMino - " + modeName;
			else
				strTitle = "[MENU] NullpoMino - " + modeName;
		}
		
		int gameNr = 1 + trainingRound;
		strTitle += String.format(" [game %d of %d]", gameNr, TRAINING_ROUNDS);
		strTitle += " fps: " + ((int)NullpoMinoSlick.actualFPS);

		appContainer.setTitle(strTitle);
	}

	/**
	 * Shutdown routine
	 */
	public void shutdown() {
		gameManager.shutdown();
		gameManager = null;
		ResourceHolderSlick.bgmUnloadAll();
	}

	/*
	 * Called when leaving this state
	 */
	@Override
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
		container.setClearEachFrame(false);
		shutdown();
	}

	/*
	 * Draw the screen
	 */
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		
		if (!container.hasFocus()) {
			if (!NullpoMinoSlick.alternateFPSTiming)
				NullpoMinoSlick.alternateFPSSleep(true);
			return;
		}

		
		// Game screen
		if (gameManager != null) {
			
			gameManager.renderAll(pause);
			
			if ((gameManager.engine.length > 0) && (gameManager.engine[0] != null)) {
				int offsetX = gameManager.receiver.getFieldDisplayPositionX(gameManager.engine[0], 0);
				int offsetY = gameManager.receiver.getFieldDisplayPositionY(gameManager.engine[0], 0);

				// Pause menu
				if (pause && !enableframestep && !pauseMessageHide) {
					NormalFontSlick.printFont(offsetX + 12, offsetY + 188 + (cursor * 16), "b",
							NormalFontSlick.COLOR_RED);

					NormalFontSlick.printFont(offsetX + 28, offsetY + 188, "CONTINUE", (cursor == 0));
					NormalFontSlick.printFont(offsetX + 28, offsetY + 204, "RETRY", (cursor == 1));
					NormalFontSlick.printFont(offsetX + 28, offsetY + 220, "END", (cursor == 2));
					if (gameManager.replayMode && !gameManager.replayRerecord)
						NormalFontSlick.printFont(offsetX + 28, offsetY + 236, "RERECORD", (cursor == 3));
				}

				// Fast forward
				if (fastforward != 0)
					NormalFontSlick.printFont(offsetX, offsetY + 376, "e" + (fastforward + 1),
							NormalFontSlick.COLOR_ORANGE);
				if (gameManager.replayShowInvisible)
					NormalFontSlick.printFont(offsetX, offsetY + 392, "SHOW INVIS", NormalFontSlick.COLOR_ORANGE);
			}
		}
		
		// FPS
		NullpoMinoSlick.drawFPS(container, true);
		// Observer
		NullpoMinoSlick.drawObserverClient();
		// Screenshot
		if (ssflag) {
			NullpoMinoSlick.saveScreenShot(container, g);
			ssflag = false;
		}

		if (!NullpoMinoSlick.alternateFPSTiming)
			NullpoMinoSlick.alternateFPSSleep(true);
	}

	private int trainingRound;
	
	
	private void doRetry()
	{
		if(trainingRound >= TRAINING_ROUNDS)
		{
			System.exit(0);
		}
		
		if(gameManager.engine[0].stat == Status.SETTING)
		{
			// NOTE(oliver): YOLOL 
			// PRESS [F] TO PAY RESPECTS
			// PRESS [HAXORZ] TO SPAM A WITHOUT HOLDING THE BUTTON HUEHUEHUEHUEHUE
			GameKeySlick.gamekey[0].setInputState(GameKeySlick.BUTTON_A,  ((int) System.currentTimeMillis()) % 2);
			return;
		}
		
		if(gameManager.engine[0].stat != Status.GAMEOVER)
		{
			return;
		}
		
		
		
		trainingRound++;
		GameKeySlick.gamekey[0].setInputState(GameKeySlick.BUTTON_RETRY, 1);
	}
	
	/*
	 * ゲーム stateを更新
	 */
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		
		updateTitleBarCaption();
		
		if (!container.hasFocus()) {
			GameKeySlick.gamekey[0].clear();
			GameKeySlick.gamekey[1].clear();
			if (NullpoMinoSlick.alternateFPSTiming)
				NullpoMinoSlick.alternateFPSSleep();
			
			// NOTE(oliver): By disabling this, the AI can run in the BG!!!
			//return;
		}

		// TTF font
		if (ResourceHolderSlick.ttfFont != null)
			ResourceHolderSlick.ttfFont.loadGlyphs();

		// Update key input states
		for (int i = 0; i < 2; i++) {
			if ((gameManager != null) && (gameManager.engine.length > i) && (gameManager.engine[i] != null)
					&& (gameManager.engine[i].isInGame) && (!pause || enableframestep)) {
				GameKeySlick.gamekey[i].update(container.getInput(), true);
			} else {
				GameKeySlick.gamekey[i].update(container.getInput(), false);
			}
		}
		
		doRetry();
		
		// Title bar update
		if ((gameManager != null) && (gameManager.engine != null) && (gameManager.engine.length > 0)
				&& (gameManager.engine[0] != null)) {
			boolean nowInGame = gameManager.engine[0].isInGame;
			if (prevInGameFlag != nowInGame) {
				prevInGameFlag = nowInGame;
				updateTitleBarCaption();
			}
		}

		// Pause
		if (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_PAUSE)
				|| GameKeySlick.gamekey[1].isPushKey(GameKeySlick.BUTTON_PAUSE)) {
			if (!pause) {
				if ((gameManager != null) && (gameManager.isGameActive()) && (pauseFrame <= 0)) {
					ResourceHolderSlick.soundManager.play("pause");
					pause = true;
					cursor = 0;
					if (!enableframestep)
						pauseFrame = 5;
					if (!enableframestep)
						ResourceHolderSlick.bgmPause();
				}
			} else {
				ResourceHolderSlick.soundManager.play("pause");
				pause = false;
				pauseFrame = 0;
				if (!enableframestep)
					ResourceHolderSlick.bgmResume();
			}
			updateTitleBarCaption();
		}
		// Pause menu
		else if (pause && !enableframestep && !pauseMessageHide) {
			
			// Cursor movement
			if (GameKeySlick.gamekey[0].isMenuRepeatKey(GameKeySlick.BUTTON_UP)) {
				cursor--;

				if (cursor < 0) {
					if (gameManager.replayMode && !gameManager.replayRerecord)
						cursor = 3;
					else
						cursor = 2;
				}

				ResourceHolderSlick.soundManager.play("cursor");
			}
			if (GameKeySlick.gamekey[0].isMenuRepeatKey(GameKeySlick.BUTTON_DOWN)) {
				cursor++;
				if (cursor > 3)
					cursor = 0;

				if ((!gameManager.replayMode || gameManager.replayRerecord) && (cursor > 2))
					cursor = 0;

				ResourceHolderSlick.soundManager.play("cursor");
			}

			// Confirm
			if (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_A)) {
				ResourceHolderSlick.soundManager.play("decide");
				if (cursor == 0) {
					// Continue
					pause = false;
					pauseFrame = 0;
					GameKeySlick.gamekey[0].clear();
					ResourceHolderSlick.bgmResume();
				} else if (cursor == 1) {
					// Retry
					ResourceHolderSlick.bgmStop();
					pause = false;
					gameManager.reset();
				} else if (cursor == 2) {
					// End
					ResourceHolderSlick.bgmStop();
					game.enterState(StateTitle.ID);
					return;
				} else if (cursor == 3) {
					// Replay re-record
					gameManager.replayRerecord = true;
					ResourceHolderSlick.soundManager.play("tspin1");
					cursor = 0;
				}
				updateTitleBarCaption();
			}
			// Unpause by cancel key
			else if (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_B) && (pauseFrame <= 0)) {
				ResourceHolderSlick.soundManager.play("pause");
				pause = false;
				pauseFrame = 5;
				GameKeySlick.gamekey[0].clear();
				ResourceHolderSlick.bgmResume();
				updateTitleBarCaption();
			}
		}
		if (pauseFrame > 0)
			pauseFrame--;

		// Hide pause menu
		pauseMessageHide = GameKeySlick.gamekey[0].isPressKey(GameKeySlick.BUTTON_C);

		if (gameManager.replayMode && !gameManager.replayRerecord && gameManager.engine[0].gameActive) {
			// Replay speed
			if (GameKeySlick.gamekey[0].isMenuRepeatKey(GameKeySlick.BUTTON_LEFT)) {
				if (fastforward > 0) {
					fastforward--;
				}
			}
			if (GameKeySlick.gamekey[0].isMenuRepeatKey(GameKeySlick.BUTTON_RIGHT)) {
				if (fastforward < 98) {
					fastforward++;
				}
			}

			// Replay re-record
			if (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_D)) {
				gameManager.replayRerecord = true;
				ResourceHolderSlick.soundManager.play("tspin1");
				cursor = 0;
			}
			// Show invisible blocks during replays
			if (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_E)) {
				gameManager.replayShowInvisible = !gameManager.replayShowInvisible;
				ResourceHolderSlick.soundManager.play("tspin1");
				cursor = 0;
			}
		} else {
			fastforward = 0;
		}

		if (gameManager != null) {
			// BGM
			if (ResourceHolderSlick.bgmPlaying != gameManager.bgmStatus.bgm) {
				ResourceHolderSlick.bgmStart(gameManager.bgmStatus.bgm);
			}
			if (ResourceHolderSlick.bgmIsPlaying()) {
				int basevolume = NullpoMinoSlick.propConfig.getProperty("option.bgmvolume", 128);
				float basevolume2 = basevolume / (float) 128;
				float newvolume = gameManager.bgmStatus.volume * basevolume2;
				if (newvolume < 0f)
					newvolume = 0f;
				if (newvolume > 1f)
					newvolume = 1f;
				container.setMusicVolume(newvolume);
				if (newvolume <= 0f)
					ResourceHolderSlick.bgmStop();
			}
		}

		// Execute game loops
		if (!pause || (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_FRAMESTEP) && enableframestep)) {
			if (gameManager != null) {
				for (int i = 0; i < Math.min(gameManager.getPlayers(), 2); i++) {
					if (!gameManager.replayMode || gameManager.replayRerecord || !gameManager.engine[i].gameActive) {
						GameKeySlick.gamekey[i].inputStatusUpdate(gameManager.engine[i].ctrl);
					}
				}

				for (int i = 0; i <= fastforward; i++)
					gameManager.updateAll();
			}
		}

		if (gameManager != null) {
			// Retry button
			if (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_RETRY)
					|| GameKeySlick.gamekey[1].isPushKey(GameKeySlick.BUTTON_RETRY)) {
				ResourceHolderSlick.bgmStop();
				pause = false;
				gameManager.reset();
			}

			// Return to title
			if (gameManager.getQuitFlag() || GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_GIVEUP)
					|| GameKeySlick.gamekey[1].isPushKey(GameKeySlick.BUTTON_GIVEUP)) {
				ResourceHolderSlick.bgmStop();
				game.enterState(StateTitle.ID);
				return;
			}
		}

		// Screenshot button
		if (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_SCREENSHOT)
				|| GameKeySlick.gamekey[1].isPushKey(GameKeySlick.BUTTON_SCREENSHOT))
			ssflag = true;

		// Exit button
		if (GameKeySlick.gamekey[0].isPushKey(GameKeySlick.BUTTON_QUIT)
				|| GameKeySlick.gamekey[1].isPushKey(GameKeySlick.BUTTON_QUIT)) {
			shutdown();
			container.exit();
		}

		if (NullpoMinoSlick.alternateFPSTiming)
			NullpoMinoSlick.alternateFPSSleep(true);
	}
}
