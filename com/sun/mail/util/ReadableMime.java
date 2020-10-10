package com.sun.mail.util;

import java.io.InputStream;
import javax.mail.MessagingException;

public abstract interface ReadableMime
{
  public abstract InputStream getMimeStream()
    throws MessagingException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\ReadableMime.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */