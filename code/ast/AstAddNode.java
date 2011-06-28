package ast;


public class AstAddNode implements AstNodeData
{
  private AstNode[] children;

  public AstAddNode(AstNode[] children) { this.children=children; }
  public AstNode[] childNodes() { return children; }

  // convenience constructor
  public static AstNode create(AstNode[] children)
    { return new AstNode(new AstAddNode(children)); }
}
