package bruteForce;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import game.Board;

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
		long count = sector.parallelStream().distinct().count();
		if (count != sector.size()) {
			return false;
		}

		return true;
	}
}
