import java.io.PrintStream;
import java.util.Random;

public class EditDistanceVerify implements StringTestGenerator.helper
{
  private AstVisitedMap<Integer> nodeValue;
  public void useTest(char[] testA, char[] testB, float datalabel,
      PrintStream printStream)
  {
    EditDistance ed = new EditDistance(new String(testA),new String(testB));
    char[] sampA = fillRandom(testA);
    char[] sampB = fillRandom(testB);
    nodeValue = new AstVisitedMap<Integer>();
    int x = manualEd(sampA,sampB);
    int y = evaluateTree(ed.getRoot(),sampA,sampB);
    System.out.println("Node count: "+AstNodeCounter.count(ed.getRoot()));
    assert x==y:"\nA: "+new String(testA)+"\nB: "+new String(testB)+"\nx: "
      +x+"\ny: "+y+"\n";
  }

  private static Random random = new Random();
  private char[] fillRandom(char[] input)
  {
    char[] rv = new char[input.length];
    for(int i=0;i<input.length;++i)
      rv[i] = (input[i]=='#'?(char)(random.nextInt(4)+'A'):input[i]);
    return rv;
  }
  private int evaluateTree(AstNode node,char[] sampA, char[] sampB)
  {
    if(nodeValue.isVisited(node)) return nodeValue.valueAt(node);
    int rv = 0;
    if(node.getType()==AstValueNode.class)
      rv = ((AstValueNode)node.getData()).getValue();
    else if(node.getType()==AstNequNode.class)
    { AstNequNode cmp = (AstNequNode)node.getData();
      AstCharRef ar = cmp.getOperandA();
      AstCharRef br = cmp.getOperandB();
      char a = (ar.isSymbolic()?sampA[ar.getId()]:ar.getChar());
      char b = (br.isSymbolic()?sampB[br.getId()]:br.getChar());
      rv = (a!=b?1:0);
    }else if(node.getType()==AstAddNode.class)
    { AstNode[] child = node.children();
      for(int i=0;i<child.length;++i) rv+=evaluateTree(child[i],sampA,sampB);
    }else if(node.getType()==AstMinNode.class)
    { AstNode[] child = node.children();
      rv = Integer.MAX_VALUE;
      for(int i=0;i<child.length;++i) 
      { int v = evaluateTree(child[i],sampA,sampB);
        if(rv>v) rv=v;
      }
    }
    else assert false: "Unknown node type";
    nodeValue.visit(node,rv);
    return rv;
  }

  private int manualEd(char[] dataA,char[] dataB)
  {
    int dp[][]=new int[dataA.length+1][dataB.length+1];
    int a,b;
    for(a=0;a<=dataA.length;++a) dp[a][0]=a;
    for(b=0;b<=dataB.length;++b) dp[0][b]=b;
    for(a=1;a<=dataA.length;++a) for(b=1;b<=dataB.length;++b)
    {
      if(dataA[a-1]==dataB[b-1]) dp[a][b]=dp[a-1][b-1];
      else if(dp[a-1][b-1]<dp[a][b-1] && dp[a-1][b-1]<dp[a-1][b])
        dp[a][b]=dp[a-1][b-1]+1;
      else if(dp[a][b-1]<dp[a-1][b]) dp[a][b]=dp[a][b-1]+1;
      else dp[a][b]=dp[a-1][b]+1;
    }
    return dp[dataA.length][dataB.length];
  }
}
