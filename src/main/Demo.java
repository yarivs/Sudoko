package main;

import game.Board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import lpsolve.*;

public class Demo {

	public Demo() {
	}
	
	private int getIndex(int row, int col, int value) {
		return row * 9 * 9 + col * 9 + value;
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
					int value = board.at(row,col);
					dataConstrainVariables[0] = getIndex(row, col, value);
					lp.addConstraintex(1, dataConstrainCoeff, dataConstrainVariables, LpSolve.EQ, 1);
					for(int val = 1; val < 10; val++) {
						if(val != value) {
							dataConstrainVariables[0] = getIndex(row, col, val);
							lp.addConstraintex(1, dataConstrainCoeff, dataConstrainVariables, LpSolve.EQ, 0);
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
			for (int j = 0; j < numVariables; j++)
				System.out.println(lp.getColName(j + 1) + ": " + coefficients[j]);

			/* we are done now */
		}

		/* clean up such that all used memory by lpsolve is freed */
		if (lp.getLp() != 0)
			lp.deleteLp();

		return (ret);
	}

	public static void main(String[] args) {
		try {
			new Demo().execute();
		} catch (LpSolveException e) {
			e.printStackTrace();
		}
	}
}