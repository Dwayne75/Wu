package org.fourthline.cling.support.model.dlna;

public class DLNAConversionIndicatorAttribute
  extends DLNAAttribute<DLNAConversionIndicator>
{
  public DLNAConversionIndicatorAttribute()
  {
    setValue(DLNAConversionIndicator.NONE);
  }
  
  public DLNAConversionIndicatorAttribute(DLNAConversionIndicator indicator)
  {
    setValue(indicator);
  }
  
  public void setString(String s, String cf)
    throws InvalidDLNAProtocolAttributeException
  {
    DLNAConversionIndicator value = null;
    try
    {
      value = DLNAConversionIndicator.valueOf(Integer.parseInt(s));
    }
    catch (NumberFormatException localNumberFormatException) {}
    if (value == null) {
      throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA play speed integer from: " + s);
    }
    setValue(value);
  }
  
  public String getString()
  {
    return Integer.toString(((DLNAConversionIndicator)getValue()).getCode());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAConversionIndicatorAttribute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */