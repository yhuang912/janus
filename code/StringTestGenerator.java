import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Random;

// Utility class for testing various string algorithms
public class StringTestGenerator
{
  public interface helper
  {
    void useTest(char[] testA, char[] testB, float datalabel,
        PrintStream printStream);
  }

  private int defaultlen = 500;
  private float secretPercentLo = 0;
  private float secretPercentHi = 100;
  private float secretPercentStep = 1f;
  private helper testUser;
  private Random random = new Random();

  public StringTestGenerator(helper hper) { testUser = hper; }

  public helper getTestUser() { return testUser; }
  public void setTestUser(helper x) { testUser=x; }
  public int  getStringLen() { return defaultlen; }
  public void setStringLen(int x) { defaultlen=x; }
  public float getSecretPercentLo() { return secretPercentLo; }
  public void  setSecretPercentLo(int x) { secretPercentLo=x; }
  public float getSecretPercentHi() { return secretPercentHi; }
  public void  getSecretPercentHi(int x) { secretPercentHi=x; }
  private float getSecretPercentStep() { return secretPercentStep; }
  private void  getSecretPercentStep(int x) { secretPercentStep=x; }

  private char[] randomString(int len,int charKind)
  { char[] rv = new char[len];
    for(int i=0;i<len;++i)
      rv[i] = (char)(random.nextInt(charKind)+'A');
    return rv;
  }
  public void singleMutationTest
    (Eraser eraser, PrintStream printStream,float mutationRate)
  {
    final int len = defaultlen;
    final float lo=secretPercentLo, hi=secretPercentHi, step=secretPercentStep;

    for(float secretPercent=lo;secretPercent<=hi;secretPercent+=step)
    {
      char[] testA = randomString(len,4);
      char[] testB = new char[len];
      for(int i=0;i<len;++i)
      { if(random.nextFloat()<mutationRate) 
          testB[i]=(char)(random.nextInt(4)+'A');
        else testB[i]=testA[i];
      }
      eraser.erase(testA,secretPercent);
      eraser.erase(testB,secretPercent);
      testUser.useTest(testA,testB,secretPercent,printStream);
    }
  }
  public void singleBlockMutationTest
    (Eraser eraser, PrintStream printStream, float mutationRate)
  {
    final int len = defaultlen;
    final float lo=secretPercentLo, hi=secretPercentHi, step=secretPercentStep;
    for(float secretPercent=lo;secretPercent<=hi;secretPercent+=step)
    {
      char[] testA = randomString(len,4);
      char[] testB = new char[len];
      int chlen = (int)(len*mutationRate);
      int chst = random.nextInt(len-chlen+1);
      for(int i=0;i<testB.length;++i) testB[i]=testA[i];
      for(int i=chst;i<chst+chlen;++i) testB[i]=(char)(random.nextInt(4)+'A');
      eraser.erase(testA,secretPercent);
      eraser.erase(testB,secretPercent);
      testUser.useTest(testA,testB,secretPercent,printStream);
    }
  }
  public void timeSingleMutation(Eraser eraser, PrintStream printStream,
      float mutationRate)
  {
    final float secretPercent = 10;
    final float lo=secretPercentLo, hi=secretPercentHi, step=secretPercentStep;
    for(int len = 10;len<=400;len+=4)
    {
      char[] testA = randomString(len,4);
      char[] testB = new char[len];
      for(int i=0;i<len;++i)
      { if(random.nextFloat()<mutationRate) 
          testB[i]=(char)(random.nextInt(4)+'A');
        else testB[i]=testA[i];
      }
      eraser.erase(testA,secretPercent);
      eraser.erase(testB,secretPercent);
      testUser.useTest(testA,testB,len,printStream);
    }
  }
  public void randomInsDel(Eraser eraser, PrintStream printStream,
      float mutationRate, float avgInsDelLen)
  {
    final int len = defaultlen;
    final int charsMutated = (int) (mutationRate * 2 * len);
    final int mutationCount = (int) (charsMutated/avgInsDelLen);
    assert mutationCount!=0 || charsMutated==0 
      : "avgInsDelLen is too long for given mutationRate";
    /*
    System.out.println("charsMutated = "+charsMutated+
        ", mutationCount = "+mutationCount);
        */
    // TODO better asserts here

    final float lo=secretPercentLo, hi=secretPercentHi, step=secretPercentStep;

    for(float secretPercent=lo;secretPercent<=hi;secretPercent+=step)
    { char[] testA = randomString(len,4);
      char[] testB = randomString(len,4);
      int[] delLenA = nonZeroRandomPartitions(charsMutated,mutationCount/2);
      int[] delLenB = nonZeroRandomPartitions(charsMutated,mutationCount/2);
      index2Len(charsMutated,delLenA);
      index2Len(charsMutated,delLenB);
      for(int i=0;i<mutationCount/2-1;++i) // we may have a tail of large sub
      { int r=random.nextInt(6);
        if(r<2)  // zap delLenA[i], prob == 1/3
        { delLenA[i+1]+=delLenA[i];
          delLenA[i]=0;
        }
        else if(r<4) // zap delLenB[i], prob == 1/3
        { delLenB[i+1]+=delLenB[i];
          delLenB[i]=0;
        }
      }
      /*
      for(int i=0;i<delLenA.length;++i)
        System.out.print(delLenA[i]+" ");
      System.out.println("");
      for(int i=0;i<delLenB.length;++i)
        System.out.print(delLenB[i]+" ");
      System.out.println("");
      */
      int[] unchangedLens = nonZeroRandomPartitions(len-charsMutated,
                              mutationCount/2+1);
      index2Len(len-charsMutated,unchangedLens);
      int ja=0,jb=0;
      for(int k=0;k<unchangedLens.length;++k)
      { for(int k2=0;k2<unchangedLens[k];++k2)
          testB[k2+jb]=testA[k2+ja];
        if(k!=unchangedLens.length-1) 
        { ja+=delLenA[k]+unchangedLens[k]; 
          jb+=delLenB[k]+unchangedLens[k]; 
        }
      }
      eraser.erase(testA,secretPercent);
      eraser.erase(testB,secretPercent);
      testUser.useTest(testA,testB,secretPercent,printStream);
    }

  }

  public void shufflerTest(Eraser eraser, PrintStream printStream,
      float mutationRate)
  {
    final int len = defaultlen;
    final float lo=secretPercentLo, hi=secretPercentHi, step=secretPercentStep;

    for(float secretPercent=lo;secretPercent<=hi;secretPercent+=step)
    {
      char[] testA = randomString(len,4);
      char[] testB = new char[len];
      int i=0,j,k;
      do
      { j = random.nextInt(len-i)+i+1;
        k = random.nextInt(j+1-i)+i;
        for(int l=i;l<k;++l) testB[j-k+l]=testA[l];
        for(int l=k;l<j;++l) testB[i+l-k]=testA[l];
        i=j;
      }while(i<len);
      eraser.erase(testA,secretPercent);
      eraser.erase(testB,secretPercent);
      testUser.useTest(testA,testB,secretPercent,printStream);
    }
  }
  /* Randomly partitions the set of integers in [0,len] into
     partitionCount partitions, and returns the indices where partitions start.
     First return value is always zero */
  private int[] nonZeroRandomPartitions(int len,int partitionCount)
  { int rv[] = new int[partitionCount];
    for(int i=1;i<partitionCount;++i)
      rv[i]=random.nextInt(len-partitionCount+1);
    java.util.Arrays.sort(rv);
    for(int i=0;i<partitionCount;++i) rv[i]+=i;
    return rv;
  }
  /* same as the previous method, but allows zero-length partitions */
  private int[] withZeroRandomPartitions(int len,int partitionCount)
  { int rv[] = new int[partitionCount];
    for(int i=1;i<partitionCount;++i)
      rv[i]=random.nextInt(len+1);
    java.util.Arrays.sort(rv);
    return rv;
  }
  private static void index2Len(int total,int index[])
  {
    for(int i=index.length-1;i>=0;--i)
    { int temp = total;
      total=index[i];
      index[i]=temp-index[i];
    }
  }


  public interface Eraser 
    { public void erase(char[] data,float percent); }

  public final Eraser scatterEraser = new Eraser(){
    public void erase(char[] data,float percent)
    {
      assert percent>=0 && percent<=100;
      int eraseCount = (int) (data.length*percent/100);
      for(int i=data.length-1;i>=0;--i)
        if(random.nextInt(i+1)<eraseCount) 
        { data[i]='#';
          --eraseCount;
        }
    }
  };

  public final Eraser singleBlockEraser = new Eraser(){
    public void erase(char[] data,float percent)
    {
      assert percent>=0 && percent<=100;
      int eraseCount = (int) (data.length*percent/100);
      int start = random.nextInt(data.length-eraseCount+1);
      for(int i=start;i<start+eraseCount;++i) data[i]='#';
    }
  };

  public Eraser nBlockEraser(final int n)
  {
    return new Eraser(){
      public void erase(char[] data,float percent)
      {
        assert percent>=0 && percent<=100;
        int eraseCount = (int)(data.length*percent/100);
        int eraseLens[] = withZeroRandomPartitions(eraseCount,n);
        index2Len(eraseCount,eraseLens);
        int origLens[] = withZeroRandomPartitions(data.length-eraseCount,n+1);
        index2Len(data.length-eraseCount,origLens);
        int start=0;
        for(int i=0;i<eraseLens.length;++i)
        { for(int j=0;j<eraseLens[i];++j)
            data[start+origLens[i]+j]='#';
          if(i<eraseLens.length) start+=eraseLens[i]+origLens[i];
        }
      }
    };
  }
}
