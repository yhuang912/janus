package ast;

// Container to actual node
public class AstNode {
	private AstNodeData data;

	public AstNode() {
	}

	public AstNode(AstNodeData data) {
		this.data = data;
	}

	public void setData(AstNodeData data) {
		this.data = data;
	}

	public AstNodeData getData() {
		return data;
	}

	public Class getType() {
		return data.getClass();
	}

	public AstNode[] children() {
		return data.childNodes();
	}
}
