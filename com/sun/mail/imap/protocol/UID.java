package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ParsingException;

public class UID
  implements Item
{
  static final char[] name = { 'U', 'I', 'D' };
  public int seqnum;
  public long uid;
  
  public UID(FetchResponse r)
    throws ParsingException
  {
    this.seqnum = r.getNumber();
    r.skipSpaces();
    this.uid = r.readLong();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\UID.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */