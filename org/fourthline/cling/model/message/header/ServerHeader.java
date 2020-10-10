package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.ServerClientTokens;

public class ServerHeader
  extends UpnpHeader<ServerClientTokens>
{
  public ServerHeader()
  {
    setValue(new ServerClientTokens());
  }
  
  public ServerHeader(ServerClientTokens tokens)
  {
    setValue(tokens);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    ServerClientTokens serverClientTokens = new ServerClientTokens();
    
    serverClientTokens.setOsName("UNKNOWN");
    serverClientTokens.setOsVersion("UNKNOWN");
    serverClientTokens.setProductName("UNKNOWN");
    serverClientTokens.setProductVersion("UNKNOWN");
    if (s.contains("UPnP/1.1")) {
      serverClientTokens.setMinorVersion(1);
    } else if (!s.contains("UPnP/1.")) {
      throw new InvalidHeaderException("Missing 'UPnP/1.' in server information: " + s);
    }
    try
    {
      int numberOfSpaces = 0;
      for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) == ' ') {
          numberOfSpaces++;
        }
      }
      String[] productNameVersion;
      String[] osNameVersion;
      String[] productNameVersion;
      if (s.contains(","))
      {
        String[] productTokens = s.split(",");
        String[] osNameVersion = productTokens[0].split("/");
        productNameVersion = productTokens[2].split("/");
      }
      else
      {
        String[] productNameVersion;
        if (numberOfSpaces > 2)
        {
          String beforeUpnpToken = s.substring(0, s.indexOf("UPnP/1.")).trim();
          String afterUpnpToken = s.substring(s.indexOf("UPnP/1.") + 8).trim();
          String[] osNameVersion = beforeUpnpToken.split("/");
          productNameVersion = afterUpnpToken.split("/");
        }
        else
        {
          String[] productTokens = s.split(" ");
          osNameVersion = productTokens[0].split("/");
          productNameVersion = productTokens[2].split("/");
        }
      }
      serverClientTokens.setOsName(osNameVersion[0].trim());
      if (osNameVersion.length > 1) {
        serverClientTokens.setOsVersion(osNameVersion[1].trim());
      }
      serverClientTokens.setProductName(productNameVersion[0].trim());
      if (productNameVersion.length > 1) {
        serverClientTokens.setProductVersion(productNameVersion[1].trim());
      }
    }
    catch (Exception ex)
    {
      serverClientTokens.setOsName("UNKNOWN");
      serverClientTokens.setOsVersion("UNKNOWN");
      serverClientTokens.setProductName("UNKNOWN");
      serverClientTokens.setProductVersion("UNKNOWN");
    }
    setValue(serverClientTokens);
  }
  
  public String getString()
  {
    return ((ServerClientTokens)getValue()).getHttpToken();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\ServerHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */