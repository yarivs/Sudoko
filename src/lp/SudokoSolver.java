package lp;

import game.Board;

import java.util.ArrayList;
import java.util.List;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

public abstract class SudokoSolver {

	protected LpSolve lpSolver;
	protected int constraint_index;
	
	
	public SudokoSolver() {
		constraint_index = 1;
	}
	
	protected abstract boolean isVariableInUse(int row, int col, int value);
	
	protected int getVariableIndex(int row, int col, int value) { 
		return row * Board.BOARD_WIDTH * Board.BOARD_WIDTH + col * Board.BOARD_WIDTH + value;
	}

	protected void addConstraintFromList(List<Integer> constrainVariables)
			throws LpSolveException {
		if (constrainVariables.size() > 0) {
			double[] constrainCoeff = new double[constrainVariables.size()];
			for (int i = 0; i < constrainVariables.size(); i++) {
				constrainCoeff[i] = 1;
			}

			lpSolver.addConstraintex(constrainVariables.size(), constrainCoeff,
					convertIntegers(constrainVariables), LpSolve.EQ, 1);
		}
	}
	

	protected void addCellOneValueConstarin() throws LpSolveException {
		//each cell has one value
		for(int row = 0; row < 9; row++) {
			for(int col = 0; col < 9; col++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int value = 0; value < 9; value++) {
					if(isVariableInUse(row, col, value)) {
						oneCellConstrainVariables.add(getVariableIndex(row, col, value));
					}
				}				
				addConstraintFromList(oneCellConstrainVariables);
			}
		}
	}
	
	protected void addRowOneUniqueValueConstrain() throws LpSolveException {
		//rows
		for(int row = 0; row < 9; row++) {
			for(int value = 0; value < 9; value++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int col = 0; col < 9; col++) {
					if(isVariableInUse(row, col, value)) {
						oneCellConstrainVariables.add(getVariableIndex(row, col, value));
					}
				}
				addConstraintFromList(oneCellConstrainVariables);
			}			
		}
	}
	
	protected void addColOneUniqueValueConstrain() throws LpSolveException {
		//cols
		for(int col = 0; col < 9; col++) {
			for(int value = 0; value < 9; value++) {
				List<Integer> oneCellConstrainVariables = new ArrayList<>();
				for(int row = 0; row < 9; row++) {
					if(isVariableInUse(row, col, value)) {
						oneCellConstrainVariables.add(getVariableIndex(row, col, value));
					}
				}
				addConstraintFromList(oneCellConstrainVariables);
			}			
		}	
	}
	
	protected void addBlockOneUniqueValueConstrain() throws LpSolveException {
		//blocks
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				for(int value = 0; value < 9; value++) {
					List<Integer> oneCellConstrainVariables = new ArrayList<>();
					for(int k = 0; k < 3; k++) {
						for(int l = 0; l < 3; l++) {
							if(isVariableInUse(3*i + k, 3*j + l, value)) {
								oneCellConstrainVariables.add(getVariableIndex(3*i + k, 3*j + l, value));
							}
						}					
					}
					addConstraintFromList(oneCellConstrainVariables);
				}				
			}
		}
	}
	
	protected void addConstraint(double[] oneCellConstrainCoeff,
			int[] oneCellConstrainVariables) throws LpSolveException {
		
		addConstraint(constraint_index, oneCellConstrainCoeff,
				oneCellConstrainVariables);
		constraint_index++;
	}

	protected void addConstraint(int rowId, double[] oneCellConstrainCoeff,
			int[] oneCellConstrainVariables) throws LpSolveException {
		
		addConstraint(rowId, oneCellConstrainCoeff, oneCellConstrainVariables, 1);
	}
	
	protected void addConstraint(int rowId, double[] oneCellConstrainCoeff,
			int[] oneCellConstrainVariables, int equalsTo) throws LpSolveException {
		
		lpSolver.setRowex(rowId, oneCellConstrainVariables.length,
				oneCellConstrainCoeff, oneCellConstrainVariables);
		lpSolver.setRh(rowId, equalsTo);
		lpSolver.setConstrType(rowId, LpSolve.EQ);
	}
	
	protected void addConstraint(int variable, int value) throws LpSolveException {
		addConstraint(constraint_index++, new double[] {1}, new int[]{variable}, value);
	}
	
	protected void initTargetFunction() throws LpSolveException {
		double[] zeroCoef = new double[] {0};
		int[] arbitraryObjectiveFunction2 = new int[1];

		/* set the objective in lpsolve */
		lpSolver.setObjFnex(1, zeroCoef,
				arbitraryObjectiveFunction2);

		/* set the object direction to maximize */
		lpSolver.setMaxim();
	}

	public static int[] convertIntegers(List<Integer> integers) {
		int[] ret = new int[integers.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = integers.get(i).intValue();
		}
		return ret;
	}
}
