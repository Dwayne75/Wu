package org.apache.http.conn.ssl;

import javax.net.ssl.SSLException;
import org.apache.http.annotation.Immutable;

@Immutable
public class StrictHostnameVerifier
  extends AbstractVerifier
{
  public final void verify(String host, String[] cns, String[] subjectAlts)
    throws SSLException
  {
    verify(host, cns, subjectAlts, true);
  }
  
  public final String toString()
  {
    return "STRICT";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\conn\ssl\StrictHostnameVerifier.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */