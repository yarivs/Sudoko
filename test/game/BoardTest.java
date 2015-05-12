package game;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import bruteForce.BruteSolver;

public class BoardTest {

	@Before
	public void setUp() throws Exception {
	}

//	@Test
//	public void test() {
//		String rprString = "000000000" + "111111111" + "222222222"
//				+ "333333333" + "444444444" + "555555555" + "666666666"
//				+ "777777777" + "888888888";
//		Board b = new Board(rprString);
//		
//		b.getBlocks().forEach(col -> System.out.println(Arrays.toString(col.toArray())));
//	}
	
	@Test
	public void testName() throws Exception {
		Board b = new Board("001700509573024106800501002700295018009400305652800007465080071000159004908007053");
		b.getRows().forEach(col -> System.out.println(Arrays.toString(col.toArray())));
		boolean solve = BruteSolver.solve(b);
		
		
		System.out.println(solve);
		b.getRows().forEach(col -> System.out.println(Arrays.toString(col.toArray())));
		
	}

}
