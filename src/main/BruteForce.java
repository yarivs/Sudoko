package main;

import game.Board;
import bruteForce.BruteSolver;

public class BruteForce {

	public static void main(String[] args) {
		Board board = new Board(args[0]);
		BruteSolver.solve(board);
		System.out.println(board.toString());
	}
}
