package ast.apps;
import ast.AstCharRef;
import ast.AstNode;
import ast.AstNodeData;

// it is a leaf node. AstCharRefs are not AstNode types
// represents similarity of two characters in a run of SmithWaterman

// TODO reducer and pringer for this
public class AstSWSimilarityNode implements AstNodeData
{
  public static final int MAX = 1;
  public static final int MIN = 0;

  private AstCharRef a,b;
  public AstSWSimilarityNode(AstCharRef aa,AstCharRef bb) { a=aa; b=bb; }
  public AstSWSimilarityNode(String strA,int indA,String strB,int indB)
    { a = new AstCharRef(strA,indA); b = new AstCharRef(strB,indB); }

  public AstCharRef getOperandA() { return a; }
  public AstCharRef getOperandB() { return b; }
  public AstNode[] childNodes() { return new AstNode[0]; }

  public static int evalChar(char chA,char chB)
    { return chA!=chB?0:1; }
  public int eval() 
  { assert !a.isSymbolic() && !b.isSymbolic()
      : "SmithWaterman similarity evaluation cannot be done on symbolics";
    return evalChar(a.getChar(),b.getChar());
  }

  // convenience constructor
  public static AstNode create(String strA,int indA,String strB,int indB)
    { return new AstNode(new AstSWSimilarityNode(strA,indA,strB,indB)); }
}
