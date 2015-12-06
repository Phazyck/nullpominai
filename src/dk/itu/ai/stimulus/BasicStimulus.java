package dk.itu.ai.stimulus;

import com.anji.util.Properties;

import dk.itu.ai.Util;
import dk.itu.ai.navigation.Move;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;

public class BasicStimulus implements StimulusGenerator {

	@Override
	public void init(Properties props) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	private double asDouble(boolean b)
	{
		double result = b ? 1 : 0;
		
		return(result);
	}
	
	private double asDouble(int i)
	{
		double result = i;
		
		return(result);
	}

	@Override
	public double[] makeStimuli(GameEngine engine, Move move) {
		
		double[] stimuli = new double[10];
		
		Field fld = new Field(engine.field);
		Piece piece = new Piece(move.piece);
		int x = move.x;
		int y = move.y;
		int rt = move.rotation;
		
		// Features of the old field.
		
		// -- Adjacency checks
		boolean neighborOnLeft = piece.checkCollision(x - 1, y, fld);
		stimuli[0] = asDouble(neighborOnLeft);
		
		boolean neighborOnRight = piece.checkCollision(x + 1, y, fld);
		stimuli[1] = asDouble(neighborOnRight);
		
		boolean neighborOnTop = piece.checkCollision(x, y - 1, fld);
		stimuli[2] = asDouble(neighborOnTop);
		
		
		// -- Number of holes and valleys needing an I piece (before placement)
		int holeBefore = fld.getHowManyHoles();
		int lidBefore = fld.getHowManyLidAboveHoles();
		int needIValleyBefore = fld.getTotalValleyNeedIPiece();
		
		// -- Field height (before clears)
		int heightBefore = fld.getHighestBlockY();
		
		// Place the piece onto the field
		piece.placeToField(x, y, rt, fld);
		
		// Features of the new field.
		
		// -- Line clears
		int lines = fld.checkLine();
		stimuli[3] = asDouble(lines);
		
		if(lines > 0) 
		{
			fld.clearLine();
			fld.downFloatingBlocks();
		}
		
		// -- All clear
		boolean allclear = fld.isEmpty();
		stimuli[4] = asDouble(allclear);
		
		// -- Field height (after clears)
		int heightAfter = fld.getHighestBlockY();
		
		// -- Danger flag
		boolean danger = (heightAfter <= 12);
		stimuli[5] = asDouble(danger);
		
		// -- Number of holes and valleys needing an I piece (after placement)
		int holeAfter = fld.getHowManyHoles();
		int lidAfter = fld.getHowManyLidAboveHoles();
		int needIValleyAfter = fld.getTotalValleyNeedIPiece();
		
		// -- Difference in holes before and after. 
		// Negative values means hole count has decreased (good)
		// Positive values mean hole count has increased (bad).
		int holeDelta = holeAfter - holeBefore;
		stimuli[6] = asDouble(holeDelta);
		
		// -- Difference in lids before and after. 
		// Negative values means lid count has decreased (good)
		// Positive values mean lid count has increased (bad).
		int lidDelta = lidAfter - lidBefore;
		stimuli[7] = asDouble(lidDelta);
		
		// -- Difference in I-valleys before and after.
		int needIValleyDelta = needIValleyAfter - needIValleyBefore;
		stimuli[8] = asDouble(needIValleyDelta);
		
		// -- Difference in height before and after.
		int heightDelta = heightAfter - heightBefore;
		stimuli[9] = asDouble(heightDelta);
		
		return stimuli;
	}

}
