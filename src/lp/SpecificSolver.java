package lp;

import game.Board;
import lpsolve.LpSolve;
import lpsolve.LpSolveException;
import main.Cell;

public class SpecificSolver extends SudokoSolver {

	private Board board;

	// Indicates which variables out of the 729 is used.
	protected boolean[][][] variables;

	private int numVariables;

	public SpecificSolver(Board board) {
		variables = new boolean[Board.BOARD_WIDTH][Board.BOARD_WIDTH][Board.BOARD_WIDTH];
		this.board = board;
		try {
			initConstraints();
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}

	public void initConstraints() throws LpSolveException {

		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				for (int value = 0; value < 9; value++) {
					variables[row][col][value] = true;
				}
			}
		}

		removeUnnecessaryVariables();
		numVariables = countVariables();

		lpSolver = LpSolve.makeLp(0, numVariables);
		if (lpSolver.getLp() == 0) {
			System.out.println("couldn't construct a new model...");
			return;
		}

		/*
		 * let us name our variables. Not required, but can be useful for
		 * debugging
		 */
		int counter = 1;
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				for (int value = 0; value < 9; value++) {
					if (variables[row][col][value]) {
						lpSolver.setColName(counter,
								String.format("A%d%d%d", row, col, value + 1));
						lpSolver.setBinary(counter, true);
						counter++;
					}
				}
			}
		}

		lpSolver.setAddRowmode(true); /*
									 * makes building the model faster if it is
									 * done rows by row
									 */

		addCellOneValueConstarin();
		addRowOneUniqueValueConstrain();
		addColOneUniqueValueConstrain();
		addBlockOneUniqueValueConstrain();

		lpSolver.setAddRowmode(false); /*
										 * rowmode should be turned off again
										 * when done building the model
										 */

		initTargetFunction();

		/*
		 * just out of curioucity, now generate the model in lp format in file
		 * model.lp
		 */
		lpSolver.writeLp("model.lp");

		/* I only want to see important messages on screen while solving */
		lpSolver.setVerbose(LpSolve.IMPORTANT);
	}

	public String solve() throws LpSolveException {
		int ret;
		/* Now let lpsolve calculate a solution */
		ret = lpSolver.solve();
		if (ret == LpSolve.OPTIMAL)
			ret = 0;
		else
			ret = 5;
		StringBuilder str = new StringBuilder();
		if (ret == 0) {
			/* a solution is calculated, now lets get some results */

			/* variable values */

			int[][] solution = new int[9][9];
			/* create space large enough for one row */
			double[] coefficients = new double[numVariables];
			
			lpSolver.getVariables(coefficients);
			for (int j = 0; j < numVariables; j++) {
				if (coefficients[j] == 1) {
					String colName = lpSolver.getColName(j + 1);
					int row = Character.getNumericValue(colName.charAt(1));
					int col = Character.getNumericValue(colName.charAt(2));
					int val = Character.getNumericValue(colName.charAt(3));
					solution[row][col] = val;
				}
			}

			for (int row = 0; row < 9; row++) {
				for (int col = 0; col < 9; col++) {
					if (solution[row][col] == 0) {
						solution[row][col] = board.at(row, col);
					}
					str.append(solution[row][col]);
				}
			}
		}

		/* clean up such that all used memory by lpsolve is freed */
		if (lpSolver.getLp() != 0) {
			lpSolver.deleteLp();
		}

		return str.toString();
	}

	private int countVariables() {
		int numVariables = 0;
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				for (int value = 0; value < 9; value++) {
					if (variables[row][col][value]) {
						numVariables++;
					}
				}
			}
		}
		return numVariables;
	}

	private void removeUnnecessaryVariables() {
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				int value = board.at(row, col);
				if (value != 0) {
					// remove all this cell 9 variables (This cell is known).
					for (int val = 0; val < 9; val++) {
						variables[row][col][val] = false;
					}
					// remove all this row 9 variables for value (also the same
					// for col).
					for (int index = 0; index < 9; index++) {
						variables[row][index][value - 1] = false;
						variables[index][col][value - 1] = false;
					}
					// remove all this block 9 variables for value.
					Cell cell = new Cell(row, col);
					Cell topLeftBlockCorner = cell.getTopLeftBlockCorner();
					for (int i = 0; i < 3; i++) {
						for (int j = 0; j < 3; j++) {
							variables[topLeftBlockCorner.row + i][topLeftBlockCorner.col
									+ j][value - 1] = false;

						}
					}
				}
			}
		}
	}

	@Override
	protected int getVariableIndex(int row, int col, int value) {
		int index = row * 9 * 9 + col * 9 + value + 1;
		int counter = 0;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				for (int val = 0; val < 9; val++) {
					if (super.getVariableIndex(i, j, val + 1) <= index) {
						if (variables[i][j][val]) {
							counter++;
						}
					}
				}
			}
		}
		return counter;
	}

	@Override
	protected boolean isVariableInUse(int row, int col, int value) {
		return variables[row][col][value];
	}

}
