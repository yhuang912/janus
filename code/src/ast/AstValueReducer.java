package ast;

public class AstValueReducer {
	public boolean reduce(AstNode node, AstReducer.ReduceInfo nodeinfo) {
		nodeinfo.upperLim = nodeinfo.lowerLim = ((AstValueNode) node.getData())
				.getValue();
		return false;
	}
}
