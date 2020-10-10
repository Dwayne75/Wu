package org.apache.http.conn;

import java.io.IOException;
import java.net.Socket;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.HttpInetConnection;
import org.apache.http.params.HttpParams;

public abstract interface OperatedClientConnection
  extends HttpClientConnection, HttpInetConnection
{
  public abstract HttpHost getTargetHost();
  
  public abstract boolean isSecure();
  
  public abstract Socket getSocket();
  
  public abstract void opening(Socket paramSocket, HttpHost paramHttpHost)
    throws IOException;
  
  public abstract void openCompleted(boolean paramBoolean, HttpParams paramHttpParams)
    throws IOException;
  
  public abstract void update(Socket paramSocket, HttpHost paramHttpHost, boolean paramBoolean, HttpParams paramHttpParams)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\conn\OperatedClientConnection.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */