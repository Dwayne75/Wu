package org.flywaydb.core.internal.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class FileCopyUtils
{
  public static String copyToString(Reader in)
    throws IOException
  {
    StringWriter out = new StringWriter();
    copy(in, out);
    String str = out.toString();
    if (str.startsWith("﻿")) {
      return str.substring(1);
    }
    return str;
  }
  
  public static byte[] copyToByteArray(InputStream in)
    throws IOException
  {
    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
    copy(in, out);
    return out.toByteArray();
  }
  
  private static void copy(Reader in, Writer out)
    throws IOException
  {
    try
    {
      char[] buffer = new char['က'];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      out.flush(); return;
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (IOException localIOException2) {}
      try
      {
        out.close();
      }
      catch (IOException localIOException3) {}
    }
  }
  
  private static int copy(InputStream in, OutputStream out)
    throws IOException
  {
    try
    {
      int byteCount = 0;
      byte[] buffer = new byte['က'];
      int bytesRead;
      while ((bytesRead = in.read(buffer)) != -1)
      {
        out.write(buffer, 0, bytesRead);
        byteCount += bytesRead;
      }
      out.flush();
      return byteCount;
    }
    finally
    {
      try
      {
        in.close();
      }
      catch (IOException localIOException2) {}
      try
      {
        out.close();
      }
      catch (IOException localIOException3) {}
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\FileCopyUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */