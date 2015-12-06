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

public class PromptingAI extends DummyAI {
	
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
			
			Debug.printStage(engine, x, y, rt, engine.nowPieceObject, 'Y');
			input = getInput(x, y, rt, ctrl);
		}
		
		ctrl.setButtonBit(input);
	}
	
	private static boolean promptUser( 
			int fromX, int fromY, int fromRt, 
			int toX, int toY, int toRt)
	{
		
		System.out.println("\nSuggested move:\n");
		int deltaRt = Util.getDeltaRt(toRt, fromRt);
		
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
		int moves = Util.getDeltaX(fromX, toX);
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
					
					Debug.printStage(engine, toX, toY, toRt, engine.nowPieceObject, (char)9633);
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
