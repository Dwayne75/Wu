package com.sun.mail.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CRLFOutputStream
  extends FilterOutputStream
{
  protected int lastb = -1;
  protected boolean atBOL = true;
  private static final byte[] newline = { 13, 10 };
  
  public CRLFOutputStream(OutputStream os)
  {
    super(os);
  }
  
  public void write(int b)
    throws IOException
  {
    if (b == 13)
    {
      writeln();
    }
    else if (b == 10)
    {
      if (this.lastb != 13) {
        writeln();
      }
    }
    else
    {
      this.out.write(b);
      this.atBOL = false;
    }
    this.lastb = b;
  }
  
  public void write(byte[] b)
    throws IOException
  {
    write(b, 0, b.length);
  }
  
  public void write(byte[] b, int off, int len)
    throws IOException
  {
    int start = off;
    
    len += off;
    for (int i = start; i < len; i++)
    {
      if (b[i] == 13)
      {
        this.out.write(b, start, i - start);
        writeln();
        start = i + 1;
      }
      else if (b[i] == 10)
      {
        if (this.lastb != 13)
        {
          this.out.write(b, start, i - start);
          writeln();
        }
        start = i + 1;
      }
      this.lastb = b[i];
    }
    if (len - start > 0)
    {
      this.out.write(b, start, len - start);
      this.atBOL = false;
    }
  }
  
  public void writeln()
    throws IOException
  {
    this.out.write(newline);
    this.atBOL = true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\CRLFOutputStream.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */