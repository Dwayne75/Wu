package org.fourthline.cling.model.message.header;

public class MXHeader
  extends UpnpHeader<Integer>
{
  public static final Integer DEFAULT_VALUE = Integer.valueOf(3);
  
  public MXHeader()
  {
    setValue(DEFAULT_VALUE);
  }
  
  public MXHeader(Integer delayInSeconds)
  {
    setValue(delayInSeconds);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    try
    {
      value = Integer.valueOf(Integer.parseInt(s));
    }
    catch (Exception ex)
    {
      Integer value;
      throw new InvalidHeaderException("Can't parse MX seconds integer from: " + s);
    }
    Integer value;
    if ((value.intValue() < 0) || (value.intValue() > 120)) {
      setValue(DEFAULT_VALUE);
    } else {
      setValue(value);
    }
  }
  
  public String getString()
  {
    return ((Integer)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\MXHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */