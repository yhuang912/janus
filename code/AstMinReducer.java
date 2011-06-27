import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

public class AstMinReducer 
{
  private AstReducer reducerMain;
  public AstMinReducer(AstReducer r) { reducerMain=r; }
  public boolean reduce(AstNode node,AstReducer.ReduceInfo nodeinfo)
  {
    boolean repeat = false;
    // scount = Symbol Count, curmin = Current Minimum of AstValueNode
    int scount = 0, curmin = Integer.MAX_VALUE;
    nodeinfo.upperLim = nodeinfo.lowerLim = Integer.MAX_VALUE;
    /*
    AstNode[] children = AstReducer.flattenedChildList(node)
                          .toArray(new AstNode[0]);
    assert children.length>0;
    // release references to old data
    node.setData(new AstMinNode(children));
    */
    AstNode[] children = node.children();
    for(int i=0;i<children.length;++i)
    { reducerMain.reduce(children[i]);
//      if(children[i].getType()==node.getType()) return true;
    }
    reducerMain.minCount++;
    for(int i=0;i<children.length;++i)
    { if(children[i].getType()!=AstValueNode.class) scount++;
      else
      { int v = ((AstValueNode)children[i].getData()).getValue();
        if(curmin>v) curmin=v;
      }

      reducerMain.childCount++;
      AstReducer.ReduceInfo childInfo = reducerMain.reduce(children[i]);
      int temphi = childInfo.upperLim;
      int templo = childInfo.lowerLim;
      if(nodeinfo.upperLim>temphi) nodeinfo.upperLim = temphi;
      if(nodeinfo.lowerLim>templo) nodeinfo.lowerLim = templo;
    }
    boolean hasconst = children.length>scount && curmin<nodeinfo.upperLim;
    scount = 0;
    AstNode lastSym = null;
    for(int i=0;i<children.length;++i)
      if(reducerMain.reduce(children[i]).lowerLim<nodeinfo.upperLim
          && children[i].getType()!=AstValueNode.class)
      { lastSym=children[i];
        scount++;
      }
    if(scount==0) 
    { 
      node.setData(new AstValueNode(curmin)); 
      nodeinfo.lowerLim = nodeinfo.upperLim = curmin;
      return false;
    }
    if(scount==1 && !hasconst) 
    { repeat = true;
      nodeinfo.hasConst = reducerMain.reduce(lastSym).hasConst;
      node.setData(lastSym.getData()); 
      return true; 
    }

    AstNode newchildren[] = new AstNode[scount+(hasconst?1:0)];
    for(int i=0,j=0;i<children.length;++i)
      if(reducerMain.reduce(children[i]).lowerLim<nodeinfo.upperLim
          && children[i].getType()!=AstValueNode.class)
        newchildren[j++]=children[i];

    if(hasconst) newchildren[scount] = AstValueNode.create(curmin);
    nodeinfo.hasConst = hasconst;
    node.setData(new AstMinNode(newchildren));
    repeat = exRemoveRedundantMinChildren(node,nodeinfo);
    repeat = factorConstAdd(node) || repeat;
    return repeat;
  }

  // if anything in nodelist makes newnode redundant, doesn't add, returns false
  // if newnode makes anything in nodelist redundant, 
  //   that is set to null, returns true
  // if nothing is redundant, returns false, adds newnode to nodelist
  private boolean checkRedundantAndAdd(LinkedList<AstNode> nodelist,
      AstNode newnode)
  {
    ListIterator<AstNode> it = nodelist.listIterator(0);
    while(it.hasNext())
    { AstNode childi = it.next();
      if(childi==null) continue;
      MinMaxRedundancy.Result r 
        = MinMaxRedundancy.checkRedundant(childi,newnode);
      if(r == MinMaxRedundancy.Result.same || 
          r == MinMaxRedundancy.Result.small1)
        return false;
      else if(r==MinMaxRedundancy.Result.small2)
      { it.set(null);
        return true;
      }
    }
    nodelist.add(newnode);
    return false;
  }
  public int branchmax=0,branchtotal=0,branchcount=0;
  /** Tries to remove redundant arguments to AstMinNode,
      returns true iff some change was made to node. 
      For now, it basically does this:
      <code>min(add(x,1),add(x,2),y) --&gt; min(add(x,1),y)</code>
   */
  private boolean exRemoveRedundantMinChildren
    (AstNode node, AstReducer.ReduceInfo nodeinfo)
  {
    if(node.getType()!=AstMinNode.class) return false;
    AstNode minchild[] = node.children();

    int i,nullcount=0,oldcount=-1;
    HashMap<MinMaxRedundancy.HashKey,LinkedList<AstNode>> hmap
      = new HashMap<MinMaxRedundancy.HashKey,LinkedList<AstNode>>();

    if(minchild.length>branchmax) branchmax = minchild.length;
    branchtotal+=minchild.length; branchcount++;
    if(minchild.length>100000)
      System.err.println("minchild.length = "+minchild.length);
    for(i=0;i<minchild.length;++i) 
    { MinMaxRedundancy.HashKey rk = new MinMaxRedundancy.HashKey(minchild[i]);
      if(hmap.containsKey(rk)) 
        if(checkRedundantAndAdd(hmap.get(rk),minchild[i])) nullcount++;
      else
      { LinkedList<AstNode> t = new LinkedList<AstNode>();
        t.add(minchild[i]);
        hmap.put(rk,t);
      }
    }
    if(minchild.length==hmap.size()) return false;
    if(nullcount==0) return false;

    AstNode newminchild[] 
      = MinMaxRedundancy.dumpToArray(hmap,minchild.length-nullcount);
    node.setData(new AstMinNode(newminchild));

    return true;
  }


  // may change Min node to Add node
  private boolean factorConstAdd(AstNode node)
  {
    // could be extended to Max as well
    if(node.getType()!=AstMinNode.class) return false;
    AstNode[] minchild = node.children();
    int factored=Integer.MAX_VALUE;
    for(int i=0;i<minchild.length;++i)
    { if(minchild[i].getType()!=AstAddNode.class)
        return false;
      AstNode[] addchild = minchild[i].children();
      if(addchild[addchild.length-1].getType()!=AstValueNode.class)
        return false;
      int v = ((AstValueNode)addchild[addchild.length-1].getData()).getValue();
      if(v<factored) factored = v;
    }
    if(factored == 0) return false;
    /*
    AstPrinter.print(node,System.err);
    System.err.println();
    */

    // start changing things
    AstNode[] newminchild = new AstNode[minchild.length];
    for(int i=0;i<minchild.length;++i)
    { AstNode[] addchild = minchild[i].children();
      int v = ((AstValueNode)addchild[addchild.length-1].getData()).getValue();
      int j;
      AstNode[] newaddchild = new AstNode[addchild.length-(v==factored?1:0)];
      for(j=0;j<addchild.length-1;++j) newaddchild[j]=addchild[j];
      if(j<newaddchild.length)
        newaddchild[j] = AstValueNode.create(v-factored);
      if(newaddchild.length==1)
        newminchild[i] = newaddchild[0];
      else
        newminchild[i] = AstAddNode.create(newaddchild);
    }
    AstNode[] factadd = new AstNode[2];
    factadd[0]=AstMinNode.create(newminchild);
    factadd[1] = AstValueNode.create(factored);
    node.setData(new AstAddNode(factadd));
    /*
    AstPrinter.print(node,System.err);
    System.err.println("\n-----");
    */
    return true;
  }
}
