package lp;

import game.Board;
import game.Cell;

import java.util.Arrays;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class GenericSolver extends SudokoSolver {

	private double[] oneCoeff;
	private double[] onesCoeffs;

	private int[] dataConstrainVariables;

	private final static int NUMBER_OF_VARIABLES = 729;
	private static final int NUMBER_OF_SUDOKU_GENERIC_CONSTRAINTS = 81 * 4;
	private static final int NUMBER_OF_BOARD_DEPENDENT_CONSTRAINTS = 25 * 9 + 25 * 24;
	private final static int NUMBER_OF_CONSTRAINTS = NUMBER_OF_SUDOKU_GENERIC_CONSTRAINTS
			+ NUMBER_OF_BOARD_DEPENDENT_CONSTRAINTS;
	private static final int FIRST_BOARD_DEPENTED_CONSTRAINT_ID = NUMBER_OF_SUDOKU_GENERIC_CONSTRAINTS + 1;

	/* --- Constructor --- */

	public GenericSolver() {

		try {
			initModel();
		} catch (LpSolveException e) {
			e.printStackTrace();
		}

		lpSolver.setVerbose(LpSolve.IMPORTANT);
		oneCoeff = new double[] { 1 };
		dataConstrainVariables = new int[1];
	}

	/* --- Public Methods --- */

	public void buildSudokuRulesConstraintes() throws LpSolveException {
		final int NUM_VALUES = 9;

		constraint_index = 1;
		int[] oneCellConstrainVariables = new int[NUM_VALUES];

		/*
		 * let us name our variables. Not required, but can be useful for
		 * debugging
		 */
		for (int row = 0; row < 9; row++) {
			for (int col = 0; col < 9; col++) {
				for (int value = 1; value < 10; value++) {
					int variableIndex = getVariableIndex(row, col, value);
					lpSolver.setColName(variableIndex,
							String.format("A%d%d%d", row, col, value));
					lpSolver.setBinary(variableIndex, true);
				}
			}
		}

		lpSolver.setAddRowmode(true); /*
									 * makes building the model faster if it is
									 * done rows by row
									 */

		onesCoeffs = new double[NUM_VALUES];
		Arrays.fill(onesCoeffs, 1);

		uniqueNumberInEachCell(oneCellConstrainVariables);
		uniqueNumberInEachRow(oneCellConstrainVariables);
		uniqueNumberInEachCol(oneCellConstrainVariables);
		uniqueNumberInEachBox(oneCellConstrainVariables);

	}

	public void setSudokoData(Board board) throws LpSolveException {
		// 25 items
		constraint_index = FIRST_BOARD_DEPENTED_CONSTRAINT_ID;
		for (int row = 0; row < Board.BOARD_WIDTH; row++) {
			for (int col = 0; col < Board.BOARD_WIDTH; col++) {
				if (board.at(row, col) != Board.NULL_VALUE) {
					assignKnownValue(board.at(row, col), row, col);
				}
			}
		}
	}

	public String solve() throws LpSolveException {
		double[] coefficients = new double[NUMBER_OF_VARIABLES];
		lpSolver.setAddRowmode(false); /*
										 * rowmode should be turned off again
										 * when done building the model
										 */

		/* Now let lpsolve calculate a solution */
		int ret = lpSolver.solve();
		if (ret != LpSolve.OPTIMAL) {
			throw new IllegalArgumentException(
					"The given sudoku is unsolveable");
		}

		StringBuilder str = new StringBuilder();
		/* a solution is calculated, now lets get some results */

		/* variable values */
		lpSolver.getVariables(coefficients);
		for (int j = 0; j < NUMBER_OF_VARIABLES; j++) {
			if (coefficients[j] == 1) {
				str.append(getValue(j));
			}
		}
		/* clean up such that all used memory by lpsolve is freed */
//		if (lpSolver.getLp() != 0)
//			lpSolver.deleteLp();

		return str.toString();
	}

	/* --- Private Methods --- */

	private void initModel() throws LpSolveException {
		lpSolver = LpSolve.makeLp(NUMBER_OF_CONSTRAINTS, NUMBER_OF_VARIABLES);
		if (lpSolver.getLp() == 0) {
			System.out.println("couldn't construct a new model...");
			return;
		}
		initTargetFunction();
		buildSudokuRulesConstraintes();
	}

	private int getValue(int variable) {
		int row = variable / 81;
		int col = (variable - (row * 81)) / 9;
		int value = variable - 81 * row - 9 * col;
		return value + 1;
	}

	private void uniqueNumberInEachBox(int[] oneCellConstrainVariables)
			throws LpSolveException {
		// boxes
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				for (int value = 1; value < 10; value++) {
					for (int k = 0; k < 3; k++) {
						for (int l = 0; l < 3; l++) {
							oneCellConstrainVariables[k * 3 + l] = getVariableIndex(
									3 * i + k, 3 * j + l, value);

						}
					}
					addConstraint(onesCoeffs, oneCellConstrainVariables);
				}
			}
		}
	}

	private void uniqueNumberInEachCol(int[] oneCellConstrainVariables)
			throws LpSolveException {
		// cols
		for (int col = 0; col < 9; col++) {
			for (int value = 1; value < 10; value++) {
				for (int row = 0; row < 9; row++) {
					oneCellConstrainVariables[row] = getVariableIndex(row, col,
							value);
				}
				addConstraint(onesCoeffs, oneCellConstrainVariables);
			}
		}
	}

	private void uniqueNumberInEachRow(int[] oneCellConstrainVariables)
			throws LpSolveException {

		for (int row = 0; row < 9; row++) {
			for (int value = 1; value < 10; value++) {
				for (int col = 0; col < 9; col++) {
					oneCellConstrainVariables[col] = getVariableIndex(row, col,
							value);
				}
				addConstraint(onesCoeffs, oneCellConstrainVariables);
			}
		}
	}

	private void uniqueNumberInEachCell(int[] oneCellConstrainVariables)
			throws LpSolveException {

		for (int row = 0; row < Board.BOARD_WIDTH; row++) {
			for (int col = 0; col < Board.BOARD_WIDTH; col++) {
				for (int value = 1; value < Board.BOARD_WIDTH + 1; value++) {
					oneCellConstrainVariables[value - 1] = getVariableIndex(
							row, col, value);
				}
				addConstraint(onesCoeffs, oneCellConstrainVariables);
			}
		}
	}

	private void assignKnownValue(byte value, int row, int col)
			throws LpSolveException {

		Cell cell = new Cell(row, col);
		assignKnownValueToCell(value, row, col, cell);
		updateRowWithKnownCell(value, row, col);
		updateColWithKnownCell(value, row, col);
		updateBoxWithKnownCell(value, cell);
	}

	private void updateBoxWithKnownCell(byte value, Cell cell)
			throws LpSolveException {
		Cell topLeftBoxCorner = cell.getTopLeftBlockCorner();
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				Cell boxCell = new Cell(topLeftBoxCorner.row + i,
						topLeftBoxCorner.col + j);
				if (boxCell.row != cell.row && boxCell.col != cell.col) { // TODO
																			// -
																			// check
//					boxCell.setConstrain(value, 0, lpSolver);
					addConstraint(getVariableIndex(boxCell.row, boxCell.col, value), 0);
				}
			}
		}
	}

	private void updateColWithKnownCell(byte value, int row, int col)
			throws LpSolveException {
		for (int index = 0; index < 9; index++) {
			if (col != index) {
//				Cell tempColCell = new Cell(row, index);
//				tempColCell.setConstrain(value, 0, lpSolver);
				addConstraint(getVariableIndex(row, index, value), 0);
			}
		}
	}

	private void updateRowWithKnownCell(byte value, int row, int col)
			throws LpSolveException {
		for (int index = 0; index < 9; index++) {
			if (row != index) {
//				Cell tempRowCell = new Cell(index, col);
//				tempRowCell.setConstrain(value, 0, lpSolver);
				addConstraint(getVariableIndex(index, col, value), 0);
			}
		}
	}

	private void assignKnownValueToCell(byte value, int row,
			int col, Cell cell) throws LpSolveException {
		for (int val = 1; val < 10; val++) {
			if (val != value) {
//				cell.setConstrain(val, 0, lpSolver);
				addConstraint(cell.getIndex(val), 0);
			} else {
				dataConstrainVariables[0] = getVariableIndex(row, col, val);
				addConstraint(oneCoeff, dataConstrainVariables);
			}
		}
	}

	@Override
	public boolean isVariableInUse(int row, int col, int value) {
		return true;
	}
}
