/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
        * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
        * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
*/
package mu.nu.nullpo.gui.slick;

import mu.nu.nullpo.util.GeneralUtil;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

/**
 * Keyboard config screen state
 */
public class StateConfigKeyboard extends BasicGameState {
	/** This state's ID */
	public static final int ID = 9;

	/** Number of frames you have to wait */
	public static final int KEYACCEPTFRAME = 15;

	/** Number of keys to set */
	public static final int NUM_KEYS = 16;

	/** Player number */
	public int player = 0;

	/** true if navigation key setting mode */
	public boolean isNavSetting = false;

	/** StateBasedGame */
	protected StateBasedGame gameObj;

	/** true if no key is pressed now (for JInput mode) */
	protected boolean noPressedKey;

	/** Number of button currently being configured */
	protected int keynum;

	/** Frame counter */
	protected int frame;

	/** Nunber of frames left in key-set mode */
	protected int keyConfigRestFrame;

	/** Button settings */
	protected int[] keymap;

	/*
	 * Fetch this state's ID
	 */
	@Override
	public int getID() {
		return ID;
	}

	/**
	 * Button settings initialization
	 */
	protected void reset() {
		noPressedKey = true;

		keynum = 0;
		frame = 0;
		keyConfigRestFrame = 0;

		keymap = new int[NUM_KEYS];

		for(int i = 0; i < NUM_KEYS; i++) {
			if(!isNavSetting)
				keymap[i] = GameKeySlick.gamekey[player].keymap[i];
			else
				keymap[i] = GameKeySlick.gamekey[player].keymapNav[i];
		}
	}

	/*
	 * State initialization
	 */
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		gameObj = game;
	}

	/**
	 * Get key name
	 * @param key Keycode
	 * @return Key name
	 */
	protected String getKeyName(int key) {
		String str = org.lwjgl.input.Keyboard.getKeyName(key);
		return (str == null) ? String.valueOf(key) : str.toUpperCase();
	}

	/*
	 * Draw the screen
	 */
	public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
		if(!container.hasFocus()) {
			if(!NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
			return;
		}

		g.drawImage(ResourceHolderSlick.imgMenu, 0, 0);

		if(!isNavSetting) {
			NormalFontSlick.printFontGrid(1, 1, "KEYBOARD SETTING (" + (player + 1) + "P)", NormalFontSlick.COLOR_ORANGE);
		} else {
			NormalFontSlick.printFontGrid(1, 1, "KEYBOARD NAVIGATION SETTING (" + (player + 1) + "P)", NormalFontSlick.COLOR_ORANGE);
		}
		if(!NullpoMinoSlick.useJInputKeyboard) {
			NormalFontSlick.printFontGrid(1, 2, "SLICK NATIVE MODE", NormalFontSlick.COLOR_CYAN);
		} else {
			NormalFontSlick.printFontGrid(1, 2, "JINPUT MODE", NormalFontSlick.COLOR_PINK);
		}

		NormalFontSlick.printFontGrid(2,  4, "UP          : " + getKeyName(keymap[GameKeySlick.BUTTON_UP]), (keynum == 0));
		NormalFontSlick.printFontGrid(2,  5, "DOWN        : " + getKeyName(keymap[GameKeySlick.BUTTON_DOWN]), (keynum == 1));
		NormalFontSlick.printFontGrid(2,  6, "LEFT        : " + getKeyName(keymap[GameKeySlick.BUTTON_LEFT]), (keynum == 2));
		NormalFontSlick.printFontGrid(2,  7, "RIGHT       : " + getKeyName(keymap[GameKeySlick.BUTTON_RIGHT]), (keynum == 3));
		if(!isNavSetting) {
			NormalFontSlick.printFontGrid(2,  8, "A (L/R-ROT) : " + getKeyName(keymap[GameKeySlick.BUTTON_A]), (keynum == 4));
			NormalFontSlick.printFontGrid(2,  9, "B (R/L-ROT) : " + getKeyName(keymap[GameKeySlick.BUTTON_B]), (keynum == 5));
			NormalFontSlick.printFontGrid(2, 10, "C (L/R-ROT) : " + getKeyName(keymap[GameKeySlick.BUTTON_C]), (keynum == 6));
			NormalFontSlick.printFontGrid(2, 11, "D (HOLD)    : " + getKeyName(keymap[GameKeySlick.BUTTON_D]), (keynum == 7));
			NormalFontSlick.printFontGrid(2, 12, "E (180-ROT) : " + getKeyName(keymap[GameKeySlick.BUTTON_E]), (keynum == 8));
		} else {
			NormalFontSlick.printFontGrid(2,  8, "A (SELECT)  : " + getKeyName(keymap[GameKeySlick.BUTTON_A]), (keynum == 4));
			NormalFontSlick.printFontGrid(2,  9, "B (CANCEL)  : " + getKeyName(keymap[GameKeySlick.BUTTON_B]), (keynum == 5));
			NormalFontSlick.printFontGrid(2, 10, "C           : " + getKeyName(keymap[GameKeySlick.BUTTON_C]), (keynum == 6));
			NormalFontSlick.printFontGrid(2, 11, "D           : " + getKeyName(keymap[GameKeySlick.BUTTON_D]), (keynum == 7));
			NormalFontSlick.printFontGrid(2, 12, "E           : " + getKeyName(keymap[GameKeySlick.BUTTON_E]), (keynum == 8));
		}
		NormalFontSlick.printFontGrid(2, 13, "F           : " + getKeyName(keymap[GameKeySlick.BUTTON_F]), (keynum == 9));
		NormalFontSlick.printFontGrid(2, 14, "QUIT        : " + getKeyName(keymap[GameKeySlick.BUTTON_QUIT]), (keynum == 10));
		NormalFontSlick.printFontGrid(2, 15, "PAUSE       : " + getKeyName(keymap[GameKeySlick.BUTTON_PAUSE]), (keynum == 11));
		NormalFontSlick.printFontGrid(2, 16, "GIVEUP      : " + getKeyName(keymap[GameKeySlick.BUTTON_GIVEUP]), (keynum == 12));
		NormalFontSlick.printFontGrid(2, 17, "RETRY       : " + getKeyName(keymap[GameKeySlick.BUTTON_RETRY]), (keynum == 13));
		NormalFontSlick.printFontGrid(2, 18, "FRAME STEP  : " + getKeyName(keymap[GameKeySlick.BUTTON_FRAMESTEP]), (keynum == 14));
		NormalFontSlick.printFontGrid(2, 19, "SCREEN SHOT : " + getKeyName(keymap[GameKeySlick.BUTTON_SCREENSHOT]), (keynum == 15));
		NormalFontSlick.printFontGrid(2, 20, "[SAVE & EXIT]", (keynum == 16));

		NormalFontSlick.printFontGrid(1, 4 + keynum, "b", NormalFontSlick.COLOR_RED);

		if(frame >= KEYACCEPTFRAME) {
			if(keyConfigRestFrame > 0) {
				NormalFontSlick.printFontGrid(1, 22, "PUSH KEY... " + GeneralUtil.getTime(keyConfigRestFrame), NormalFontSlick.COLOR_PINK);
			} else if(keynum < NUM_KEYS) {
				NormalFontSlick.printFontGrid(1, 22, "UP/DOWN:   MOVE CURSOR", NormalFontSlick.COLOR_GREEN);
				NormalFontSlick.printFontGrid(1, 23, "ENTER:     SET KEY", NormalFontSlick.COLOR_GREEN);
				NormalFontSlick.printFontGrid(1, 24, "DELETE:    SET TO NONE", NormalFontSlick.COLOR_GREEN);
				NormalFontSlick.printFontGrid(1, 25, "BACKSPACE: CANCEL", NormalFontSlick.COLOR_GREEN);
			} else {
				NormalFontSlick.printFontGrid(1, 22, "UP/DOWN:   MOVE CURSOR", NormalFontSlick.COLOR_GREEN);
				NormalFontSlick.printFontGrid(1, 23, "ENTER:     SAVE & EXIT", NormalFontSlick.COLOR_GREEN);
				NormalFontSlick.printFontGrid(1, 24, "BACKSPACE: CANCEL", NormalFontSlick.COLOR_GREEN);
			}
		}

		// FPS
		NullpoMinoSlick.drawFPS(container);
		// Observer
		NullpoMinoSlick.drawObserverClient();
		if(!NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
	}

	/*
	 * Update game state
	 */
	public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
		if(!container.hasFocus()) {
			if(NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
			return;
		}

		frame++;
		if(keyConfigRestFrame > 0) keyConfigRestFrame--;

		// JInput
		if(NullpoMinoSlick.useJInputKeyboard) {
			JInputManager.poll();

			if(frame >= KEYACCEPTFRAME) {
				for(int i = 0; i < JInputManager.MAX_SLICK_KEY; i++) {
					if(JInputManager.isKeyDown(i)) {
						onKey(i);
						frame = 0;
						break;
					}
				}
			}
		}

		if(NullpoMinoSlick.alternateFPSTiming) NullpoMinoSlick.alternateFPSSleep();
	}

	/*
	 * When a key is released (Slick native)
	 */
	@Override
	public void keyReleased(int key, char c) {
		if(!NullpoMinoSlick.useJInputKeyboard) {
			onKey(key);
		}
	}

	/**
	 * When a key is released
	 * @param key Keycode
	 */
	protected void onKey(int key) {
		if(frame >= KEYACCEPTFRAME) {
			if(keyConfigRestFrame > 0) {
				// Key-set mode
				ResourceHolderSlick.soundManager.play("move");
				keymap[keynum] = key;
				keyConfigRestFrame = 0;
			} else {
				// Menu mode
				if(key == Input.KEY_UP) {
					ResourceHolderSlick.soundManager.play("cursor");
					keynum--;
					if(keynum < 0) keynum = NUM_KEYS;
				}
				if(key == Input.KEY_DOWN) {
					ResourceHolderSlick.soundManager.play("cursor");
					keynum++;
					if(keynum > NUM_KEYS) keynum = 0;
				}

				// Enter
				if(key == Input.KEY_ENTER) {
					ResourceHolderSlick.soundManager.play("decide");

					if(keynum >= NUM_KEYS) {
						// Save & Exit
						for(int i = 0; i < NUM_KEYS; i++) {
							if(!isNavSetting)
								GameKeySlick.gamekey[player].keymap[i] = keymap[i];
							else
								GameKeySlick.gamekey[player].keymapNav[i] = keymap[i];
						}
						GameKeySlick.gamekey[player].saveConfig(NullpoMinoSlick.propConfig);
						NullpoMinoSlick.saveConfig();
						gameObj.enterState(StateConfigMainMenu.ID);
					} else {
						// Set key
						keyConfigRestFrame = 60 * 5;
					}
					return;
				}

				// Delete
				if(key == Input.KEY_DELETE) {
					if((keynum < NUM_KEYS) && (keymap[keynum] != 0)) {
						ResourceHolderSlick.soundManager.play("change");
						keymap[keynum] = 0;
					}
				}

				// Backspace
				if(key == Input.KEY_BACK) {
					if(isNavSetting)
						gameObj.enterState(StateConfigKeyboardNavi.ID);
					else
						gameObj.enterState(StateConfigMainMenu.ID);
				}
			}
		}
	}

	/**
	 * Called when entering this state
	 */
	@Override
	public void enter(GameContainer container, StateBasedGame game) throws SlickException {
		reset();
	}

	/**
	 * Called when leaving this state
	 */
	@Override
	public void leave(GameContainer container, StateBasedGame game) throws SlickException {
		reset();
	}
}
