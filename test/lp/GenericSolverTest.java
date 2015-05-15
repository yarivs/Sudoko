package lp;

import static org.junit.Assert.assertEquals;
import game.Board;
import lpsolve.LpSolveException;

import org.junit.Test;

public class GenericSolverTest {


	@Test
	public void test() throws LpSolveException {
		GenericSolver gSolver = new GenericSolver();
		Board board = new Board("000075400000000008080190000300001060000000034000068170204000603900000020530200000");
		gSolver.setSudokoData(board);
		
		assertEquals("693875412145632798782194356357421869816957234429368175274519683968743521531286947", gSolver.solve());
	}

}
