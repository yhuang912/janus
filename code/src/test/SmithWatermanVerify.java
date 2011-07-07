package test;

import java.io.PrintStream;
import java.util.Random;

import ast.AstAddNode;
import ast.AstCharRef;
import ast.AstMaxNode;
import ast.AstNode;
import ast.AstValueNode;
import ast.AstVisitedMap;
import ast.apps.AstSWSimilarityNode;

public class SmithWatermanVerify {
	// Compare evaluated graph's value with manually calculated SmithWaterman
	public void useTest(char[] dataA, char[] dataB, int[] cost, PrintStream ps) {
		SmithWaterman sw = new SmithWaterman(new String(dataA), new String(
				dataB), cost);
		char[] sampA = fillRandom(dataA), sampB = fillRandom(dataB);
		nodeValue = new AstVisitedMap<Integer>();
		int x = evaluateGraph(sw.getRoot(), sampA, sampB);
		int y = manualSw(sampA, sampB, cost);
		assert x == y : "\nA = " + new String(dataA) + "\nB = "
				+ new String(dataB) + "\nx = " + x + ", y = " + y + "\n";
	}

	private static Random random = new Random();

	private char[] fillRandom(char[] input) {
		char[] rv = new char[input.length];
		for (int i = 0; i < rv.length; ++i) {
			if (input[i] == '#')
				rv[i] = (char) (random.nextInt() % 4 + 'A');
			else
				rv[i] = input[i];
		}
		return rv;
	}

	private AstVisitedMap<Integer> nodeValue;

	int evaluateGraph(AstNode node, char[] sampA, char[] sampB) {
		if (nodeValue.isVisited(node))
			return nodeValue.valueAt(node);
		int rv = 0;
		if (node.getType() == AstValueNode.class)
			rv = ((AstValueNode) node.getData()).getValue();
		else if (node.getType() == AstAddNode.class) {
			AstNode[] child = node.children();
			rv = 0;
			for (int i = 0; i < child.length; ++i)
				rv += evaluateGraph(child[i], sampA, sampB);
		} else if (node.getType() == AstMaxNode.class) {
			AstNode[] child = node.children();
			rv = Integer.MIN_VALUE;
			for (int i = 0; i < child.length; ++i) {
				int x = evaluateGraph(child[i], sampA, sampB);
				if (x > rv)
					rv = x;
			}
		} else if (node.getType() == AstSWSimilarityNode.class) {
			AstSWSimilarityNode data = (AstSWSimilarityNode) node.getData();
			AstCharRef a = data.getOperandA(), b = data.getOperandB();
			char ca = (a.isSymbolic() ? sampA[a.getId()] : a.getChar());
			char cb = (b.isSymbolic() ? sampB[b.getId()] : b.getChar());
			rv = AstSWSimilarityNode.evalChar(ca, cb);
		} else
			assert false : "Unknown node type";
		nodeValue.visit(node, rv);
		return rv;
	}

	private int manualSw(char sampA[], char sampB[], int cost[]) {
		assert cost.length >= sampA.length && cost.length >= sampB.length : "cost[] array too small for SmithWaterman evaluation";

		int dp[][] = new int[sampA.length + 1][sampB.length + 1];
		dp[0][0] = 0;
		for (int i = 1; i <= sampA.length; ++i)
			dp[i][0] = 0;
		for (int j = 1; j <= sampB.length; ++j)
			dp[0][j] = 0;
		for (int i = 1; i <= sampA.length; ++i)
			for (int j = 0; j <= sampB.length; ++j) {
				dp[i][j] = 0;
				int x = dp[i - 1][j - 1]
						+ AstSWSimilarityNode.evalChar(sampA[i - 1],
								sampB[j - 1]);
				if (x > 0)
					dp[i][j] = x;
				for (int k = 0; k < i; ++k) {
					x = dp[k][j] + cost[i - k - 1];
					if (dp[i][j] < x)
						dp[i][j] = x;
				}
				for (int k = 0; k < j; ++k) {
					x = dp[i][k] + cost[j - k - 1];
					if (dp[i][j] < x)
						dp[i][j] = x;
				}
			}
		return dp[sampA.length][sampB.length];
	}
}
