package com.sun.org.apache.xml.internal.resolver.helpers;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class FileURL
{
  public static URL makeURL(String pathname)
    throws MalformedURLException
  {
    if (pathname.startsWith("/")) {
      return new URL("file://" + pathname);
    }
    String userdir = System.getProperty("user.dir");
    userdir.replace('\\', '/');
    if (userdir.endsWith("/")) {
      return new URL("file:///" + userdir + pathname);
    }
    return new URL("file:///" + userdir + "/" + pathname);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\org\apache\xml\internal\resolver\helpers\FileURL.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */