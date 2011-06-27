import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Stack;

public class AstReducer
{
  AstValueReducer valueHelper = new AstValueReducer();
  AstMinReducer minHelper = new AstMinReducer(this);
  AstMaxReducer maxHelper = new AstMaxReducer(this);
  AstAddReducer addHelper = new AstAddReducer(this);
  AstNequReducer nequHelper = new AstNequReducer();
  AstSWSimilarityReducer swSimilarityHelper = new AstSWSimilarityReducer();

  public void printStats(java.io.PrintStream printStream)
  {
    float avg = minHelper.branchtotal*1f/minHelper.branchcount;
    printStream.println("Min branch avg = "+avg+", max = "+minHelper.branchmax);
  }
  // node-specific extra info used by reduce
  public static class ReduceInfo
  {
    public int upperLim,lowerLim;
    public boolean hasConst;  // used only for non-leaves
    public boolean topDownDone = false;
  }

  private AstVisitedMap<ReduceInfo> visited;

  public AstReducer() { visited = new AstVisitedMap<ReduceInfo>(); }

  // Do some extra processing not normally done on every node
  // Does min() --> reduce(add(add(min(),+1),-1))
  // Helps activate the add(min(add())) rules even when min() is root
  //   Also calls Add-specific top-down reducer
  public void reduceRoot(AstNode root)
  {
    //reduceTopDown(root);
    AstNode addchild1[]={root,AstValueNode.create(+1)};
    AstNode add1 = AstAddNode.create(addchild1);
    AstNode addchild2[] = {add1,AstValueNode.create(-1)};
    AstNode add2 = AstAddNode.create(addchild2);
    reduce(add2);
    root.setData(add2.getData());
  }

  public void reduceTopDown(AstNode node)
  {
    Stack<AstNode> stk = new Stack<AstNode>();
    stk.push(node);
    int itercount = 0;
    //System.err.println(visited.size());
    while(!stk.empty())
    {
      node = stk.pop();
//      if(!visited.isVisited(node)) visited.visit(node,new ReduceInfo());
      if(!visited.isVisited(node)) continue;
      ReduceInfo nodeinfo = visited.valueAt(node);
      if(nodeinfo.topDownDone) continue;
      if(node.getType()==AstAddNode.class) 
        addHelper.reduceTopDown(node);
      nodeinfo.topDownDone = true;
      AstNode child[] = node.children();
      for(int i=0;i<child.length;++i) 
        stk.push(child[i]);
      itercount ++;
      /*
      if(itercount %1000000==0) 
        System.err.println(itercount+" "+visited.size());
        */
    }
    //System.err.println(node.getType());
  }

  public int childCount = 0;
  public long minCount = 0;
  public boolean loggingTime = false;
  public ReduceInfo reduce(AstNode node)
  {
    if(visited.isVisited(node)) return visited.valueAt(node); // already reduced
    ReduceInfo nodeinfo = new ReduceInfo();
    visited.visit(node,nodeinfo);

    boolean repeat;

    do
    {
      repeat = false;
      if(node.getType()==AstValueNode.class)
        repeat = valueHelper.reduce(node,nodeinfo);

      else if(node.getType()==AstMinNode.class)
        repeat = minHelper.reduce(node,nodeinfo);
      else if(node.getType()==AstMaxNode.class)
        repeat = maxHelper.reduce(node,nodeinfo);
      else if(node.getType()==AstAddNode.class)
        repeat = addHelper.reduce(node,nodeinfo);
      else if(node.getType()==AstNequNode.class)
        repeat = nequHelper.reduce(node,nodeinfo);
      else if(node.getType()==AstSWSimilarityNode.class)
        repeat = swSimilarityHelper.reduce(node,nodeinfo);

      if(loggingTime) 
      {
        AstPrinter.print(node,System.err);
        System.err.println();
      }
    }while(repeat);

    return nodeinfo;
  }

  private static int flatsize(AstNode root)
  { int rv=0;
    AstNode mychildren[] = root.children();
    for(int i=0;i<mychildren.length;++i)
      if(mychildren[i].getType()==root.getType())
        rv+=flatsize(mychildren[i]);
      else rv++;
    return rv;
  }

  public static ArrayList<AstNode> flattenedChildList(AstNode root)
  { ArrayList<AstNode> rv = new ArrayList<AstNode>();
    AstNode mychildren[] = root.children();
    if(mychildren.length>=1000)
      System.err.println("Flattening to breadth "+flatsize(root));
    for(int i=0;i<mychildren.length;++i) 
      if(mychildren[i].getType()==root.getType())
        rv.addAll(flattenedChildList(mychildren[i]));
      else rv.add(mychildren[i]);
    return rv;
  }
}
