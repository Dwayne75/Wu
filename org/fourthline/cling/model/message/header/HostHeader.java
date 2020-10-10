package org.fourthline.cling.model.message.header;

import org.fourthline.cling.model.types.HostPort;

public class HostHeader
  extends UpnpHeader<HostPort>
{
  int port = 1900;
  String group = "239.255.255.250";
  
  public HostHeader()
  {
    setValue(new HostPort(this.group, this.port));
  }
  
  public HostHeader(int port)
  {
    setValue(new HostPort(this.group, port));
  }
  
  public HostHeader(String host, int port)
  {
    setValue(new HostPort(host, port));
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.contains(":"))
    {
      try
      {
        this.port = Integer.valueOf(s.substring(s.indexOf(":") + 1)).intValue();
        this.group = s.substring(0, s.indexOf(":"));
        setValue(new HostPort(this.group, this.port));
      }
      catch (NumberFormatException ex)
      {
        throw new InvalidHeaderException("Invalid HOST header value, can't parse port: " + s + " - " + ex.getMessage());
      }
    }
    else
    {
      this.group = s;
      setValue(new HostPort(this.group, this.port));
    }
  }
  
  public String getString()
  {
    return ((HostPort)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\HostHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */