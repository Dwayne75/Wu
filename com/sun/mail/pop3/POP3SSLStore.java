package com.sun.mail.pop3;

import javax.mail.Session;
import javax.mail.URLName;

public class POP3SSLStore
  extends POP3Store
{
  public POP3SSLStore(Session session, URLName url)
  {
    super(session, url, "pop3s", true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\pop3\POP3SSLStore.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */