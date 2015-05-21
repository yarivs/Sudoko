package bruteForce;

import game.Board;

import java.util.Collection;
import java.util.List;

public class Validator {

	public static boolean isValidSolution(Board b) {
		return validateSectors(b.getRows()) && validateSectors(b.getRows())
				&& validateSectors(b.getBlocks());
	}

	private static boolean validateSectors(List<? extends Collection<Byte>> sectors) {
		for (Collection<Byte> sector : sectors) {
			if (!validateSector(sector)) {
				return false;
			}
		}

		return true;
	}

	private static boolean validateSector(Collection<Byte> sector) {
		if (sector.contains(Board.NULL_VALUE)) {
			return false;
		}
		long count = sector.stream().distinct().count();
		if (count != sector.size() || count != Board.BOARD_WIDTH) {
			return false;
		}

		return true;
	}
}
