// it is a leaf node. AstCharRefs are not AstNode types
public class AstNequNode implements AstNodeData
{
  private AstCharRef a,b;
  public AstNequNode(AstCharRef aa,AstCharRef bb) { a=aa; b=bb; }
  public AstNequNode(String strA,int indA,String strB,int indB)
    { a = new AstCharRef(strA,indA); b = new AstCharRef(strB,indB); }

  public AstCharRef getOperandA() { return a; }
  public AstCharRef getOperandB() { return b; }
  public AstNode[] childNodes() { return new AstNode[0]; }

  // convenience constructor
  public static AstNode create(String strA,int indA,String strB,int indB)
    { return new AstNode(new AstNequNode(strA,indA,strB,indB)); }
}
