package main;

import static org.junit.Assert.assertEquals;
import lpsolve.LpSolveException;

import org.junit.Before;
import org.junit.Test;

public class DemoTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws LpSolveException {
		String result = new Demo().execute();
		assertEquals("693875412145632798782194356357421869816957234429368175274519683968743521531286947", result);
	}

}
