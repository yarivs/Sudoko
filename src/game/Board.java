package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//import utils.Parser;

public class Board {

	public static final int BOARD_WIDTH = 9;
	public static final int BLOCK_WIDTH = BOARD_WIDTH / 3;
	public static final Byte NULL_VALUE = 0;
	
	public Board(String rprString) {
		rows = toArray(rprString);
	}

	public List<List<Byte>> getRows() {
		return rows;
	}

	public List<List<Byte>> getCols() {
		List<List<Byte>> cols = new ArrayList<>(rows.size());
		for (int col = 0; col < rows.size(); col++) {
			final int colFinal = col;
			cols.add(rows.parallelStream().map(row -> row.get(colFinal))
					.collect(Collectors.toList()));
		}
		return cols;
	}

	public List<List<Byte>> getBlocks() {
		List<List<Byte>> blocks = new ArrayList<>(rows.size());
		
		for (int i = 0; i < rows.size(); i++) {
			blocks.add(new ArrayList<>());
		}
		
		for (int i = 0; i < rows.size(); i++) {
			int blockI = i / BLOCK_WIDTH;
			for (int j = 0; j < rows.size(); j++) {
				int blockJ = j / BLOCK_WIDTH;
				
				Byte cellValue = rows.get(i).get(j);
				blocks.get(BLOCK_WIDTH * blockI + blockJ).add(cellValue);
			}	
		}
		
		
		return blocks;
	}
	
	public void setCell(int i, int j, byte value) {
		byte oldValue = at(i, j);
		rows.get(i).set(j, value);
		if (value == 0 && oldValue != 0) {
			leftToSet++;
		} else if (value != 0 && oldValue == 0) {
			leftToSet--;
		}
	}
	
	public byte at(int i, int j) {
		return rows.get(i).get(j);
	}
	
	public int dimention() {
		return BOARD_WIDTH;
	}
	
	public boolean isFull() {
		return leftToSet == 0;
	}
	
	public List<List<Byte>> toArray(String rprString) {
		List<List<Byte>> rows = new ArrayList<>(BOARD_WIDTH);

		for (int x = 0; x < BOARD_WIDTH; x++) {
			List<Byte> row = new ArrayList<>(BOARD_WIDTH);
			for (int y = 0; y < BOARD_WIDTH; y++) {
				byte cellValue = Byte.parseByte(rprString.charAt(x * BOARD_WIDTH + y)
						+ "");
				row.add(cellValue);
				if (cellValue != 0) {
					leftToSet--;
				}
			}
			rows.add(row);
		}

		return rows;
	}

	@Override
	public String toString() {
		String s = "";
		for (List<Byte> row : rows) {
			 s += Arrays.toString(row.toArray()) + "\n";
		}
		return s;
	}
	
	private List<List<Byte>> rows;
	
	private int leftToSet = BOARD_WIDTH * BOARD_WIDTH;
}
