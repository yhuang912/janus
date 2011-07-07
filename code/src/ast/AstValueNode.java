package ast;

public class AstValueNode implements AstNodeData {
	private int value;

	public AstValueNode(int v) {
		value = v;
	}

	public int getValue() {
		return value;
	}

	// There is no setValue(). It's meant to be an immutable object.
	// use AstNode.set(new AstValueNode(v)) instead
	public AstNode[] childNodes() {
		return new AstNode[0];
	}

	// convenience constructor
	public static AstNode create(int v) {
		return new AstNode(new AstValueNode(v));
	}
}
