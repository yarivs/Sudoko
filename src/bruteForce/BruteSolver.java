package bruteForce;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import game.Board;

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
		List<Byte> col = b.getCols().get(j);
		i /= Board.BLOCK_WIDTH;
		j /= Board.BLOCK_WIDTH;
		List<Byte> block = b.getBlocks().get(Board.BLOCK_WIDTH * i + j);

		List<Integer> possibilities = IntStream.range(1, 10).boxed()
				.filter(x -> !row.contains(x.byteValue()))
				.filter(x -> !col.contains(x.byteValue()))
				.filter(x -> !block.contains(x.byteValue()))
				.collect(Collectors.toList());

		// Converting to List<Byte>
		List<Byte> bPossibilities = new ArrayList<>();
		for (Integer num : possibilities) {
			bPossibilities.add(num.byteValue());
		}

		return bPossibilities;
	}

}
