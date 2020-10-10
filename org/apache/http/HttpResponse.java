package org.apache.http;

import java.util.Locale;

public abstract interface HttpResponse
  extends HttpMessage
{
  public abstract StatusLine getStatusLine();
  
  public abstract void setStatusLine(StatusLine paramStatusLine);
  
  public abstract void setStatusLine(ProtocolVersion paramProtocolVersion, int paramInt);
  
  public abstract void setStatusLine(ProtocolVersion paramProtocolVersion, int paramInt, String paramString);
  
  public abstract void setStatusCode(int paramInt)
    throws IllegalStateException;
  
  public abstract void setReasonPhrase(String paramString)
    throws IllegalStateException;
  
  public abstract HttpEntity getEntity();
  
  public abstract void setEntity(HttpEntity paramHttpEntity);
  
  public abstract Locale getLocale();
  
  public abstract void setLocale(Locale paramLocale);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\HttpResponse.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */