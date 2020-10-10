package com.sun.mail.imap.protocol;

import com.sun.mail.iap.Protocol;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.util.ASCIIUtility;
import java.io.IOException;
import java.util.Vector;

public class IMAPResponse
  extends Response
{
  private String key;
  private int number;
  
  public IMAPResponse(Protocol c)
    throws IOException, ProtocolException
  {
    super(c);
    init();
  }
  
  private void init()
    throws IOException, ProtocolException
  {
    if ((isUnTagged()) && (!isOK()) && (!isNO()) && (!isBAD()) && (!isBYE()))
    {
      this.key = readAtom();
      try
      {
        this.number = Integer.parseInt(this.key);
        this.key = readAtom();
      }
      catch (NumberFormatException ne) {}
    }
  }
  
  public IMAPResponse(IMAPResponse r)
  {
    super(r);
    this.key = r.key;
    this.number = r.number;
  }
  
  public IMAPResponse(String r)
    throws IOException, ProtocolException
  {
    super(r);
    init();
  }
  
  public String[] readSimpleList()
  {
    skipSpaces();
    if (this.buffer[this.index] != 40) {
      return null;
    }
    this.index += 1;
    
    Vector v = new Vector();
    for (int start = this.index; this.buffer[this.index] != 41; this.index += 1) {
      if (this.buffer[this.index] == 32)
      {
        v.addElement(ASCIIUtility.toString(this.buffer, start, this.index));
        start = this.index + 1;
      }
    }
    if (this.index > start) {
      v.addElement(ASCIIUtility.toString(this.buffer, start, this.index));
    }
    this.index += 1;
    
    int size = v.size();
    if (size > 0)
    {
      String[] s = new String[size];
      v.copyInto(s);
      return s;
    }
    return null;
  }
  
  public String getKey()
  {
    return this.key;
  }
  
  public boolean keyEquals(String k)
  {
    if ((this.key != null) && (this.key.equalsIgnoreCase(k))) {
      return true;
    }
    return false;
  }
  
  public int getNumber()
  {
    return this.number;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\IMAPResponse.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */