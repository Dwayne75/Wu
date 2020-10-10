package org.fourthline.cling.support.model;

import org.fourthline.cling.model.types.InvalidValueException;
import org.seamless.util.MimeType;

public class ProtocolInfo
{
  public static final String WILDCARD = "*";
  protected Protocol protocol = Protocol.ALL;
  protected String network = "*";
  protected String contentFormat = "*";
  protected String additionalInfo = "*";
  
  public ProtocolInfo(String s)
    throws InvalidValueException
  {
    if (s == null) {
      throw new NullPointerException();
    }
    s = s.trim();
    String[] split = s.split(":");
    if (split.length != 4) {
      throw new InvalidValueException("Can't parse ProtocolInfo string: " + s);
    }
    this.protocol = Protocol.value(split[0]);
    this.network = split[1];
    this.contentFormat = split[2];
    this.additionalInfo = split[3];
  }
  
  public ProtocolInfo(MimeType contentFormatMimeType)
  {
    this.protocol = Protocol.HTTP_GET;
    this.contentFormat = contentFormatMimeType.toString();
  }
  
  public ProtocolInfo(Protocol protocol, String network, String contentFormat, String additionalInfo)
  {
    this.protocol = protocol;
    this.network = network;
    this.contentFormat = contentFormat;
    this.additionalInfo = additionalInfo;
  }
  
  public Protocol getProtocol()
  {
    return this.protocol;
  }
  
  public String getNetwork()
  {
    return this.network;
  }
  
  public String getContentFormat()
  {
    return this.contentFormat;
  }
  
  public MimeType getContentFormatMimeType()
    throws IllegalArgumentException
  {
    return MimeType.valueOf(this.contentFormat);
  }
  
  public String getAdditionalInfo()
  {
    return this.additionalInfo;
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    ProtocolInfo that = (ProtocolInfo)o;
    if (!this.additionalInfo.equals(that.additionalInfo)) {
      return false;
    }
    if (!this.contentFormat.equals(that.contentFormat)) {
      return false;
    }
    if (!this.network.equals(that.network)) {
      return false;
    }
    if (this.protocol != that.protocol) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.protocol.hashCode();
    result = 31 * result + this.network.hashCode();
    result = 31 * result + this.contentFormat.hashCode();
    result = 31 * result + this.additionalInfo.hashCode();
    return result;
  }
  
  public String toString()
  {
    return this.protocol.toString() + ":" + this.network + ":" + this.contentFormat + ":" + this.additionalInfo;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\ProtocolInfo.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */