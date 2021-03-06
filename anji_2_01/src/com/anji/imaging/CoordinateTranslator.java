/*
 * Copyright (C) 2004 Derek James and Philip Tucker
 * 
 * This file is part of ANJI (Another NEAT Java Implementation).
 * 
 * ANJI is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * created by Philip Tucker on Jun 9, 2004
 */

package com.anji.imaging;

/**
 * Interface to object which transforms a 2-d set of coordinates to another 2-d set of
 * coordinates.
 * 
 * @author Philip Tucker
 */
public interface CoordinateTranslator {
//	/**
//	 * x axis
//	 */
//	public final static short X = 0;
//	
//	/**
//	 * y axis
//	 */
//	public final static short Y = 1;
//
//	/**
//	 * z axis
//	 */
//	public final static short Z = 2;

	/**
	 * transforms <code>coords</code> based on perspective; <code>coords</code> should be
	 * between 0 and max, inclusive
	 * 
	 * @param coords
	 * @param max
	 */
	public void transform( IntLocation2D coords, int max );

}
