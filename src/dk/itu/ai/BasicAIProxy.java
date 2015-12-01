package dk.itu.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.play.GameEngine.Status;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;

public class BasicAIProxy extends DummyAI {
	
	/**
	 * This method calculates a delta rotation between a current rotation (fromRt) and a target rotation (toRt).
	 * 
	 * The return values are either:
	 * -1 - The delta to get from the fromRt to the toRt is a single counter-clockwise rotation. 
	 * 	0 - No delta, the fromRt and toRt rotations are alike.
	 *  1 - The delta to get from the fromRt to the toRt is a single clockwise rotation.
	 *  2 - The delta to get from the fromRt to the toRt is two clockwise rotations.
	 *  		(two counter-clockwise rotations can be used for the same result.) 
	 *  
	 * @param fromRt The rotation the piece is currently in.
	 * @param toRt The rotation the piece should rotate towards.
	 * @return The rotation delta.
	 */
	private static int getDeltaRt(int fromRt, int toRt)
	{
		int diffRt = ((toRt - fromRt) + 4) % 4;
		
		if(diffRt > 2)
		{
			diffRt -= 4;
		}
		
		return(diffRt);
	}
	
	/**
	 * This method calculates the delta movement between a current X-coordinate (fromX) and a target X-coordinate (toX).
	 * 
	 * The return value 'd', is to be interpreted as follows:
	 * 
	 *  if d < 0 then: 
	 *  	The delta to get from the fromX coordinate to the toX coordinate is 'd' moves to the left.
	 *   
	 * 	if d = 0 then: 
	 * 		No delta, the fromX and toX coordinates are alike.
	 * 
	 *  if d > 0 then:
	 *  	The delta to get from the fromX coordinate to the toX coordinate is 'd' moves to the right. 
	 * 
	 * @param fromX The X-coordinate the piece is currently in.
	 * @param toX The X-coordinate the piece should move towards.
	 * @return The movement delta along the X-axis.
	 */
	private static int getDeltaX(int fromX, int toX)
	{
		int diffX = toX - fromX;
		return(diffX);
	}
	
	private int getInput(int x, int y, int rt, Controller ctrl)
	{
		int input = 0;
		
		int deltaRt = getDeltaRt(rt, bestRt);
		int deltaX = getDeltaX(x, bestX);
		
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
			
			Debug.printStage(engine, x, y, rt, 'Y');
			input = getInput(x, y, rt, ctrl);
		}
		
		ctrl.setButtonBit(input);
	}
	
	private static boolean promptUser( 
			int fromX, int fromY, int fromRt, 
			int toX, int toY, int toRt)
	{
		
		System.out.println("\nSuggested move:\n");
		int deltaRt = getDeltaRt(toRt, fromRt);
		
		switch(deltaRt)
		{
			case -1: {
				System.out.println("> 1xCW rotation.");
			} break;
			case 0: {
				System.out.println("> No rotation.");
			} break;
			case 1: {
				System.out.println("> 1xCCW rotation.");
			} break;
			case 2: {
				System.out.println("> 2xCCW rotation.");
			} break;
			default: {
				System.out.println("Error in rotation code.");
			} break;
		}
		
		String moveDir = "right";
		int moves = getDeltaX(fromX, toX);
		if(moves < 0)
		{
			moveDir = "left";
			moves *= -1;
		}
		
		if(moves == 0)
		{
			System.out.println("> No movement.");
		}
		else
		{
			System.out.println("> Move " + moves + " times " + moveDir + ".");
		}
		
		System.out.println("> Drop.\n");
		System.out.println("> Lock.\n");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String answer = null;
		
		while(true)
		{
			System.out.print("Do you want to use this move? (y/N)");
			
			try {
				answer = br.readLine();
				
				if(answer.equalsIgnoreCase("y"))
				{
					return true;
				}
				else
				{
					return false;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void newPiece(GameEngine engine, int playerID) {
		
		Piece pieceNow = engine.nowPieceObject;
		int fromX = engine.nowPieceX;
		int fromY = engine.nowPieceY;
		int fromRt = pieceNow.direction;
		
		Field fld = new Field(engine.field);
		
		while(true)
		{
			for(int toRt = 0; toRt < Piece.DIRECTION_COUNT; toRt++) {
				int minX = pieceNow.getMostMovableLeft(fromX, fromY, toRt, engine.field);
				int maxX = pieceNow.getMostMovableRight(fromX, fromY, toRt, engine.field);
				
				for(int toX = minX; toX <= maxX; toX++) {
					fld.copy(engine.field);
					int toY = pieceNow.getBottom(toX, fromY, toRt, fld);
					
					Debug.printStage(engine, toX, toY, toRt, (char)9633);
					boolean confirmed = promptUser(fromX, fromY, fromRt, toX, toY, toRt);
					
					if(confirmed)
					{
						bestHold = false;
						bestX = toX;
						bestY = toY;
						bestRt = toRt;
						return;
					}
				}
			}
		}
	}
}
