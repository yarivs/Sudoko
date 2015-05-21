package main;

import game.Board;
import lp.SpecificSolver;
import lpsolve.LpSolveException;

public class CustomModel {

	public static void main(String[] args) throws LpSolveException {
		Board board = new Board(args[0]);
		SpecificSolver solver = new SpecificSolver(board);
		System.out.println(solver.solve());
	}
}
