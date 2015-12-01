package dk.itu.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.ai.DummyAI;

public class BasicAIProxy extends DummyAI {
		
	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		Piece piece = engine.nowPieceObject;
		
		if(piece != null)
		{
			int x = engine.nowPieceX;
			int y = engine.nowPieceY;
			int rt = piece.direction;
			Debug.printStage(engine, x, y, rt, 'Y');
			
			
			int input = 0;
			
			int nowX = engine.nowPieceX;
			int nowY = engine.nowPieceY;
			int nowRt = engine.nowPieceObject.direction;
			
			int diffX = lastPieceX - nowX;
			int diffRt = lastPieceRt - nowRt;
			
			todoRt -= diffRt;
			todoMove -= diffX;
			
			if(todoDrop)
			{
				// Piece has not been dropped yet.
				
				System.out.println("SETTING CONTROL");
				
				if(todoRt > 0 && !ctrl.isPress(Controller.BUTTON_A))
				{
					// Piece needs to be rotated counter-clockwise.
					input |= Controller.BUTTON_BIT_A;
				}
				
				if(todoRt < 0 && !ctrl.isPress(Controller.BUTTON_B))
				{
					// Piece needs to be rotated clockwise.
					input |= Controller.BUTTON_BIT_B;
				}
				
				if(todoMove > 0 && !ctrl.isPress(Controller.BUTTON_LEFT))
				{
					// Piece needs to be moved to the left.
					input |= Controller.BUTTON_BIT_LEFT;
					
				}
				
				if(todoMove < 0 && !ctrl.isPress(Controller.BUTTON_RIGHT))
				{
					// Piece needs to be moved to the right.
					input |= Controller.BUTTON_BIT_RIGHT;
				}
				
				if(todoRt == 0 && todoMove == 0)
				{
					if(targetY != nowY && !ctrl.isPress(Controller.BUTTON_UP))
					{
						// DROP!
						input |= Controller.BUTTON_BIT_UP;
					}
					else if(targetY != nowY && !ctrl.isPress(Controller.BUTTON_DOWN))
					{
						// LOCK!
						input |= Controller.BUTTON_BIT_DOWN;
				
					}
				}
				
				
			}
			
			ctrl.setButtonBit(input);
			
			lastPieceX = nowX;
			lastPieceRt = nowRt;
		}
	}
	
	private boolean userConfirmed()
	{
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
	
	private int lastPieceRt;
	private int lastPieceX;
	
	private int todoRt;
	private int todoMove;
	private int targetY;
	private boolean todoDrop;
	
	@Override
	public void newPiece(GameEngine engine, int playerID) {
		
		Piece pieceNow = engine.nowPieceObject;
		int nowRt = pieceNow.direction;
		int nowX = engine.nowPieceX;
		int nowY = engine.nowPieceY;
		
		Field fld = new Field(engine.field);
		
		while(true)
		{
			for(int rt = 0; rt < Piece.DIRECTION_COUNT; rt++) {
				int minX = pieceNow.getMostMovableLeft(nowX, nowY, rt, engine.field);
				int maxX = pieceNow.getMostMovableRight(nowX, nowY, rt, engine.field);
				
				for(int x = minX; x <= maxX; x++) {
					fld.copy(engine.field);
					int y = pieceNow.getBottom(x, nowY, rt, fld);
					
					Debug.printStage(engine, x, y, rt, (char)9633);
					
					System.out.println("\nSuggested move:\n");
					
					int diffRt = nowRt - rt;
					
					switch(diffRt)
					{
						case -1: {
							System.out.println("> 1xCW rotation.");
						} break;
						case 0: {
							//System.out.println("No rotation.");
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
					
					String moveDir = "left";
					int diffX = nowX - x;
					int moves = diffX;
					if(diffX < 0)
					{
						moveDir = "right";
						moves *= -1;
					}
					
					if(diffX == 0)
					{
//						System.out.println("No movement.");
					}
					else
					{
						System.out.println("> Move " + moves + " times " + moveDir + ".");
					}
					
					System.out.println("> Drop.\n");
					
					if(userConfirmed())
					{
						bestHold = false;
						bestX = x;
						bestY = y;
						bestRt = rt;
						
						todoRt = diffRt;
						todoMove = diffX;
						todoDrop = true;
						targetY = y;
						
						lastPieceX = engine.nowPieceX;
						lastPieceRt = engine.nowPieceObject.direction;
						
						return;
					}
				}
			}
		}
	}
}
