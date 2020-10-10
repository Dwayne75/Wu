package com.sun.xml.bind.v2.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.xml.transform.stream.StreamSource;

public final class DataSourceSource
  extends StreamSource
{
  private final DataSource source;
  private final String charset;
  private Reader r;
  private InputStream is;
  
  public DataSourceSource(DataHandler dh)
    throws MimeTypeParseException
  {
    this(dh.getDataSource());
  }
  
  public DataSourceSource(DataSource source)
    throws MimeTypeParseException
  {
    this.source = source;
    
    String ct = source.getContentType();
    if (ct == null)
    {
      this.charset = null;
    }
    else
    {
      MimeType mimeType = new MimeType(ct);
      this.charset = mimeType.getParameter("charset");
    }
  }
  
  public void setReader(Reader reader)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setInputStream(InputStream inputStream)
  {
    throw new UnsupportedOperationException();
  }
  
  public Reader getReader()
  {
    try
    {
      if (this.charset == null) {
        return null;
      }
      if (this.r == null) {
        this.r = new InputStreamReader(this.source.getInputStream(), this.charset);
      }
      return this.r;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public InputStream getInputStream()
  {
    try
    {
      if (this.charset != null) {
        return null;
      }
      if (this.is == null) {
        this.is = this.source.getInputStream();
      }
      return this.is;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }
  
  public DataSource getDataSource()
  {
    return this.source;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\util\DataSourceSource.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */