package com.sun.mail.imap.protocol;

import java.util.Vector;

public class MessageSet
{
  public int start;
  public int end;
  
  public MessageSet() {}
  
  public MessageSet(int start, int end)
  {
    this.start = start;
    this.end = end;
  }
  
  public int size()
  {
    return this.end - this.start + 1;
  }
  
  public static MessageSet[] createMessageSets(int[] msgs)
  {
    Vector v = new Vector();
    for (int i = 0; i < msgs.length; i++)
    {
      MessageSet ms = new MessageSet();
      ms.start = msgs[i];
      for (int j = i + 1; j < msgs.length; j++) {
        if (msgs[j] != msgs[(j - 1)] + 1) {
          break;
        }
      }
      ms.end = msgs[(j - 1)];
      v.addElement(ms);
      i = j - 1;
    }
    MessageSet[] msgsets = new MessageSet[v.size()];
    v.copyInto(msgsets);
    return msgsets;
  }
  
  public static String toString(MessageSet[] msgsets)
  {
    if ((msgsets == null) || (msgsets.length == 0)) {
      return null;
    }
    int i = 0;
    StringBuffer s = new StringBuffer();
    int size = msgsets.length;
    for (;;)
    {
      int start = msgsets[i].start;
      int end = msgsets[i].end;
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
  
  public static int size(MessageSet[] msgsets)
  {
    int count = 0;
    if (msgsets == null) {
      return 0;
    }
    for (int i = 0; i < msgsets.length; i++) {
      count += msgsets[i].size();
    }
    return count;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\MessageSet.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */