package test;

import java.io.PrintStream;

import test.StringTestGenerator.Eraser;
import test.StringTestGenerator.helper;

import ast.AstNodeCounter;

public class EditDistanceTest
{
  private static StringTestGenerator.helper timePrinter 
    = new StringTestGenerator.helper(){
    public void useTest(char[] testA,char[] testB, float dataLabel,
        PrintStream printStream)
    {
      printStream.println("A: "+new String(testA));
      printStream.println("B: "+new String(testB));
      long start = System.nanoTime(), end;
      EditDistance ed = new EditDistance(new String(testA),new String(testB));
      end = System.nanoTime();
      printStream.println(dataLabel+": "+(end-start)*1e-6);
    }
  };
  private static StringTestGenerator.helper testPrinter 
    = new StringTestGenerator.helper(){
    public void useTest(char[] testA,char[] testB, float dataLabel,
        PrintStream printStream)
    {
      printStream.println("A: "+new String(testA));
      printStream.println("B: "+new String(testB));
      EditDistance ed = new EditDistance(new String(testA),new String(testB));
      //AstPrinter.print(ed.getRoot(),printStream);
      //printStream.println("");
      printStream.println(dataLabel+": "+AstNodeCounter.count(ed.getRoot()));
    }
  };
  private static StringTestGenerator gen = new StringTestGenerator(testPrinter);
  private static StringTestGenerator.Eraser curEraser = gen.nBlockEraser(1);

  public static void parseCommandLine(String[] args)
  { for(int i=0;i<args.length;++i) 
    { if(args[i].startsWith("-deflen="))
        gen.setStringLen(
            Integer.parseInt(args[i].substring("-deflen=".length())));
      if(args[i].equals("-scatrErase"))
        curEraser = gen.scatterEraser;
      if(args[i].startsWith("-nblockErase="))
        curEraser = gen.nBlockEraser(
            Integer.parseInt(args[i].substring("-nblockErase=".length())));
    }
  }

  public static void main(String[] args)
  {
    parseCommandLine(args);
    //testUser = new EditDistanceVerify();
    System.out.println("\n\nScatter subs\n--------------------");
    gen.singleMutationTest(curEraser,System.out,.1f);
    System.out.println("\n\nSingle block test\n---------------------");
    gen.singleBlockMutationTest(curEraser,System.out,.1f);
    System.out.println("\n\nShuffler test\n---------------------");
    gen.shufflerTest(curEraser,System.out,.1f);
    System.out.println("\n\nRandom Inserts and Deletes");
    System.out.println("-------------------------------");
    gen.randomInsDel(curEraser,System.out,.2f,5);
    /*
    testUser = timePrinter;
    System.out.println("\n\nTiming results\n--------------------");
    gen.timeSingleMutation(curEraser,System.out,.1f);
    */
  }

}
