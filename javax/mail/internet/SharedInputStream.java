package javax.mail.internet;

import java.io.InputStream;

public abstract interface SharedInputStream
{
  public abstract long getPosition();
  
  public abstract InputStream newStream(long paramLong1, long paramLong2);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\internet\SharedInputStream.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */