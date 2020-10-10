package org.fourthline.cling.support.model.dlna;

import java.util.EnumMap;
import java.util.Map;
import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.support.model.Protocol;
import org.fourthline.cling.support.model.ProtocolInfo;
import org.seamless.util.MimeType;

public class DLNAProtocolInfo
  extends ProtocolInfo
{
  protected final Map<DLNAAttribute.Type, DLNAAttribute> attributes = new EnumMap(DLNAAttribute.Type.class);
  
  public DLNAProtocolInfo(String s)
    throws InvalidValueException
  {
    super(s);
    parseAdditionalInfo();
  }
  
  public DLNAProtocolInfo(MimeType contentFormatMimeType)
  {
    super(contentFormatMimeType);
  }
  
  public DLNAProtocolInfo(DLNAProfiles profile)
  {
    super(MimeType.valueOf(profile.getContentFormat()));
    this.attributes.put(DLNAAttribute.Type.DLNA_ORG_PN, new DLNAProfileAttribute(profile));
    this.additionalInfo = getAttributesString();
  }
  
  public DLNAProtocolInfo(DLNAProfiles profile, EnumMap<DLNAAttribute.Type, DLNAAttribute> attributes)
  {
    super(MimeType.valueOf(profile.getContentFormat()));
    this.attributes.putAll(attributes);
    this.attributes.put(DLNAAttribute.Type.DLNA_ORG_PN, new DLNAProfileAttribute(profile));
    this.additionalInfo = getAttributesString();
  }
  
  public DLNAProtocolInfo(Protocol protocol, String network, String contentFormat, String additionalInfo)
  {
    super(protocol, network, contentFormat, additionalInfo);
    parseAdditionalInfo();
  }
  
  public DLNAProtocolInfo(Protocol protocol, String network, String contentFormat, EnumMap<DLNAAttribute.Type, DLNAAttribute> attributes)
  {
    super(protocol, network, contentFormat, "");
    this.attributes.putAll(attributes);
    this.additionalInfo = getAttributesString();
  }
  
  public DLNAProtocolInfo(ProtocolInfo template)
  {
    this(template.getProtocol(), template
      .getNetwork(), template
      .getContentFormat(), template
      .getAdditionalInfo());
  }
  
  public boolean contains(DLNAAttribute.Type type)
  {
    return this.attributes.containsKey(type);
  }
  
  public DLNAAttribute getAttribute(DLNAAttribute.Type type)
  {
    return (DLNAAttribute)this.attributes.get(type);
  }
  
  public Map<DLNAAttribute.Type, DLNAAttribute> getAttributes()
  {
    return this.attributes;
  }
  
  protected String getAttributesString()
  {
    String s = "";
    for (DLNAAttribute.Type type : DLNAAttribute.Type.values())
    {
      String value = this.attributes.containsKey(type) ? ((DLNAAttribute)this.attributes.get(type)).getString() : null;
      if ((value != null) && (value.length() != 0)) {
        s = s + (s.length() == 0 ? "" : ";") + type.getAttributeName() + "=" + value;
      }
    }
    return s;
  }
  
  protected void parseAdditionalInfo()
  {
    if (this.additionalInfo != null)
    {
      String[] atts = this.additionalInfo.split(";");
      for (String att : atts)
      {
        String[] attNameValue = att.split("=");
        if (attNameValue.length == 2)
        {
          DLNAAttribute.Type type = DLNAAttribute.Type.valueOfAttributeName(attNameValue[0]);
          if (type != null)
          {
            DLNAAttribute dlnaAttrinute = DLNAAttribute.newInstance(type, attNameValue[1], getContentFormat());
            this.attributes.put(type, dlnaAttrinute);
          }
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAProtocolInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */