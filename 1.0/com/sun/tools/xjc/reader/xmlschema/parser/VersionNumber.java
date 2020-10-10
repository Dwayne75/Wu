package com.sun.tools.xjc.reader.xmlschema.parser;

import java.util.StringTokenizer;

public class VersionNumber
  implements Comparable
{
  private final int[] digits;
  
  public VersionNumber(String num)
  {
    StringTokenizer tokens = new StringTokenizer(num, ".");
    this.digits = new int[tokens.countTokens()];
    if (this.digits.length < 2) {
      throw new IllegalArgumentException();
    }
    int i = 0;
    while (tokens.hasMoreTokens()) {
      this.digits[(i++)] = Integer.parseInt(tokens.nextToken());
    }
  }
  
  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < this.digits.length; i++)
    {
      if (i != 0) {
        buf.append('.');
      }
      buf.append(Integer.toString(this.digits[i]));
    }
    return buf.toString();
  }
  
  public boolean isOlderThan(VersionNumber rhs)
  {
    return compareTo(rhs) < 0;
  }
  
  public boolean isNewerThan(VersionNumber rhs)
  {
    return compareTo(rhs) > 0;
  }
  
  public boolean equals(Object o)
  {
    return compareTo(o) == 0;
  }
  
  public int hashCode()
  {
    int x = 0;
    for (int i = 0; i < this.digits.length; i++) {
      x = x << 1 | this.digits[i];
    }
    return x;
  }
  
  public int compareTo(Object o)
  {
    VersionNumber rhs = (VersionNumber)o;
    for (int i = 0;; i++)
    {
      if ((i == this.digits.length) && (i == rhs.digits.length)) {
        return 0;
      }
      if (i == this.digits.length) {
        return -1;
      }
      if (i == rhs.digits.length) {
        return 1;
      }
      int r = this.digits[i] - rhs.digits[i];
      if (r != 0) {
        return r;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\parser\VersionNumber.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */