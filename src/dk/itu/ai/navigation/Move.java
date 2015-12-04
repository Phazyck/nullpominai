package dk.itu.ai.navigation;

/**
 * Move representation class, with accompanying required input to get to this position
 * @author Kas
 */
public class Move
{
	public static final int NONE = 0;
	
	// X-axis translation (-1 <-> +1)
	public static final int LEFT = -1;
	public static final int RIGHT = 1;
	
	// Piece rotation (-1 <-> +1)
	public static final int CCW = -1;
	public static final int CW = 1;
	
	// Iterator help
	public static final int[] TRANSLATIONS = {0, RIGHT, LEFT};
	public static final int[] ROTATIONS = {0, CW, CCW};
	
	
	public final int x;
	public final int y;
	public final int rotation;
	
	public final int dx;
	public final int drt;
	
	public final int floorKicksPerformed;
	
	public final Move parent;

	public Move(int x, int y, int rotation, int dx, int drt, int floorKicksPerformed, Move parent) {
		this.x = x;
		this.y = y;
		this.rotation = rotation;
		this.dx = dx;
		this.drt = drt;
		this.floorKicksPerformed = floorKicksPerformed;
		this.parent = parent;
	}

	/* Automatically generated code below */
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rotation;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + floorKicksPerformed;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Move other = (Move) obj;
		if (rotation != other.rotation)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		if (floorKicksPerformed != other.floorKicksPerformed)
			return false;
		return true;
	}
}