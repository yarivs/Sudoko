package bruteForce;

import java.util.List;

import game.Board;

public class Validator {

	public static boolean validate(Board b) {
		return validateSectors(b.getRows()) && validateSectors(b.getRows())
				&& validateSectors(b.getBlocks());
	}

	private static boolean validateSectors(List<List<Byte>> sectors) {
		for (List<Byte> sector : sectors) {
			if (!validateSector(sector)) {
				return false;
			}
		}

		return true;
	}

	private static boolean validateSector(List<Byte> sector) {
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
