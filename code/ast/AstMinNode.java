package ast;
public class AstMinNode implements AstNodeData
{
  private AstNode[] children;

  public AstMinNode(AstNode[] children) { this.children=children; }
  public AstNode[] childNodes() { return children; }

  // convenience constructor
  public static AstNode create(AstNode[] children)
    { return new AstNode(new AstMinNode(children)); }
}
