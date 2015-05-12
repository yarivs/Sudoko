package bruteForce;

import java.util.List;

import game.Board;

public class BruteSolver {

	public static boolean solve(Board b) {
		if (b.isFull()) {
			return Validator.validate(b);
		}
		
		while (!b.isFull()) {
			for (int i = 0; i < b.dimention(); i++) {
				for (int j = 0; j < b.dimention(); j++) {
					if (b.at(i, j) == Board.NULL_VALUE) {
						for (int k = 1; k < b.dimention() + 1; k++) {
							b.setCell(i, j, (byte)k);
							if (solve(b)) {
								return true;
							}
						}
						b.setCell(i, j, Board.NULL_VALUE);
					}
				}
			}
		}
		
		return false;
	}
	
	public static void solve(Board b, int i, int j, int value) {
		
	}
}
