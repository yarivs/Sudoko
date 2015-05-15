package lp;

import game.Board;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import main.Cell;

public class GenericSolver {

	LpSolve lp;

	private double[] dataConstrainCoeff;

	private int[] dataConstrainVariables;

	final static int NUMBER_OF_VARIABLES = 729;

	public GenericSolver() {
		
		try {
			initModel();
		} catch (LpSolveException e) {
			e.printStackTrace();
		}

		lp.setVerbose(LpSolve.IMPORTANT);
		
		dataConstrainCoeff = new double[] { 1 };
		dataConstrainVariables = new int[1];
	}

	private void initModel() throws LpSolveException {
		lp = LpSolve.makeLp(0, NUMBER_OF_VARIABLES);
		if (lp.getLp() == 0) {
			System.out.println("couldn't construct a new model...");
			return;
		}
		initTargetFunction();
		buildSudokuRulesConstraintes();
	}

	// here value is 1 to 9.
	private int getIndex(int row, int col, int value) {
		return row * Board.BOARD_WIDTH * Board.BOARD_WIDTH + col
				* Board.BOARD_WIDTH + value;
	}

	private int getValue(int variable) {
		int row = variable / 81;
		int col = (variable - (row * 81)) / 9;
		int value = variable - 81 * row - 9 * col;
		return value + 1;
	}

	public void buildSudokuRulesConstraintes() throws LpSolveException {
		final int NUM_VALUES = 9;

		/* create space large enough for one row */
		double[] oneCellConstrainCoeff = new double[NUM_VALUES];
		int[] oneCellConstrainVariables = new int[NUM_VALUES];

		/*
		 * let us name our variables. Not required, but can be useful for
		 * debugging
		 */
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				for (int value = 1; value < 10; value++) {
					lp.setColName(getIndex(row, col, value),
							String.format("A%d%d%d", row, col, value));
					lp.setBinary(getIndex(row, col, value), true);
				}
			}
		}

		lp.setAddRowmode(true); /*
								 * makes building the model faster if it is done
								 * rows by row
								 */

		for (int i = 0; i < NUM_VALUES; i++) {
			oneCellConstrainCoeff[i] = 1;
		}

		// each cell has one value
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				for (int value = 1; value < 10; value++) {
					oneCellConstrainVariables[value - 1] = getIndex(row, col,
							value);
				}
				lp.addConstraintex(NUM_VALUES, oneCellConstrainCoeff,
						oneCellConstrainVariables, LpSolve.EQ, 1);
			}
		}

		// rows
		for (int row = 0; row < 9; row++) {
			for (int value = 1; value < 10; value++) {
				for (int col = 0; col < 9; col++) {
					oneCellConstrainVariables[col] = getIndex(row, col, value);
				}
				lp.addConstraintex(NUM_VALUES, oneCellConstrainCoeff,
						oneCellConstrainVariables, LpSolve.EQ, 1);
			}
		}

		// cols
		for (int col = 0; col < 9; col++) {
			for (int value = 1; value < 10; value++) {
				for (int row = 0; row < 9; row++) {
					oneCellConstrainVariables[row] = getIndex(row, col, value);
				}
				lp.addConstraintex(NUM_VALUES, oneCellConstrainCoeff,
						oneCellConstrainVariables, LpSolve.EQ, 1);
			}
		}

		// boxes
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int value = 1; value < 10; value++) {
					for (int k = 0; k < 3; k++) {
						for (int l = 0; l < 3; l++) {
							oneCellConstrainVariables[k * 3 + l] = getIndex(3
									* i + k, 3 * j + l, value);

						}
					}
					lp.addConstraintex(NUM_VALUES, oneCellConstrainCoeff,
							oneCellConstrainVariables, LpSolve.EQ, 1);
				}
			}
		}

	}

	private void initTargetFunction() throws LpSolveException {
		double[] arbitraryObjectiveFunction = new double[1];
		int[] arbitraryObjectiveFunction2 = new int[1];

		/* set the objective in lpsolve */
		lp.setObjFnex(1, arbitraryObjectiveFunction,
				arbitraryObjectiveFunction2);

		/* set the object direction to maximize */
		lp.setMaxim();
	}

	public void setSudokoData(Board board) throws LpSolveException {
		// 25 items
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				if (board.at(row, col) != 0) {
					Cell cell = new Cell(row, col);
					int value = board.at(row, col);
					dataConstrainVariables[0] = getIndex(row, col, value);
					lp.addConstraintex(1, dataConstrainCoeff,
							dataConstrainVariables, LpSolve.EQ, 1);
					for (int val = 1; val < 10; val++) {
						if (val != value) {
							cell.setConstrain(val, 0, lp);
						}
					}
					for (int index = 0; index < 9; index++) {
						if (row != index) {
							Cell tempRowCell = new Cell(index, col);
							tempRowCell.setConstrain(value, 0, lp);
						}
						if (col != index) {
							Cell tempColCell = new Cell(row, index);
							tempColCell.setConstrain(value, 0, lp);
						}
					}
					Cell topLeftBoxCorner = cell.getTopLeftBlockCorner();
					for (int i = 0; i < 3; i++) {
						for (int j = 0; j < 3; j++) {
							Cell boxCell = new Cell(topLeftBoxCorner.row + i,
									topLeftBoxCorner.col + j);
							if (boxCell.row != cell.row
									&& boxCell.col != cell.col) { // TODO -
																	// check
								boxCell.setConstrain(value, 0, lp);
							}
						}
					}
				}
			}
		}
	}

	public String solve() throws LpSolveException {
		double[] coefficients = new double[NUMBER_OF_VARIABLES];
		lp.setAddRowmode(false); /*
								 * rowmode should be turned off again when done
								 * building the model
								 */

		/* Now let lpsolve calculate a solution */
		int ret = lp.solve();
		if (ret != LpSolve.OPTIMAL) {
			throw new IllegalArgumentException(
					"The given sudoku is unsolveable.");
		}

		String str = "";
		/* a solution is calculated, now lets get some results */

		/* variable values */
		lp.getVariables(coefficients);
		for (int j = 0; j < NUMBER_OF_VARIABLES; j++) {
			if (coefficients[j] == 1) {
				str += getValue(j);
			}
		}
		/* clean up such that all used memory by lpsolve is freed */
		if (lp.getLp() != 0)
			lp.deleteLp();

		return str;
	}
}
