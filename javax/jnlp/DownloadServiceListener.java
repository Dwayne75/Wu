package javax.jnlp;

import java.net.URL;

public abstract interface DownloadServiceListener
{
  public abstract void progress(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt);
  
  public abstract void validating(URL paramURL, String paramString, long paramLong1, long paramLong2, int paramInt);
  
  public abstract void upgradingArchive(URL paramURL, String paramString, int paramInt1, int paramInt2);
  
  public abstract void downloadFailed(URL paramURL, String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\jnlp\DownloadServiceListener.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */