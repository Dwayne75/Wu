package org.fourthline.cling.model.message.header;

public class EXTHeader
  extends UpnpHeader<String>
{
  public static final String DEFAULT_VALUE = "";
  
  public EXTHeader()
  {
    setValue("");
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if ((s != null) && (s.length() > 0)) {
      throw new InvalidHeaderException("Invalid EXT header, it has no value: " + s);
    }
  }
  
  public String getString()
  {
    return (String)getValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\EXTHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */