package dk.itu.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.ai.BasicAI;

public class BasicAIProxy extends BasicAI {
	
	private int debugWidth = 14;
	private int debugHeight = 24;
	private int debugOffsetX = 2;
	private int debugOffsetY = 2;
	
	private char[][] debugField = new char[debugHeight][debugWidth];
	
	private void resetDebugStage(int width, int height) 
	{
		for(int y = 0; y < debugHeight; ++y)
		{
			char[] line = debugField[y];
			
			for(int x = 0; x < debugWidth; ++x)
			{
				if((y < debugOffsetY)
					|| (y >= debugOffsetY + height)
					|| (x < debugOffsetX)
					|| (x >= debugOffsetX + width))
				{
					line[x] = (char)9618; //'#';
				}
				else
				{
					line[x] = (char)9617;//' z';
				}
			
				
			}
		}
	}
	
	private void printDebugStage()
	{
		StringBuilder sb = new StringBuilder();
		for(char[] debugLine : debugField)
		{
			sb.append('\n');
			for(char block : debugLine)
			{
				sb.append(block);
			}
			
		}
		
		System.out.println(sb.toString());
		
	}
	
	private void setDebugChar(char c, int x, int y)
	{
		debugField[debugOffsetY + y][debugOffsetX + x] = c;
	}
	
	private void printStage(GameEngine engine, int pieceX, int pieceY, int pieceRt, char pieceChar)
	{
		Field field = engine.field;
		int height = field.getHeight();
		int width = field.getWidth();
		resetDebugStage(width, height);
			
		for(int y = 0; y < height; ++y)
		{
			Block[] row = field.getRow(y);
			
			for(int x = 0; x < width; ++x)
			{
				int color = row[x].color;
				
				if(color != Block.BLOCK_COLOR_NONE)
				{
					setDebugChar((char)9632, x, y);
				}
			}
		}
		
		Piece piece = engine.nowPieceObject;
		
		int[] dataX = piece.dataX[pieceRt];
		int[] dataY = piece.dataY[pieceRt];
		
		if(dataX.length != dataY.length)
		{
			System.out.println("Length mismatch in piece data");
		}
		
		for(int i = 0; i < dataX.length; ++i)
		{
			int px = pieceX + dataX[i];
			int py = pieceY + dataY[i];
			
			setDebugChar(pieceChar, px, py);
		}
		
		char d = (char)(pieceRt + (int)'0');
		
		setDebugChar(d, pieceX, pieceY);
		
		printDebugStage();
	}
	
	@Override
	public void setControl(GameEngine engine, int playerID, Controller ctrl) {
		Piece piece = engine.nowPieceObject;
		
		if(piece != null)
		{
			int x = engine.nowPieceX;
			int y = engine.nowPieceY;
			int rt = piece.direction;
			printStage(engine, x, y, rt, 'Y');
			String pieceName = Piece.getPieceName(piece.id);
			int offsetX = piece.dataOffsetX[rt];
			int offsetY = piece.dataOffsetY[rt];
			if(offsetX != 0 || offsetY != 0)
			{
				pieceName = "piece: " + pieceName;
			}
			
		}
		
		super.setControl(engine, playerID, ctrl);
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
					
					printStage(engine, x, y, rt, (char)9633);
					
					System.out.println("\nSuggested move:\n");
					
					switch(nowRt - rt)
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
					
					if(diffX < 0)
					{
						moveDir = "right";
						diffX *= -1;
					}
					
					if(diffX == 0)
					{
//						System.out.println("No movement.");
					}
					else
					{
						System.out.println("> Move " + diffX + " times " + moveDir + ".");
					}
					
					System.out.println("> Drop.\n");
					
					if(userConfirmed())
					{
						bestHold = false;
						bestX = x;
						bestY = y;
						bestRt = rt;
						bestXSub = x;
						bestYSub = y;
						bestRtSub = -1;
						bestPts = Integer.MAX_VALUE;
						return;
					}
				}
			}
		}
//		
//		
//		
//		if(pieceNow != null)
//		{
//			
//			int rt = pieceNow.direction;
//			printStage(engine, nowX, nowY, rt, 'Y');
//			
//			
//			String pieceName = Piece.getPieceName(pieceNow.id);
//			int offsetX = pieceNow.dataOffsetX[rt];
//			int offsetY = pieceNow.dataOffsetY[rt];
//			if(offsetX != 0 || offsetY != 0)
//			{
//				pieceName = "piece: " + pieceName;
//			}
//			
//		}
//		
//		super.newPiece(engine,  playerID);
	}
}
