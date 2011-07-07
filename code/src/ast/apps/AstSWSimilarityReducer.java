package ast.apps;

import ast.AstCharRef;
import ast.AstNode;
import ast.AstReducer;
import ast.AstValueNode;

public class AstSWSimilarityReducer {
	public boolean reduce(AstNode node, AstReducer.ReduceInfo nodeinfo) {
		AstSWSimilarityNode sim = ((AstSWSimilarityNode) node.getData());
		AstCharRef a = sim.getOperandA();
		AstCharRef b = sim.getOperandB();
		if (!a.isSymbolic() && !b.isSymbolic()) {
			int v = sim.eval();
			node.setData(new AstValueNode(v));
			nodeinfo.upperLim = nodeinfo.lowerLim = v;
		} else {
			nodeinfo.upperLim = AstSWSimilarityNode.MAX;
			nodeinfo.lowerLim = AstSWSimilarityNode.MIN;
		}
		return false;
	}
}
