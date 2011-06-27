import java.util.Scanner;

public class SmithWaterman
{
  private String a,b;
  private int insDelCost[];
  private AstNode root;

  public SmithWaterman(String a,String b,int insDelCost[])
  {
    assert a.length()<=insDelCost.length && b.length()<=insDelCost.length
      : "SmithWaterman insDelCost parameter is too short";
    this.a = a; this.b = b; this.insDelCost = insDelCost;

    AstNode mat[][] = new AstNode[a.length()+1][b.length()+1];
    AstReducer reducer = new AstReducer();

    mat[0][0] = AstValueNode.create(0);
    for(int i=0;i<a.length();++i) mat[i+1][0] = AstValueNode.create(0);
    for(int j=0;j<b.length();++j) mat[0][j+1] = AstValueNode.create(0);
    for(int i=0;i<a.length();++i) for(int j=0;j<b.length();++j)
    {
      AstNode maxparam[] = new AstNode[i+j+4];
      AstNode addparam[];

      int k,l=0;
      
      maxparam[l++]=AstValueNode.create(0);

      addparam = new AstNode[2];
      addparam[0]=mat[i][j];
      addparam[1]=AstSWSimilarityNode.create(a,i,b,j);
      maxparam[l++] = AstAddNode.create(addparam);

      for(k=0;k<=i;++k)
      { addparam = new AstNode[2];
        addparam[0] = mat[k][j+1];
        addparam[1] = AstValueNode.create(-insDelCost[i-k]);
        maxparam[l++] = AstAddNode.create(addparam);
      }
      for(k=0;k<=j;++k)
      { addparam = new AstNode[2];
        addparam[0] = mat[i+1][k];
        addparam[1] = AstValueNode.create(-insDelCost[j-k]);
        maxparam[l++] = AstAddNode.create(addparam);
      }
      mat[i+1][j+1] = AstMaxNode.create(maxparam);
      reducer.reduce(mat[i+1][j+1]);
    }
    root = mat[a.length()][b.length()];
  }

  public AstNode getRoot() { return root; }
  public String getInputA() { return a; }
  public String getInputB() { return b; }

  public static void main(String args[])
  {
    Scanner scanner = new Scanner(System.in);
    String a = scanner.nextLine(), b = scanner.nextLine();

    int cost[] = new int[a.length()>b.length()?a.length():b.length()];
    for(int i=0;i<cost.length;++i) cost[i]=i+1;

    SmithWaterman sw = new SmithWaterman(a,b,cost);
    AstPrinter.print(sw.getRoot(),System.out);
  }
}
