package com.sun.tools.xjc.util;

import java.io.IOException;
import java.io.OutputStream;

public class NullStream
  extends OutputStream
{
  public void write(int b)
    throws IOException
  {}
  
  public void close()
    throws IOException
  {}
  
  public void flush()
    throws IOException
  {}
  
  public void write(byte[] b, int off, int len)
    throws IOException
  {}
  
  public void write(byte[] b)
    throws IOException
  {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\util\NullStream.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */