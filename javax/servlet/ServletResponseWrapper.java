package javax.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class ServletResponseWrapper
  implements ServletResponse
{
  private ServletResponse response;
  
  public ServletResponseWrapper(ServletResponse response)
  {
    if (response == null) {
      throw new IllegalArgumentException("Response cannot be null");
    }
    this.response = response;
  }
  
  public ServletResponse getResponse()
  {
    return this.response;
  }
  
  public void setResponse(ServletResponse response)
  {
    if (response == null) {
      throw new IllegalArgumentException("Response cannot be null");
    }
    this.response = response;
  }
  
  public void setCharacterEncoding(String charset)
  {
    this.response.setCharacterEncoding(charset);
  }
  
  public String getCharacterEncoding()
  {
    return this.response.getCharacterEncoding();
  }
  
  public ServletOutputStream getOutputStream()
    throws IOException
  {
    return this.response.getOutputStream();
  }
  
  public PrintWriter getWriter()
    throws IOException
  {
    return this.response.getWriter();
  }
  
  public void setContentLength(int len)
  {
    this.response.setContentLength(len);
  }
  
  public void setContentType(String type)
  {
    this.response.setContentType(type);
  }
  
  public String getContentType()
  {
    return this.response.getContentType();
  }
  
  public void setBufferSize(int size)
  {
    this.response.setBufferSize(size);
  }
  
  public int getBufferSize()
  {
    return this.response.getBufferSize();
  }
  
  public void flushBuffer()
    throws IOException
  {
    this.response.flushBuffer();
  }
  
  public boolean isCommitted()
  {
    return this.response.isCommitted();
  }
  
  public void reset()
  {
    this.response.reset();
  }
  
  public void resetBuffer()
  {
    this.response.resetBuffer();
  }
  
  public void setLocale(Locale loc)
  {
    this.response.setLocale(loc);
  }
  
  public Locale getLocale()
  {
    return this.response.getLocale();
  }
  
  public boolean isWrapperFor(ServletResponse wrapped)
  {
    if (this.response == wrapped) {
      return true;
    }
    if ((this.response instanceof ServletResponseWrapper)) {
      return ((ServletResponseWrapper)this.response).isWrapperFor(wrapped);
    }
    return false;
  }
  
  public boolean isWrapperFor(Class wrappedType)
  {
    if (!ServletResponse.class.isAssignableFrom(wrappedType)) {
      throw new IllegalArgumentException("Given class " + wrappedType.getName() + " not a subinterface of " + ServletResponse.class.getName());
    }
    if (wrappedType.isAssignableFrom(this.response.getClass())) {
      return true;
    }
    if ((this.response instanceof ServletResponseWrapper)) {
      return ((ServletResponseWrapper)this.response).isWrapperFor(wrappedType);
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\ServletResponseWrapper.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */