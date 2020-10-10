package com.sun.mail.smtp;

import javax.mail.Session;
import javax.mail.URLName;

public class SMTPSSLTransport
  extends SMTPTransport
{
  public SMTPSSLTransport(Session session, URLName urlname)
  {
    super(session, urlname, "smtps", true);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\smtp\SMTPSSLTransport.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */