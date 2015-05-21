package bruteForce;

import game.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Solves Sudoku board using brute-force.
 */
public class BruteSolver {

	/**
	 * Solves sudoku board. Sets the solution in-place.
	 * 
	 * @param b
	 *            the board
	 * @return true if and only if a solution was found.
	 */
	public static boolean solve(Board b) {
		if (b.isFull()) {
			return true;
		}

		for (int i = 0; i < Board.BOARD_WIDTH; i++) {
			for (int j = 0; j < Board.BOARD_WIDTH; j++) {
				if (b.at(i, j) == Board.NULL_VALUE) {
					List<Byte> possibilities = getPossibilities(b, i, j);
					if (possibilities.isEmpty()) {
						return false;
					}

					for (Byte k : possibilities) {
						b.setCell(i, j, (byte) k);
						if (solve(b)) {
							return true;
						}
					}

					b.setCell(i, j, Board.NULL_VALUE);
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns a list of feasible assignments according to the state of the
	 * board.
	 * 
	 * @param b
	 *            the state of the board
	 * @param i
	 *            the row's index of the cell
	 * @param j
	 *            the col's index of the set
	 * @return feasible assignments to cell[i][j]
	 */
	public static List<Byte> getPossibilities(Board b, int i, int j) {
		List<Byte> row = b.getRows().get(i);
		Set<Byte> col = b.getCols().get(j);
		i /= Board.BLOCK_WIDTH;
		j /= Board.BLOCK_WIDTH;
		Set<Byte> block = b.getBlocks().get(Board.BLOCK_WIDTH * i + j);

		List<Byte> possibilities = new ArrayList<>();
		for (byte k = 1; k < Board.BOARD_WIDTH + 1; k++) {
			if (!row.contains(k) && !col.contains(k) && !block.contains(k)) {
				possibilities.add(k);
			}
		}

		return possibilities;
	}

}
