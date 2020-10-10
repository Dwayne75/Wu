package javax.mail;

import java.io.IOException;
import java.io.InputStream;

abstract interface StreamLoader
{
  public abstract void load(InputStream paramInputStream)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\StreamLoader.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */