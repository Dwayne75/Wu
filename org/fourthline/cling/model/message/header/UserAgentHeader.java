package org.fourthline.cling.model.message.header;

public class UserAgentHeader
  extends UpnpHeader<String>
{
  public UserAgentHeader() {}
  
  public UserAgentHeader(String s)
  {
    setValue(s);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    setValue(s);
  }
  
  public String getString()
  {
    return (String)getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\UserAgentHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */