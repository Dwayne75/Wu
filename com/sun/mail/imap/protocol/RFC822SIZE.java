package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;

public class RFC822SIZE
  implements Item
{
  static final char[] name = { 'R', 'F', 'C', '8', '2', '2', '.', 'S', 'I', 'Z', 'E' };
  public int msgno;
  public int size;
  
  public RFC822SIZE(FetchResponse r)
    throws ParsingException
  {
    this.msgno = r.getNumber();
    r.skipSpaces();
    this.size = r.readNumber();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\RFC822SIZE.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */