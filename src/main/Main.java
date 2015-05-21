package main;

import game.Board;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import bruteForce.BruteSolver;
import bruteForce.Validator;
import lp.GenericSolver;
import lp.SpecificSolver;
import lpsolve.LpSolveException;

public class Main {

	public static void main(String[] args) throws IOException, LpSolveException {

//		validateFile("resources/5_specific_solved.txt");
		String fromPath = "resources/5.txt";
		List<String> sudokus = Files.readAllLines(Paths.get(fromPath),
				Charset.defaultCharset());
		
//		solveBrutally(sudokus);
//		solveSpecificlly(sudokus);
		solveGenerally(sudokus);
	}

	private static void solveGenerally(List<String> sudokus)
			throws LpSolveException, IOException {
		GenericSolver solver = new GenericSolver();
		String toPath = "resources/5_generic_solved.txt";

		List<String> solved = new ArrayList<>();

		long start = System.currentTimeMillis();
		for (String sudoko : sudokus) {
			solver.setSudokoData(new Board(sudoko));
			String solution = solver.solve();
			solved.add(solution);
		}
		long end = System.currentTimeMillis();
		Files.write(Paths.get(toPath), solved, Charset.defaultCharset());
		System.out.println(end - start);
	}
	
	private static void solveSpecificlly(List<String> sudokus)
			throws LpSolveException, IOException {
		String toPath = "resources/5_specific_solved.txt";

		List<String> solved = new ArrayList<>();

		long start = System.currentTimeMillis();
		for (String sudoko : sudokus) {
			SpecificSolver solver = new SpecificSolver(new Board(sudoko));
			String solution = solver.solve();
			solved.add(solution);
		}
		long end = System.currentTimeMillis();
		Files.write(Paths.get(toPath), solved, Charset.defaultCharset());
		System.out.println(end - start);
	}
	
	private static void solveBrutally(List<String> sudokus)
			throws  IOException {
		String toPath = "resources/5_brute_solved.txt";

		List<String> solved = new ArrayList<>();

		long start = System.currentTimeMillis();
		for (String sudoko : sudokus) {
			Board b = new Board(sudoko);
			BruteSolver.solve(b);
			solved.add(b.toString());
		}
		long end = System.currentTimeMillis();
		Files.write(Paths.get(toPath), solved, Charset.defaultCharset());
		System.out.println(end - start);
	}
	
	
	public static void validateFile(String path) throws IOException {
		List<String> sudokus = Files.readAllLines(Paths.get(path),
				Charset.defaultCharset());
		
		for (String sudoko : sudokus) {
			if(!Validator.isValidSolution(new Board(sudoko))) {
				System.err.println("Invalid solution: " + sudoko);
			}
		}
		
		System.out.println("Done.");
	}
}
