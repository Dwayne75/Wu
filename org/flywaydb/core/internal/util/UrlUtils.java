package org.flywaydb.core.internal.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

public class UrlUtils
{
  public static String toFilePath(URL url)
  {
    try
    {
      String filePath = new File(URLDecoder.decode(url.getPath().replace("+", "%2b"), "UTF-8")).getAbsolutePath();
      if (filePath.endsWith("/")) {
        return filePath.substring(0, filePath.length() - 1);
      }
      return filePath;
    }
    catch (UnsupportedEncodingException e)
    {
      throw new IllegalStateException("Can never happen", e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\UrlUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */