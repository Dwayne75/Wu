package com.sun.tools.xjc.util;

import java.io.PrintStream;

public class EditDistance
{
  private int[] cost;
  private int[] back;
  private final String a;
  private final String b;
  
  public static int editDistance(String a, String b)
  {
    return new EditDistance(a, b).calc();
  }
  
  public static String findNearest(String key, String[] group)
  {
    int c = Integer.MAX_VALUE;
    String r = null;
    for (int i = 0; i < group.length; i++)
    {
      int ed = editDistance(key, group[i]);
      if (c > ed)
      {
        c = ed;
        r = group[i];
      }
    }
    return r;
  }
  
  private EditDistance(String a, String b)
  {
    this.a = a;
    this.b = b;
    this.cost = new int[a.length() + 1];
    this.back = new int[a.length() + 1];
    for (int i = 0; i <= a.length(); i++) {
      this.cost[i] = i;
    }
  }
  
  private void flip()
  {
    int[] t = this.cost;
    this.cost = this.back;
    this.back = t;
  }
  
  private int min(int a, int b, int c)
  {
    return Math.min(a, Math.min(b, c));
  }
  
  private int calc()
  {
    for (int j = 0; j < this.b.length(); j++)
    {
      flip();
      this.cost[0] = (j + 1);
      for (int i = 0; i < this.a.length(); i++)
      {
        int match = this.a.charAt(i) == this.b.charAt(j) ? 0 : 1;
        this.cost[(i + 1)] = min(this.back[i] + match, this.cost[i] + 1, this.back[(i + 1)] + 1);
      }
    }
    return this.cost[this.a.length()];
  }
  
  public static void main(String[] args)
  {
    System.out.println(editDistance(args[0], args[1]));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\util\EditDistance.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */