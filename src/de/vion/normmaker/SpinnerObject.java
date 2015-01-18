package de.vion.normmaker;

/**
 * @author André Pomp
 * 
 *         This class represents the objects that we display in the spinner of
 *         the {@link MainActivity}
 *
 */
public class SpinnerObject {

	private int rows;
	private int cols;

	public SpinnerObject(int rows, int cols) {
		super();
		this.rows = rows;
		this.cols = cols;
	}

	@Override
	public String toString() {
		return "Rows: " + this.rows + " Columns: " + this.cols;
	}

	public int getRows() {
		return this.rows;
	}

	public int getCols() {
		return this.cols;
	}

}
