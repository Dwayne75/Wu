package com.sun.mail.imap;

import javax.mail.Session;
import javax.mail.URLName;

public class IMAPSSLStore
  extends IMAPStore
{
  public IMAPSSLStore(Session session, URLName url)
  {
    super(session, url, "imaps", true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\IMAPSSLStore.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */