import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class AstAddReducer
{
  private AstReducer reducerMain;
  public AstAddReducer(AstReducer r) { reducerMain = r; }
  public boolean reduce(AstNode node,AstReducer.ReduceInfo nodeinfo)
  {
    boolean repeat = false;
    int scount=0, cursum=0;
    nodeinfo.upperLim = nodeinfo.lowerLim = 0;

    AstNode[] children = AstReducer.flattenedChildList(node)
                          .toArray(new AstNode[0]);
    AstNode lastSym = null;
    for(int i=0;i<children.length;++i)
    { reducerMain.reduce(children[i]);
      if(node.getType()==children[i].getType()) repeat = true;
    }
    if(repeat) return true;
    for(int i=0;i<children.length;++i)
    { if(children[i].getType()!=AstValueNode.class)
        { scount++; lastSym=children[i]; }
      else cursum+=((AstValueNode)children[i].getData()).getValue();
      AstReducer.ReduceInfo childinfo = reducerMain.reduce(children[i]);

      nodeinfo.upperLim += childinfo.upperLim;
      nodeinfo.lowerLim += childinfo.lowerLim;
    }
    if(scount==0) 
    { node.setData(new AstValueNode(cursum)); 
      nodeinfo.upperLim = nodeinfo.lowerLim = cursum;
      return false;
    }
    else if(scount==1 && cursum==0)
    { node.setData(lastSym.getData());
      repeat = true;
      nodeinfo.hasConst = reducerMain.reduce(lastSym).hasConst;
      return true;
    }

    boolean hasconst = children.length>scount && cursum!=0;
    AstNode newchildren[] = new AstNode[scount+(hasconst?1:0)];
    for(int i=0,j=0;i<children.length;++i)
      if(children[i].getType()!=AstValueNode.class)
        newchildren[j++]=children[i];

    if(hasconst) newchildren[scount] = AstValueNode.create(cursum);
    nodeinfo.hasConst = hasconst;
    node.setData(new AstAddNode(newchildren));
    // Just swapping these two will worsen optimizations
    repeat = factorAddThroughMin(node,nodeinfo) || repeat;
    //repeat = distributeConstAddThroughMin(node) || repeat;
    return repeat;
  }

  public boolean reduceTopDown(AstNode node)
  {
    return false;
    //return distributeConstAddThroughMin(node);
  }


  // add(min(add(x,1),add(y,1)),1) --> min(add(x,2),add(y,2))
  // Returns true iff we have made any modifications here
  private boolean distributeConstAddThroughMin(AstNode node)
  {
    if(node.getType()!=AstAddNode.class) return false;

    AstNode child[] = node.children();

    // assume last node is const, if at all
    if(child[child.length-1].getType()!=AstValueNode.class) return false;
    for(int i=0;i<child.length-1;++i)
    { if(child[i].getType()!=AstMinNode.class) return false;
      AstNode minchild[] = child[i].children();
      for(int j=0;j<minchild.length;++j)
      {
        if(minchild[j].getType()!=AstAddNode.class) return false;
        AstNode subaddchild[] = minchild[j].children();
        if(subaddchild[subaddchild.length-1].getType()!=AstValueNode.class)
          return false;
      }
    }

    // safe to change it now
    AstNode newchild[] = new AstNode[child.length-1];
    int incrAmount = ((AstValueNode)child[child.length-1].getData()).getValue();
    for(int i=0;i<child.length-1;++i) // for each min in add
    { AstNode minchild[] = child[i].children(); // must NOT be modified
      AstNode newminchild[] = new AstNode[minchild.length];
      // assume last node is const
      for(int j=0;j<minchild.length;++j)  // for each add in min
      { AstNode addchild[] = minchild[j].children();
        AstNode newaddchild[] = new AstNode[addchild.length];
        for(int k=0;k<addchild.length;++k) newaddchild[k]=addchild[k];
        AstNode lastchild = addchild[addchild.length-1];
        newaddchild[addchild.length-1] = AstValueNode.create(
            incrAmount + ((AstValueNode)lastchild.getData()).getValue());
        newminchild[j] = AstAddNode.create(newaddchild);
      }
      newchild[i] = AstMinNode.create(newminchild);
    }
    if(newchild.length>1) node.setData(new AstAddNode(newchild));
    else node.setData(newchild[0].getData());

    if(node.getType()==AstValueNode.class)
      assert ((AstValueNode)node.getData()).getValue()<=2000000;
    //System.err.println("Changed");
    return true;
  }


  /* Opposite of distributeConstAddThroughMin
     Factors out variable parts
     add(min(add(x,y,z),add(x,y,w)),p,q) --> add(x,y,min(z,w),p,q)
     Helps out limit checking for the terms that remain unfactored
  */
  private boolean factorAddThroughMin
    (AstNode node, AstReducer.ReduceInfo nodeinfo)
  {
    if(node.getType()!=AstAddNode.class) return false;

    boolean sthFactored = false;
    AstNode addchild[] = node.children();
    ArrayList<AstNode> newaddchild = new ArrayList<AstNode>();
    for(int i=0;i<addchild.length;++i) 
      if(addchild[i].getType()==AstMinNode.class)
      { AstNode minnode = addchild[i];
        ArrayList<AstNode> newminchild = new ArrayList<AstNode>();
        AstNode[] factored = factorAddsOut(minnode.children(),newminchild);
        if(factored==null || factored.length==0) 
        { newaddchild.add(minnode);
          continue;
        }
        for(int j=0;j<factored.length;++j) newaddchild.add(factored[j]);
        newaddchild.add(AstMinNode.create(newminchild.toArray(new AstNode[0])));
        sthFactored = true;
      }else newaddchild.add(addchild[i]);

    if(sthFactored)
      node.setData(new AstAddNode(newaddchild.toArray(new AstNode[0])));

    //System.out.println(sthFactored);
    return sthFactored;
  }



  // Takes in a bunch of addnodes, factors out the common ones, 
  //   returns the common ones, puts the rest in outnodes
  //   never factors constants
  // Returns null if all are not addnodes
  // caller may assume that addnode is not modified in any way
  private AstNode[] factorAddsOut
    (AstNode[] addnode,ArrayList<AstNode> outnodes)
  {
    int i,j;
    for(i=0;i<addnode.length;++i) if(addnode[i].getType()!=AstAddNode.class)
      return null;
    outnodes.clear();
    // eventually assigned to outnodes through AstAddNode.create
    ArrayList<ArrayList<AstNode>> rest = new ArrayList<ArrayList<AstNode>>();
    // eventual return value
    ArrayList<AstNode> isect = new ArrayList<AstNode>();
    // copy so that addnode is not modified
    for(i=0;i<addnode.length;++i) 
      rest.add(new ArrayList<AstNode>(Arrays.asList(addnode[i].children())));
    
    Comparator<AstNode> cmp = new Comparator<AstNode>(){
      public int compare(AstNode a,AstNode b)
      { boolean isAConst = a.getType()==AstValueNode.class;
        boolean isBConst = b.getType()==AstValueNode.class;
        if(a.equals(b)) return 0;
        // send constants to the end
        if(isAConst!=isBConst) return isAConst?1:-1;  
        return a.toString().compareTo(b.toString());
      }
      public boolean equals(Object o) { return this.equals(o); }
    };
    // sort all
    for(i=0;i<addnode.length;++i) java.util.Collections.sort(rest.get(i),cmp);
    // get set intersection
    int next[] = new int[addnode.length];
    boolean done = false;
    do
    { AstNode min=rest.get(0).get(next[0]);
      AstNode max=min;
      for(i=1;i<addnode.length;++i)
      { AstNode cur = rest.get(i).get(next[i]);
        if(cmp.compare(min,cur)>0) min=cur;
        if(cmp.compare(max,cur)<0) max=cur;
      }
      if(cmp.compare(min,max)==0) 
      { isect.add(min);
        for(i=0;i<addnode.length;++i) rest.get(i).set(next[i]++,null);
      }else for(i=0;i<addnode.length;++i)
        if(cmp.compare(rest.get(i).get(next[i]),max)!=0) next[i]++;

      for(i=0;i<addnode.length;++i) if(next[i]>=rest.get(i).size()) 
        { done=true; break; }
    }while(!done);

    // assign from rest to outnodes
    for(i=0;i<addnode.length;++i)
    { ArrayList<AstNode> cur = new ArrayList<AstNode>();
      for(j=0;j<rest.get(i).size();++j) if(rest.get(i).get(j)!=null)
        cur.add(rest.get(i).get(j));
      outnodes.add(AstAddNode.create(cur.toArray(new AstNode[0])));
    }
    return isect.toArray(new AstNode[0]);
  }

}
