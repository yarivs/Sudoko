package main;

import game.Board;
import lp.GenericSolver;
import lpsolve.LpSolveException;

public class GenericModel {

	public static void main(String[] args) throws LpSolveException {
		GenericSolver solver = new GenericSolver();
		Board board = new Board(args[0]);
		solver.setSudokoData(board);
		System.out.println(solver.solve());
	}
}
