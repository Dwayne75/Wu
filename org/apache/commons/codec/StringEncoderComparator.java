package org.apache.commons.codec;

import java.util.Comparator;

public class StringEncoderComparator
  implements Comparator
{
  private final StringEncoder stringEncoder;
  
  /**
   * @deprecated
   */
  public StringEncoderComparator()
  {
    this.stringEncoder = null;
  }
  
  public StringEncoderComparator(StringEncoder stringEncoder)
  {
    this.stringEncoder = stringEncoder;
  }
  
  public int compare(Object o1, Object o2)
  {
    int compareCode = 0;
    try
    {
      Comparable s1 = (Comparable)this.stringEncoder.encode(o1);
      Comparable s2 = (Comparable)this.stringEncoder.encode(o2);
      compareCode = s1.compareTo(s2);
    }
    catch (EncoderException ee)
    {
      compareCode = 0;
    }
    return compareCode;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\commons\codec\StringEncoderComparator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */