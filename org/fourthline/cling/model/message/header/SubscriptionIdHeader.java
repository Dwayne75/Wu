package org.fourthline.cling.model.message.header;

public class SubscriptionIdHeader
  extends UpnpHeader<String>
{
  public static final String PREFIX = "uuid:";
  
  public SubscriptionIdHeader() {}
  
  public SubscriptionIdHeader(String value)
  {
    setValue(value);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (!s.startsWith("uuid:")) {
      throw new InvalidHeaderException("Invalid subscription ID header value, must start with 'uuid:': " + s);
    }
    setValue(s);
  }
  
  public String getString()
  {
    return (String)getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\SubscriptionIdHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */