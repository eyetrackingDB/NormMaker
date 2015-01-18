package de.vion.normmaker;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.view.View;

/**
 * @author André Pomp
 * 
 *         The view that shows the points at which the users have to gaze
 *
 */
public class DrawView extends View {

	List<Position> allPoints = new ArrayList<Position>();
	private Paint inactivePaint = new Paint();
	private Paint activePaint = new Paint();
	private boolean doOnce = false;

	// The points per row and column
	private int pointsPerRow;
	private int pointsPerCol;

	// The point that is currently looked at
	int currentPoint = 0;

	public DrawView(Context context, int pointsperRow, int pointsPerCol) {
		super(context);
		this.inactivePaint.setColor(Color.YELLOW);
		this.inactivePaint.setStyle(Style.FILL);
		this.activePaint.setColor(Color.BLUE);
		this.activePaint.setStyle(Style.FILL);
		this.pointsPerRow = pointsperRow;
		this.pointsPerCol = pointsPerCol;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!this.doOnce) {
			calculatePoints(canvas.getWidth(), canvas.getHeight());
			this.doOnce = true;
		}
		canvas.drawColor(Color.TRANSPARENT);
		for (int i = 0; i < this.allPoints.size(); i++) {
			if (this.currentPoint == i) {
				canvas.drawCircle(this.allPoints.get(i).point.x,
						this.allPoints.get(i).point.y, 20, this.activePaint);
			} else {
				canvas.drawCircle(this.allPoints.get(i).point.x,
						this.allPoints.get(i).point.y, 20, this.inactivePaint);
			}
		}
	}

	private void calculatePoints(int width, int height) {
		int totalWidth = width - 40; // (remove 20 on each side)
		int totalHeight = height - 40;// remove 20 on each side

		int widthBetweenTwoPoints = totalWidth / (this.pointsPerCol - 1);
		int heightBetweenTwoPoints = totalHeight / (this.pointsPerRow - 1);

		int currentWidth = 20;
		int currentHeight = 20;
		int position = 0;
		for (int i = 0; i < this.pointsPerRow; i++) {
			for (int j = 0; j < this.pointsPerCol; j++) {
				this.allPoints.add(new Position(position, new Point(
						currentWidth + (j * widthBetweenTwoPoints),
						currentHeight + (i * heightBetweenTwoPoints))));
				position++;
			}
		}

		// Randomize points
		// Collections.shuffle(this.allPoints, new Random(System.nanoTime()));
	}

	public int getCurrentPointNumber() {
		return this.currentPoint;
	}

	public Position getCurrentPosition() {
		return this.allPoints.get(this.currentPoint);
	}

	public void updatePoint() {
		this.currentPoint = (this.currentPoint + 1) % this.allPoints.size();
		this.invalidate();
	}
}