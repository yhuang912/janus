import java.util.Scanner;

public class EditDistance
{
  private String a,b;
  private AstNode root;

  public EditDistance(String a, String b)
  {
    int ai,bi,an=a.length(), bn=b.length();
    this.a=a; this.b=b;

    // 1D array to avoid O(n^2) memory use if possible
    AstNode cur[] = new AstNode[bn+1], next[] = new AstNode[bn+1];
    AstReducer reducer = new AstReducer();  // the optimizer

    for(bi=0;bi<=bn;++bi) { cur[bi] = AstValueNode.create(bi); }

    for(ai=1;ai<=an;++ai)
    { next[0] = AstValueNode.create(ai);
      for(bi=1;bi<=bn;++bi)
      { 
        AstNode cmp = AstNequNode.create(a,ai-1,b,bi-1);
        AstNode add1ops[] = {cur[bi],AstValueNode.create(1)};
        AstNode add2ops[] = {next[bi-1],AstValueNode.create(1)};
        AstNode add3ops[] = {cur[bi-1], cmp};
        AstNode addnode1 = AstAddNode.create(add1ops);
        AstNode addnode2 = AstAddNode.create(add2ops);
        AstNode addnode3 = AstAddNode.create(add3ops);
        AstNode minOps[] = {addnode1,addnode2,addnode3};
        next[bi] = AstMinNode.create(minOps);
        reducer.reduce(next[bi]);
        reducer.minCount=reducer.childCount=0;
      }
      AstNode[] t=cur; cur=next; next=t;
    }
    root = cur[bn];
    reducer.reduceRoot(root);
    //reducer.printStats(System.err);
  }

  public AstNode getRoot() { return root; }
  public String getInputA() { return a; }
  public String getInputB() { return b; }

  // test harness
  public static void main(String args[])
  {
    Scanner scanner = new Scanner(System.in);
    String a = scanner.nextLine(), b = scanner.nextLine();
    EditDistance ed = new EditDistance(a,b);
    AstPrinter.print(ed.getRoot(),System.out);
    //System.out.println(AstNodeCounter.count(ed.getRoot()));
  }
  /*
  public static void main(String args[])
  {
    Scanner scanner = new Scanner(System.in);
    EditDistanceTest.helper tester = new EditDistanceVerify();
    String a = scanner.nextLine(), b = scanner.nextLine();
    tester.useTest(a.toCharArray(),b.toCharArray(),0f,System.out);
  }
  */

  //---------------------------------------------------------------------

  private static class TotalObjectCounter
  {
    private static class ObjectCount
    {
      public int count;
      public int recentTraversalId;
    };
    private AstVisitedMap<ObjectCount> visited =
      new AstVisitedMap<ObjectCount>();
    private int tid = 0;
    public void newCount() { tid++; }
    public int count(AstNode root) { return countHelper(root); }
    private int countHelper(AstNode node)
    { if(visited.isVisited(node))
      { ObjectCount oc = visited.valueAt(node);
        if(oc.recentTraversalId == tid) return 0;
        else
        { oc.recentTraversalId = tid;
          return oc.count;
        }
      }
      ObjectCount oc = new ObjectCount();
      oc.recentTraversalId = tid;
      int rv=1;
      AstNode[] child = node.children();
      for(int i=0;i<child.length;++i) rv+=countHelper(child[i]);
      oc.count=rv;
      visited.visit(node,oc);
      return rv;
    }

  }

  private TotalObjectCounter totalObjectCounter;
  private void printMemUsage(int iter,AstNode[] curRow,AstNode[] nextRow)
  {
    if(totalObjectCounter==null) totalObjectCounter = new TotalObjectCounter();
    int i,rv=0;
    totalObjectCounter.newCount();
    for(i=0;i<curRow.length;++i) rv+=totalObjectCounter.count(curRow[i]);
    for(i=0;i<nextRow.length;++i) rv+=totalObjectCounter.count(nextRow[i]);
    System.out.println("Iter "+iter+": "+rv);
  }

}
