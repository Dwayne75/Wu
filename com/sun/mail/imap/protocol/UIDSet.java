package com.sun.mail.imap.protocol;

import java.util.Vector;

public class UIDSet
{
  public long start;
  public long end;
  
  public UIDSet() {}
  
  public UIDSet(long start, long end)
  {
    this.start = start;
    this.end = end;
  }
  
  public long size()
  {
    return this.end - this.start + 1L;
  }
  
  public static UIDSet[] createUIDSets(long[] msgs)
  {
    Vector v = new Vector();
    for (int i = 0; i < msgs.length; i++)
    {
      UIDSet ms = new UIDSet();
      ms.start = msgs[i];
      for (int j = i + 1; j < msgs.length; j++) {
        if (msgs[j] != msgs[(j - 1)] + 1L) {
          break;
        }
      }
      ms.end = msgs[(j - 1)];
      v.addElement(ms);
      i = j - 1;
    }
    UIDSet[] msgsets = new UIDSet[v.size()];
    v.copyInto(msgsets);
    return msgsets;
  }
  
  public static String toString(UIDSet[] msgsets)
  {
    if ((msgsets == null) || (msgsets.length == 0)) {
      return null;
    }
    int i = 0;
    StringBuffer s = new StringBuffer();
    int size = msgsets.length;
    for (;;)
    {
      long start = msgsets[i].start;
      long end = msgsets[i].end;
      if (end > start) {
        s.append(start).append(':').append(end);
      } else {
        s.append(start);
      }
      i++;
      if (i >= size) {
        break;
      }
      s.append(',');
    }
    return s.toString();
  }
  
  public static long size(UIDSet[] msgsets)
  {
    long count = 0L;
    if (msgsets == null) {
      return 0L;
    }
    for (int i = 0; i < msgsets.length; i++) {
      count += msgsets[i].size();
    }
    return count;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\UIDSet.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */