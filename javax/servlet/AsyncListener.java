package javax.servlet;

import java.io.IOException;
import java.util.EventListener;

public abstract interface AsyncListener
  extends EventListener
{
  public abstract void onComplete(AsyncEvent paramAsyncEvent)
    throws IOException;
  
  public abstract void onTimeout(AsyncEvent paramAsyncEvent)
    throws IOException;
  
  public abstract void onError(AsyncEvent paramAsyncEvent)
    throws IOException;
  
  public abstract void onStartAsync(AsyncEvent paramAsyncEvent)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\AsyncListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */