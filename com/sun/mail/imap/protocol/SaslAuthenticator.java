package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ProtocolException;

public abstract interface SaslAuthenticator
{
  public abstract boolean authenticate(String[] paramArrayOfString, String paramString1, String paramString2, String paramString3, String paramString4)
    throws ProtocolException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\protocol\SaslAuthenticator.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */