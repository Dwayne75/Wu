package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ByteArray;
import com.sun.mail.iap.ParsingException;
import java.io.ByteArrayInputStream;

public class RFC822DATA
  implements Item
{
  static final char[] name = { 'R', 'F', 'C', '8', '2', '2' };
  public int msgno;
  public ByteArray data;
  
  public RFC822DATA(FetchResponse r)
    throws ParsingException
  {
    this.msgno = r.getNumber();
    r.skipSpaces();
    this.data = r.readByteArray();
  }
  
  public ByteArray getByteArray()
  {
    return this.data;
  }
  
  public ByteArrayInputStream getByteArrayInputStream()
  {
    if (this.data != null) {
      return this.data.toByteArrayInputStream();
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\RFC822DATA.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */