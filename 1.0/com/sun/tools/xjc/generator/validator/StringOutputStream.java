package com.sun.tools.xjc.generator.validator;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class StringOutputStream
  extends OutputStream
{
  private final Writer writer;
  
  public StringOutputStream(Writer _writer)
  {
    this.writer = _writer;
  }
  
  public void write(int ch)
    throws IOException
  {
    this.writer.write(ch);
  }
  
  public void write(byte[] data)
    throws IOException
  {
    write(data, 0, data.length);
  }
  
  public void write(byte[] data, int start, int len)
    throws IOException
  {
    char[] buf = new char[len];
    for (int i = 0; i < len; i++) {
      buf[i] = ((char)(data[(i + start)] & 0xFF));
    }
    this.writer.write(buf);
  }
  
  public void close()
    throws IOException
  {
    this.writer.close();
  }
  
  public void flush()
    throws IOException
  {
    this.writer.flush();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\validator\StringOutputStream.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */