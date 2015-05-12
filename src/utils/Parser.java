package utils;

import java.util.ArrayList;
import java.util.List;

public class Parser {

	public static final int BOARD_WIDTH = 9;

	public static List<List<Byte>> toArray(String rprString) {
		List<List<Byte>> rows = new ArrayList<>(BOARD_WIDTH);

		for (int x = 0; x < BOARD_WIDTH; x++) {
			List<Byte> row = new ArrayList<>(BOARD_WIDTH);
			for (int y = 0; y < BOARD_WIDTH; y++) {
				row.add(Byte.parseByte(rprString.charAt(x * BOARD_WIDTH + y)
						+ ""));
			}
			rows.add(row);
		}

		return rows;
	}
}
