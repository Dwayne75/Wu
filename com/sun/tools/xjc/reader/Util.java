package com.sun.tools.xjc.reader;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.xml.sax.InputSource;

public class Util
{
  public static Object getFileOrURL(String fileOrURL)
    throws IOException
  {
    try
    {
      return new URL(fileOrURL);
    }
    catch (MalformedURLException e) {}
    return new File(fileOrURL).getCanonicalFile();
  }
  
  public static InputSource getInputSource(String fileOrURL)
  {
    try
    {
      Object o = getFileOrURL(fileOrURL);
      if ((o instanceof URL)) {
        return new InputSource(escapeSpace(((URL)o).toExternalForm()));
      }
      String url = ((File)o).toURL().toExternalForm();
      return new InputSource(escapeSpace(url));
    }
    catch (IOException e) {}
    return new InputSource(fileOrURL);
  }
  
  public static String escapeSpace(String url)
  {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < url.length(); i++) {
      if (url.charAt(i) == ' ') {
        buf.append("%20");
      } else {
        buf.append(url.charAt(i));
      }
    }
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\Util.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */