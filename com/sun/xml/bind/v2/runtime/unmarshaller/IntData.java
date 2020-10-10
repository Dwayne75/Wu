package com.sun.xml.bind.v2.runtime.unmarshaller;

import com.sun.xml.bind.v2.runtime.output.Pcdata;
import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import java.io.IOException;

public class IntData
  extends Pcdata
{
  private int data;
  private int length;
  
  public void reset(int i)
  {
    this.data = i;
    if (i == Integer.MIN_VALUE) {
      this.length = 11;
    } else {
      this.length = (i < 0 ? stringSizeOfInt(-i) + 1 : stringSizeOfInt(i));
    }
  }
  
  private static final int[] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE };
  
  private static int stringSizeOfInt(int x)
  {
    for (int i = 0;; i++) {
      if (x <= sizeTable[i]) {
        return i + 1;
      }
    }
  }
  
  public String toString()
  {
    return String.valueOf(this.data);
  }
  
  public int length()
  {
    return this.length;
  }
  
  public char charAt(int index)
  {
    return toString().charAt(index);
  }
  
  public CharSequence subSequence(int start, int end)
  {
    return toString().substring(start, end);
  }
  
  public void writeTo(UTF8XmlOutput output)
    throws IOException
  {
    output.text(this.data);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\unmarshaller\IntData.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */