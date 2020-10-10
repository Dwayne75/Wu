package com.sun.mail.handlers;

import java.awt.datatransfer.DataFlavor;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;

public class text_plain
  implements DataContentHandler
{
  private static ActivationDataFlavor myDF = new ActivationDataFlavor(String.class, "text/plain", "Text String");
  
  private static class NoCloseOutputStream
    extends FilterOutputStream
  {
    public NoCloseOutputStream(OutputStream os)
    {
      super();
    }
    
    public void close() {}
  }
  
  protected ActivationDataFlavor getDF()
  {
    return myDF;
  }
  
  public DataFlavor[] getTransferDataFlavors()
  {
    return new DataFlavor[] { getDF() };
  }
  
  public Object getTransferData(DataFlavor df, DataSource ds)
    throws IOException
  {
    if (getDF().equals(df)) {
      return getContent(ds);
    }
    return null;
  }
  
  public Object getContent(DataSource ds)
    throws IOException
  {
    String enc = null;
    InputStreamReader is = null;
    try
    {
      enc = getCharset(ds.getContentType());
      is = new InputStreamReader(ds.getInputStream(), enc);
    }
    catch (IllegalArgumentException iex)
    {
      throw new UnsupportedEncodingException(enc);
    }
    try
    {
      int pos = 0;
      
      char[] buf = new char['Ѐ'];
      int count;
      int size;
      while ((count = is.read(buf, pos, buf.length - pos)) != -1)
      {
        pos += count;
        if (pos >= buf.length)
        {
          size = buf.length;
          if (size < 262144) {
            size += size;
          } else {
            size += 262144;
          }
          char[] tbuf = new char[size];
          System.arraycopy(buf, 0, tbuf, 0, pos);
          buf = tbuf;
        }
      }
      return new String(buf, 0, pos);
    }
    finally
    {
      try
      {
        is.close();
      }
      catch (IOException ex) {}
    }
  }
  
  public void writeTo(Object obj, String type, OutputStream os)
    throws IOException
  {
    if (!(obj instanceof String)) {
      throw new IOException("\"" + getDF().getMimeType() + "\" DataContentHandler requires String object, " + "was given object of type " + obj.getClass().toString());
    }
    String enc = null;
    OutputStreamWriter osw = null;
    try
    {
      enc = getCharset(type);
      osw = new OutputStreamWriter(new NoCloseOutputStream(os), enc);
    }
    catch (IllegalArgumentException iex)
    {
      throw new UnsupportedEncodingException(enc);
    }
    String s = (String)obj;
    osw.write(s, 0, s.length());
    
    osw.close();
  }
  
  private String getCharset(String type)
  {
    try
    {
      ContentType ct = new ContentType(type);
      String charset = ct.getParameter("charset");
      if (charset == null) {
        charset = "us-ascii";
      }
      return MimeUtility.javaCharset(charset);
    }
    catch (Exception ex) {}
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\handlers\text_plain.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */