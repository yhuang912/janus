package ast;
public class AstMaxNode implements AstNodeData
{
  private AstNode[] children;

  public AstMaxNode(AstNode[] children) { this.children=children; }
  public AstNode[] childNodes() { return children; }

  // convenience constructor
  public static AstNode create(AstNode[] children)
    { return new AstNode(new AstMaxNode(children)); }
}
