package javax.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public abstract interface ServletResponse
{
  public abstract String getCharacterEncoding();
  
  public abstract String getContentType();
  
  public abstract ServletOutputStream getOutputStream()
    throws IOException;
  
  public abstract PrintWriter getWriter()
    throws IOException;
  
  public abstract void setCharacterEncoding(String paramString);
  
  public abstract void setContentLength(int paramInt);
  
  public abstract void setContentType(String paramString);
  
  public abstract void setBufferSize(int paramInt);
  
  public abstract int getBufferSize();
  
  public abstract void flushBuffer()
    throws IOException;
  
  public abstract void resetBuffer();
  
  public abstract boolean isCommitted();
  
  public abstract void reset();
  
  public abstract void setLocale(Locale paramLocale);
  
  public abstract Locale getLocale();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletResponse.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */