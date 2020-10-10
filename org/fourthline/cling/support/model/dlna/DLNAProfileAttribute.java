package org.fourthline.cling.support.model.dlna;

public class DLNAProfileAttribute
  extends DLNAAttribute<DLNAProfiles>
{
  public DLNAProfileAttribute()
  {
    setValue(DLNAProfiles.NONE);
  }
  
  public DLNAProfileAttribute(DLNAProfiles profile)
  {
    setValue(profile);
  }
  
  public void setString(String s, String cf)
    throws InvalidDLNAProtocolAttributeException
  {
    DLNAProfiles value = DLNAProfiles.valueOf(s, cf);
    if (value == null) {
      throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA profile from: " + s);
    }
    setValue(value);
  }
  
  public String getString()
  {
    return ((DLNAProfiles)getValue()).getCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAProfileAttribute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */