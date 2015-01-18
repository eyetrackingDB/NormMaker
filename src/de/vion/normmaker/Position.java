package de.vion.normmaker;

import android.graphics.Point;

/**
 * @author André Pomp
 * 
 * This class represents a position (x,y) on the screen 
 *
 */
public class Position {

	public int picturePosition;

	public Point point;

	public Position(int picturePosition, Point point) {
		super();
		this.picturePosition = picturePosition;
		this.point = point;
	}
}
