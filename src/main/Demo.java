package main;

import game.Board;

import java.util.ArrayList;
import java.util.List;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public class Demo {

	public Demo() {
	}
	
	private int[] getTopLeftBoxCorner(int row, int col) {
		int [] arr = new int[2];
		return arr;
	}
	
	//here value is 1 to 9.
	private int getIndex(int row, int col, int value) {
		return row * Board.BOARD_WIDTH * Board.BOARD_WIDTH + col * Board.BOARD_WIDTH + value;
	}
	
	private int getValue(int variable) {
		int row = variable / 81;
		int col = (variable	- (row * 81)) / 9;
		int value = variable - 81 * row - 9 * col;
		return value + 1;
	}

	public String execute() throws LpSolveException {
		LpSolve lp;
		int numVariables = 729;
		final int NUM_VALUES = 9;

		int ret = 0;

		Board board = new Board(
				"000075400000000008080190000300001060000000034000068170204000603900000020530200000");

		/* create space large enough for one row */
		int[] variables = new int[numVariables];
		double[] coefficients = new double[numVariables];
		double[] oneCellConstrainCoeff = new double[NUM_VALUES];
		int[] oneCellConstrainVariables = new int[NUM_VALUES];
		
		int[] dataConstrainVariables = new int[1];
		double[] dataConstrainCoeff = new double[1];

		lp = LpSolve.makeLp(0, numVariables);
		if (lp.getLp() == 0) {
			System.out.println("couldn't construct a new model...");
			return "";
		}

		/*
		 * let us name our variables. Not required, but can be useful for
		 * debugging
		 */
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				for(int value = 1; value < 10; value++) {
					lp.setColName(getIndex(row, col, value), String.format("A%d%d%d", row, col, value));
					lp.setBinary(getIndex(row, col, value), true);
				}
			}
		}

		lp.setAddRowmode(true); /*
								 * makes building the model faster if it is done
								 * rows by row
								 */
		
		for(int i = 0; i < NUM_VALUES; i++) {
			oneCellConstrainCoeff[i] = 1;
		}
		
		//each cell has one value
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				for(int value = 1; value < 10; value++) {
					oneCellConstrainVariables[value-1] = getIndex(row, col, value);
				}
				lp.addConstraintex(NUM_VALUES, oneCellConstrainCoeff, oneCellConstrainVariables, LpSolve.EQ, 1);
			}
		}
		
		
		//rows
		for(int row = 0; row < 9; row++) {
			for(int value = 1; value < 10; value++) {
				for(int col = 0; col < 9; col++) {
					oneCellConstrainVariables[col] = getIndex(row, col, value);
				}
				lp.addConstraintex(NUM_VALUES, oneCellConstrainCoeff, oneCellConstrainVariables, LpSolve.EQ, 1);
			}			
		}
		
		//cols
		for(int col = 0; col < 9; col++) {
			for(int value = 1; value < 10; value++) {
				for(int row = 0; row < 9; row++) {
					oneCellConstrainVariables[row] = getIndex(row, col, value);
				}
				lp.addConstraintex(NUM_VALUES, oneCellConstrainCoeff, oneCellConstrainVariables, LpSolve.EQ, 1);
			}			
		}
		
		//boxes
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				for(int value = 1; value < 10; value++) {
					for(int k = 0; k < 3; k++) {
						for(int l = 0; l < 3; l++) {
							oneCellConstrainVariables[k*3+l] = getIndex(3*i + k, 3*j + l, value);
							
						}					
					}
					lp.addConstraintex(NUM_VALUES, oneCellConstrainCoeff, oneCellConstrainVariables, LpSolve.EQ, 1);
				}				
			}
		}
		
		
		dataConstrainCoeff[0] = 1;
		//25 items
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				if(board.at(row,col) != 0) {
					Cell cell = new Cell(row, col);
					int value = board.at(row,col);
					dataConstrainVariables[0] = getIndex(row, col, value);
					lp.addConstraintex(1, dataConstrainCoeff, dataConstrainVariables, LpSolve.EQ, 1);
					for(int val = 1; val < 10; val++) {
						if(val != value) {
							cell.setConstrain(val, 0, lp);
						}
					}
					for(int index = 0; index < 9; index ++) {
						if(row != index) {
							Cell tempRowCell = new Cell(index, col);
							tempRowCell.setConstrain(value, 0, lp);
						}
						if(col != index) {
							Cell tempColCell = new Cell(row, index);
							tempColCell.setConstrain(value, 0, lp);
						}						
					}
					Cell topLeftBoxCorner = cell.getTopLeftBlockCorner();
					for(int i = 0; i < 3; i ++) {
						for(int j = 0; j < 3; j++) {
							Cell boxCell = new Cell(topLeftBoxCorner.row + i, topLeftBoxCorner.col + j);
							if(boxCell.row != cell.row && boxCell.col != cell.col) { //TODO - check
								boxCell.setConstrain(value, 0, lp);
							}
						}
					}
				}				
			}
		}
		


		lp.setAddRowmode(false); /*
								 * rowmode should be turned off again when
								 * done building the model
								 */

		double[] arbitraryObjectiveFunction = new double[1];
		int[] arbitraryObjectiveFunction2 = new int[1];

		/* set the objective in lpsolve */
		lp.setObjFnex(1, arbitraryObjectiveFunction, arbitraryObjectiveFunction2);		

		/* set the object direction to maximize */
		lp.setMaxim();

		/*
		 * just out of curioucity, now generate the model in lp format in
		 * file model.lp
		 */
		lp.writeLp("model.lp");

		/* I only want to see important messages on screen while solving */
		lp.setVerbose(LpSolve.IMPORTANT);

		/* Now let lpsolve calculate a solution */
		ret = lp.solve();
		if (ret == LpSolve.OPTIMAL)
			ret = 0;
		else
			ret = 5;
		

		String str = "";
		if (ret == 0) {
			/* a solution is calculated, now lets get some results */

			/* objective value */
			System.out.println("Objective value: " + lp.getObjective());

			/* variable values */
			lp.getVariables(coefficients);
			for (int j = 0; j < numVariables; j++) {
				System.out.println(lp.getColName(j + 1) + ": " + coefficients[j]);
				if(coefficients[j] == 1) {
					str += getValue(j);
				}
			}
			System.out.println(str);
//			Board solution = new Board(str);
//			System.out.println(solution);
			/* we are done now */
		}

		/* clean up such that all used memory by lpsolve is freed */
		if (lp.getLp() != 0)
			lp.deleteLp();

		return str;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static int[] convertIntegers(List<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = integers.get(i).intValue();
	    }
	    return ret;
	}
	
	//here value is 0 to 8.
	private int getRealIndex(int row, int col, int value, int[][][] variablesBitMap) {
		int index = row * 9 * 9 + col * 9 + value + 1;
		int counter = 0;
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 9; j++) {
				for(int val = 0; val < 9; val++) {
					if(getIndex(i, j, val+1) <= index) {
						if(variablesBitMap[i][j][val] == 1) {
							counter++;
						}
					} 
				}
			}
		}
		return counter;
	}	
	
	private void removeUnnecessaryVariables(int[][][] variablesBitMap, Board board) {
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				int value = board.at(row, col);
				if(value != 0) {
					//remove all this cell 9 variables (This cell is known).
					for(int val = 0; val < 9; val++) {
						variablesBitMap[row][col][val] = 0; 
					}
					//remove all this row 9 variables for value (also the same for col).
					for(int index = 0; index < 9; index++) {
						variablesBitMap[row][index][value-1] = 0;
						variablesBitMap[index][col][value-1] = 0;
					}
					//remove all this block 9 variables for value.
					Cell cell = new Cell(row, col);
					Cell topLeftBlockCorner = cell.getTopLeftBlockCorner();
					for(int i = 0; i < 3; i ++) {
						for(int j = 0; j < 3; j++) {
								variablesBitMap[topLeftBlockCorner.row + i][topLeftBlockCorner.col + j][value-1] = 0;
							
						}
					}
				}
			}
		}
	}
	
	private void addConstrainFromList(LpSolve lp, List<Integer> constrainVariables) throws LpSolveException {
		if(constrainVariables.size() > 0) {
			double[] constrainCoeff = new double[constrainVariables.size()];
			for(int i = 0; i < constrainVariables.size(); i++) {
				constrainCoeff[i] = 1;
			}
			 
			lp.addConstraintex(constrainVariables.size(), constrainCoeff, convertIntegers(constrainVariables), LpSolve.EQ, 1);
		}
	}
	
	private void addCellOneValueConstarin(LpSolve lp, int[][][] variablesBitMap) throws LpSolveException {
		//each cell has one value
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int value = 0; value < 9; value++) {
					if(variablesBitMap[row][col][value] == 1) {
						oneCellConstrainVariables.add(getRealIndex(row, col, value, variablesBitMap));
					}
				}				
				addConstrainFromList(lp, oneCellConstrainVariables);
			}
		}
	}

	
	private void addRowOneUniqueValueConstrain(LpSolve lp, int[][][] variablesBitMap) throws LpSolveException {
		//rows
		for(int row = 0; row < 9; row++) {
			for(int value = 0; value < 9; value++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int col = 0; col < 9; col++) {
					if(variablesBitMap[row][col][value] == 1) {
						oneCellConstrainVariables.add(getRealIndex(row, col, value, variablesBitMap));
					}
				}
				addConstrainFromList(lp, oneCellConstrainVariables);
			}			
		}
	}
	
	private void addColOneUniqueValueConstrain(LpSolve lp, int[][][] variablesBitMap) throws LpSolveException {
		//cols
		for(int col = 0; col < 9; col++) {
			for(int value = 0; value < 9; value++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int row = 0; row < 9; row++) {
					if(variablesBitMap[row][col][value] == 1) {
						oneCellConstrainVariables.add(getRealIndex(row, col, value, variablesBitMap));
					}
				}
				addConstrainFromList(lp, oneCellConstrainVariables);
			}			
		}	
	}
	
	private void addBlockOneUniqueValueConstrain(LpSolve lp, int[][][] variablesBitMap) throws LpSolveException {
		//blocks
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				for(int value = 0; value < 9; value++) {
					List<Integer> oneCellConstrainVariables = new ArrayList<>();
					for(int k = 0; k < 3; k++) {
						for(int l = 0; l < 3; l++) {
							if(variablesBitMap[3*i + k][3*j + l][value] == 1) {
								oneCellConstrainVariables.add(getRealIndex(3*i + k, 3*j + l, value, variablesBitMap));
							}
						}					
					}
					addConstrainFromList(lp, oneCellConstrainVariables);
				}				
			}
		}
	}
	
	public String execute2() throws LpSolveException {
		LpSolve lp;
		String str = "";
		
		//Indicates which variables out of the 729 is used.
		int[][][] variablesBitMap = new int[9][9][9];		
		
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				for(int value = 0; value < 9; value++) {
					variablesBitMap[row][col][value] = 1;
				}
			}
		}

		int ret = 0;

		Board board = new Board(
				"000075400000000008080190000300001060000000034000068170204000603900000020530200000");
		System.out.println(board);
		
		removeUnnecessaryVariables(variablesBitMap, board);
		
		int numVariables = 0;
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				for(int value = 0; value < 9; value++) {
					if(variablesBitMap[row][col][value] == 1) {
						numVariables++;
					}
				}
			}
		}
		

		/* create space large enough for one row */
		double[] coefficients = new double[numVariables];

		lp = LpSolve.makeLp(0, numVariables);
		if (lp.getLp() == 0) {
			System.out.println("couldn't construct a new model...");
			return "";
		}

		/*
		 * let us name our variables. Not required, but can be useful for
		 * debugging
		 */
		int counter = 1;
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				for(int value = 0; value < 9; value++) {
					if(variablesBitMap[row][col][value] == 1) {
						lp.setColName(counter, String.format("A%d%d%d", row, col, value+1));
						lp.setBinary(counter, true);
						counter++;
					}
				}
			}
		}		

		lp.setAddRowmode(true); /*
								 * makes building the model faster if it is done
								 * rows by row
								 */
		

		addCellOneValueConstarin(lp, variablesBitMap);
		addRowOneUniqueValueConstrain(lp, variablesBitMap);
		addColOneUniqueValueConstrain(lp, variablesBitMap);
		addBlockOneUniqueValueConstrain(lp, variablesBitMap);	


		lp.setAddRowmode(false); /*
								 * rowmode should be turned off again when
								 * done building the model
								 */

		double[] arbitraryObjectiveFunction = new double[1];
		int[] arbitraryObjectiveFunction2 = new int[1];

		/* set the objective in lpsolve */
		lp.setObjFnex(1, arbitraryObjectiveFunction, arbitraryObjectiveFunction2);		

		/* set the object direction to maximize */
		lp.setMaxim();

		/*
		 * just out of curioucity, now generate the model in lp format in
		 * file model.lp
		 */
		lp.writeLp("model.lp");

		/* I only want to see important messages on screen while solving */
		lp.setVerbose(LpSolve.IMPORTANT);

		/* Now let lpsolve calculate a solution */
		ret = lp.solve();
		if (ret == LpSolve.OPTIMAL)
			ret = 0;
		else
			ret = 5;
		

		
		if (ret == 0) {
			/* a solution is calculated, now lets get some results */

			/* objective value */
			System.out.println("Objective value: " + lp.getObjective());
			
			/* variable values */
			
			int[][] solution = new int[9][9];
			lp.getVariables(coefficients);
			for (int j = 0; j < numVariables; j++) {
				System.out.println(lp.getColName(j + 1) + ": " + coefficients[j]);
				if(coefficients[j] == 1) {
					int row = Character.getNumericValue(lp.getColName(j + 1).charAt(1));
					int col = Character.getNumericValue(lp.getColName(j + 1).charAt(2));
					int val = Character.getNumericValue(lp.getColName(j + 1).charAt(3));
					solution[row][col]=val;
				}
			}
			
			for(int row = 0; row < 9; row++) {
				for(int col = 0; col < 9; col++) {
					if(solution[row][col] == 0) {
						solution[row][col] = board.at(row, col);						
					}
					str += solution[row][col];
				}
			}
			System.out.println(str);
			Board solutionBoard = new Board(str);
			System.out.println(solutionBoard);
			/* we are done now */
		}

		/* clean up such that all used memory by lpsolve is freed */
		if (lp.getLp() != 0)
			lp.deleteLp();

		return str;
	}

	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();
			new Demo().execute();
			long endTime   = System.currentTimeMillis();
			long totalTime = endTime - startTime;
			System.out.println(totalTime);
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}
}