package ast;

public class AstNequReducer {
	public boolean reduce(AstNode node, AstReducer.ReduceInfo nodeinfo) {
		AstNequNode nequ = ((AstNequNode) node.getData());
		AstCharRef a = nequ.getOperandA();
		AstCharRef b = nequ.getOperandB();
		if (!a.isSymbolic() && !b.isSymbolic()) {
			int v = (a.getChar() != b.getChar() ? 1 : 0);
			node.setData(new AstValueNode(v));
			nodeinfo.upperLim = nodeinfo.lowerLim = v;
		} else {
			nodeinfo.upperLim = 1;
			nodeinfo.lowerLim = 0;
		}
		return false;
	}
}
