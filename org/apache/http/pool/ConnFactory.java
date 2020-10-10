package org.apache.http.pool;

import java.io.IOException;

public abstract interface ConnFactory<T, C>
{
  public abstract C create(T paramT)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\pool\ConnFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */