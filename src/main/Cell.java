package main;

import lpsolve.*;

public class Cell {
	public int row;
	public int col;
	
	public Cell(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public Cell getTopLeftBlockCorner() {
		return new Cell((row / 3) * 3, (col / 3) * 3);
	}
	
	private int getIndex(int value) {
		return row * 9 * 9 + col * 9 + value;
	}	
	
	public void setConstrain(int value, int equlas, LpSolve lp) throws LpSolveException {
		int[] constarinVariable = {getIndex(value)};
		double[] constarinCoeff = {1};
		lp.addConstraintex(1, constarinCoeff, constarinVariable, LpSolve.EQ, equlas);
	}
}