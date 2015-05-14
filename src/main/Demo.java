package main;

import java.util.ArrayList;
import java.util.List;

import game.Board;
import lpsolve.*;

public class Demo {

	public Demo() {
	}
	
	private int[] getTopLeftBoxCorner(int row, int col) {
		int [] arr = new int[2];
		return arr;
	}
	
	//here value is 1 to 9.
	private int getIndex(int row, int col, int value) {
		return row * 9 * 9 + col * 9 + value;
	}
	
	private int getValue(int variable) {
		int row = variable / 81;
		int col = (variable	- (row * 81)) / 9;
		int value = variable - 81 * row - 9 * col;
		return value + 1;
	}

	public int execute() throws LpSolveException {
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
			return -1;
		}

		/*
		 * let us name our variables. Not required, but can be useful for
		 * debugging
		 */
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				for(int value = 1; value < 10; value++) {
					lp.setColName(getIndex(row, col, value), String.format("A%d%d%d", row, col, value));
				}
			}
		}
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				for(int value = 1; value < 10; value++) {					
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
					Cell topLeftBoxCorner = cell.getTopLeftBoxCorner();
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
		

		if (ret == 0) {
			/* a solution is calculated, now lets get some results */

			/* objective value */
			System.out.println("Objective value: " + lp.getObjective());

			/* variable values */
			lp.getVariables(coefficients);
			String str = "";
			for (int j = 0; j < numVariables; j++) {
				System.out.println(lp.getColName(j + 1) + ": " + coefficients[j]);
				if(coefficients[j] == 1) {
					str += getValue(j);
				}
			}
			System.out.println(str);
			Board solution = new Board(str);
			System.out.println(solution);
			/* we are done now */
		}

		/* clean up such that all used memory by lpsolve is freed */
		if (lp.getLp() != 0)
			lp.deleteLp();

		return (ret);
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
	
	
	public int execute2() throws LpSolveException {
		LpSolve lp;
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
		
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				int value = board.at(row, col);
				if(value != 0) {
					for(int val = 0; val < 9; val++) {
						variablesBitMap[row][col][val] = 0; //no need all that 9 variables.
					}
					for(int index = 0; index < 9; index++) {
						variablesBitMap[row][index][value-1] = 0;
						variablesBitMap[index][col][value-1] = 0;
					}
					Cell cell = new Cell(row, col);
					Cell topLeftBlockCorner = cell.getTopLeftBoxCorner();
					for(int i = 0; i < 3; i ++) {
						for(int j = 0; j < 3; j++) {
								variablesBitMap[topLeftBlockCorner.row + i][topLeftBlockCorner.col + j][value-1] = 0;
							
						}
					}
				}
			}
		}
		
		int realNumVariables = 0;
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				for(int value = 0; value < 9; value++) {
					if(variablesBitMap[row][col][value] == 1) {
						realNumVariables++;
					}
				}
			}
		}
		

		/* create space large enough for one row */
		double[] coefficients = new double[realNumVariables];

		lp = LpSolve.makeLp(0, realNumVariables);
		if (lp.getLp() == 0) {
			System.out.println("couldn't construct a new model...");
			return -1;
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
	
		
		//each cell has one value
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int value = 0; value < 9; value++) {
					if(variablesBitMap[row][col][value] == 1) {
						oneCellConstrainVariables.add(getRealIndex(row, col, value, variablesBitMap));
					}
				}				
				double[] oneCellConstrainCoeff = new double[oneCellConstrainVariables.size()];
				for(int i = 0; i < oneCellConstrainVariables.size(); i++) {
					oneCellConstrainCoeff[i] = 1;
				}
				if(oneCellConstrainVariables.size() > 0) {
					lp.addConstraintex(oneCellConstrainVariables.size(), oneCellConstrainCoeff, convertIntegers(oneCellConstrainVariables), LpSolve.EQ, 1);
				}
			}
		}
		
		
		//rows
		for(int row = 0; row < 9; row++) {
			for(int value = 0; value < 9; value++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int col = 0; col < 9; col++) {
					if(variablesBitMap[row][col][value] == 1) {
						oneCellConstrainVariables.add(getRealIndex(row, col, value, variablesBitMap));
					}
				}
				double[] oneCellConstrainCoeff = new double[oneCellConstrainVariables.size()];
				for(int i = 0; i < oneCellConstrainVariables.size(); i++) {
					oneCellConstrainCoeff[i] = 1;
				}
				if(oneCellConstrainVariables.size() > 0) {
					lp.addConstraintex(oneCellConstrainVariables.size(), oneCellConstrainCoeff, convertIntegers(oneCellConstrainVariables), LpSolve.EQ, 1);
				}
			}			
		}
		
		//cols
		for(int col = 0; col < 9; col++) {
			for(int value = 0; value < 9; value++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int row = 0; row < 9; row++) {
					if(variablesBitMap[row][col][value] == 1) {
						oneCellConstrainVariables.add(getRealIndex(row, col, value, variablesBitMap));
					}
				}
				double[] oneCellConstrainCoeff = new double[oneCellConstrainVariables.size()];
				for(int i = 0; i < oneCellConstrainVariables.size(); i++) {
					oneCellConstrainCoeff[i] = 1;
				}
				if(oneCellConstrainVariables.size() > 0) {
					lp.addConstraintex(oneCellConstrainVariables.size(), oneCellConstrainCoeff, convertIntegers(oneCellConstrainVariables), LpSolve.EQ, 1);
				}
			}			
		}
		
		//boxes
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
					double[] oneCellConstrainCoeff = new double[oneCellConstrainVariables.size()];
					for(int m = 0; m < oneCellConstrainVariables.size(); m++) {
						oneCellConstrainCoeff[m] = 1;
					}
					if(oneCellConstrainVariables.size() > 0) {
						lp.addConstraintex(oneCellConstrainVariables.size(), oneCellConstrainCoeff, convertIntegers(oneCellConstrainVariables), LpSolve.EQ, 1);
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
		

		if (ret == 0) {
			/* a solution is calculated, now lets get some results */

			/* objective value */
			System.out.println("Objective value: " + lp.getObjective());
			
			/* variable values */
			
			int[][] solution = new int[9][9];
			lp.getVariables(coefficients);
			for (int j = 0; j < realNumVariables; j++) {
				System.out.println(lp.getColName(j + 1) + ": " + coefficients[j]);
				if(coefficients[j] == 1) {
					int row = Character.getNumericValue(lp.getColName(j + 1).charAt(1));
					int col = Character.getNumericValue(lp.getColName(j + 1).charAt(2));
					int val = Character.getNumericValue(lp.getColName(j + 1).charAt(3));
					solution[row][col]=val;
				}
			}
			
			String str = "";
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

		return (ret);
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