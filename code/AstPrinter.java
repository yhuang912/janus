import java.io.PrintStream;


public class AstPrinter
{
  // Used for passing an int by reference
  private static class MutableInt
  {
    public int value;
    public MutableInt(int v) { value=v; }
  }

  /** The only public method in this class :) */
  public static void print(AstNode root, PrintStream out)
  { // vmap contains labels for visited nodes
    AstVisitedMap<String> vmap = new AstVisitedMap<String>();
    MutableInt vc = new MutableInt(1);  // serial number of the next label
    printSubtree(root,out,vmap,vc);
  }
  private static void printNewLabel (AstNode node, PrintStream out,
      AstVisitedMap<String> vmap, MutableInt vc)
  {
    String label = "N"+vc.value++;
    vmap.visit(node,label);
    out.print(label+':');
  }
  private static void printSubtree (AstNode node, PrintStream out,
      AstVisitedMap<String> vmap, MutableInt vc)
  {
    AstNodeData data = node.getData();
    if(node.getType()==AstValueNode.class) 
      out.print(((AstValueNode)data).getValue());
    else if(vmap.isVisited(node)) out.print(vmap.valueAt(node));
    else if(node.getType()==AstNequNode.class)
    { printNewLabel(node,out,vmap,vc);
      AstNequNode nequ = (AstNequNode)data;
      AstCharRef a = nequ.getOperandA(), b = nequ.getOperandB();
      String cha = (a.isSymbolic()?"A["+a.getId()+"]":"'"+a.getChar()+"'");
      String chb = (b.isSymbolic()?"B["+b.getId()+"]":"'"+b.getChar()+"'");
      out.print("nequ("+cha+", "+chb+")");
    }
    else if(node.getType()==AstSWSimilarityNode.class)
    { printNewLabel(node,out,vmap,vc);
      AstSWSimilarityNode sim = (AstSWSimilarityNode)data;
      AstCharRef a = sim.getOperandA(), b = sim.getOperandB();
      String cha = (a.isSymbolic()?"A["+a.getId()+"]":"'"+a.getChar()+"'");
      String chb = (b.isSymbolic()?"B["+b.getId()+"]":"'"+b.getChar()+"'");
      out.print("similar("+cha+", "+chb+")");
    }
    else
    { printNewLabel(node,out,vmap,vc);
      String operation;
      if (node.getType()==AstMinNode.class) operation="min";
      else if(node.getType()==AstMaxNode.class) operation="max";
      else if(node.getType()==AstAddNode.class) operation="add";
      else operation="---";
      out.print(operation+'(');
      for(int i=0;i<node.children().length;++i)
      { if(i>0) out.print(", ");
        printSubtree(node.children()[i],out,vmap,vc);
      }
      out.print(')');
    }
  }
}
