package org.apache.http.concurrent;

public abstract interface FutureCallback<T>
{
  public abstract void completed(T paramT);
  
  public abstract void failed(Exception paramException);
  
  public abstract void cancelled();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\concurrent\FutureCallback.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */