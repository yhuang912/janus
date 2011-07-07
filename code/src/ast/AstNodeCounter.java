package ast;

public class AstNodeCounter {
	/**
	 * The only public method in this class :) . Counts internal nodes, not
	 * leaves
	 */
	public static int count(AstNode root) {
		AstVisitedMap<Boolean> visited = new AstVisitedMap<Boolean>();
		return countSubtree(root, visited);
	}

	private static int countSubtree(AstNode node, AstVisitedMap<Boolean> visited) {
		if (visited.isVisited(node))
			return 0;
		else
			visited.visit(node, true);

		AstNode[] children = node.children();
		if (children.length == 0) // return 0; // don't count leaves
		{
			if (node.getType() == AstValueNode.class)
				return 0;
			else
				return 1;
		}
		int rv = children.length - 1;
		if (rv == 0)
			rv = 1;
		for (int i = 0; i < children.length; ++i)
			rv += countSubtree(children[i], visited);
		return rv;
	}
}
