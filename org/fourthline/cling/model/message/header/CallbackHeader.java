package org.fourthline.cling.model.message.header;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CallbackHeader
  extends UpnpHeader<List<URL>>
{
  private static final Logger log = Logger.getLogger(CallbackHeader.class.getName());
  
  public CallbackHeader()
  {
    setValue(new ArrayList());
  }
  
  public CallbackHeader(List<URL> urls)
  {
    this();
    ((List)getValue()).addAll(urls);
  }
  
  public CallbackHeader(URL url)
  {
    this();
    ((List)getValue()).add(url);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() == 0) {
      return;
    }
    if ((!s.contains("<")) || (!s.contains(">"))) {
      throw new InvalidHeaderException("URLs not in brackets: " + s);
    }
    s = s.replaceAll("<", "");
    String[] split = s.split(">");
    try
    {
      List<URL> urls = new ArrayList();
      for (String sp : split)
      {
        sp = sp.trim();
        if (!sp.startsWith("http://"))
        {
          log.warning("Discarding non-http callback URL: " + sp);
        }
        else
        {
          URL url = new URL(sp);
          try
          {
            url.toURI();
          }
          catch (URISyntaxException ex)
          {
            log.log(Level.WARNING, "Discarding callback URL, not a valid URI on this platform: " + url, ex);
            continue;
          }
          urls.add(url);
        }
      }
      setValue(urls);
    }
    catch (MalformedURLException ex)
    {
      throw new InvalidHeaderException("Can't parse callback URLs from '" + s + "': " + ex);
    }
  }
  
  public String getString()
  {
    StringBuilder s = new StringBuilder();
    for (URL url : (List)getValue()) {
      s.append("<").append(url.toString()).append(">");
    }
    return s.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\CallbackHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */