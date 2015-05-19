package game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Board {

	public static final int BOARD_WIDTH = 9;
	public static final int BLOCK_WIDTH = BOARD_WIDTH / 3;
	public static final Byte NULL_VALUE = 0;
	
	public Board(String rprString) {
		cols = initSets();
		boxes = initSets();
		rows = toArray(rprString);
	}

	private ArrayList<Set<Byte>> initSets() {
		ArrayList<Set<Byte>> sets = new ArrayList<>();
		for (int i = 0; i < BOARD_WIDTH; i++) {
			sets.add(new HashSet<>());
		}
		return sets;
	}

	public List<List<Byte>> getRows() {
		return rows;
	}

	public List<Set<Byte>> getCols() {
		return cols;
	}

	public List<Set<Byte>> getBlocks() {
		return boxes;
	}
	
	public void setCell(int i, int j, byte value) {
		byte oldValue = at(i, j);
		rows.get(i).set(j, value);
		cols.get(j).add(value);
		boxes.get(toBoxIndex(i, j)).add(value);
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
				cols.get(y).add(cellValue);
				boxes.get(toBoxIndex(x, y)).add(cellValue);
				
				if (cellValue != 0) {
					leftToSet--;
				}
			}
			rows.add(row);
		}

		return rows;
	}
	
	public int toBoxIndex(int i, int j) {
		int blockI = i / BLOCK_WIDTH;
		int blockJ = j / BLOCK_WIDTH;
		return BLOCK_WIDTH * blockI + blockJ;
	}

	public String toDebugString() {
		String s = "";
		for (List<Byte> row : rows) {
			 s += Arrays.toString(row.toArray()) + "\n";
		}
		return s;
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < BOARD_WIDTH; i++) {
			for (int j = 0; j < BOARD_WIDTH; j++) {
				s.append(at(i, j));
			}
		}
		return s.toString();
	}
	
	private List<List<Byte>> rows;
	private List<Set<Byte>> cols;
	private List<Set<Byte>> boxes;
	
	private int leftToSet = BOARD_WIDTH * BOARD_WIDTH;
}
