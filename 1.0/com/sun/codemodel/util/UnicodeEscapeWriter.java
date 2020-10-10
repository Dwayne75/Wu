package com.sun.codemodel.util;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class UnicodeEscapeWriter
  extends FilterWriter
{
  public UnicodeEscapeWriter(Writer next)
  {
    super(next);
  }
  
  public final void write(int ch)
    throws IOException
  {
    if (!requireEscaping(ch))
    {
      this.out.write(ch);
    }
    else
    {
      this.out.write("\\u");
      String s = Integer.toHexString(ch);
      for (int i = s.length(); i < 4; i++) {
        this.out.write(48);
      }
      this.out.write(s);
    }
  }
  
  protected boolean requireEscaping(int ch)
  {
    if (ch >= 128) {
      return true;
    }
    if ((ch < 32) && (" \t\r\n".indexOf(ch) == -1)) {
      return true;
    }
    return false;
  }
  
  public final void write(char[] buf, int off, int len)
    throws IOException
  {
    for (int i = 0; i < len; i++) {
      write(buf[(off + i)]);
    }
  }
  
  public final void write(char[] buf)
    throws IOException
  {
    write(buf, 0, buf.length);
  }
  
  public final void write(String buf, int off, int len)
    throws IOException
  {
    write(buf.toCharArray(), off, len);
  }
  
  public final void write(String buf)
    throws IOException
  {
    write(buf.toCharArray(), 0, buf.length());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\util\UnicodeEscapeWriter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */