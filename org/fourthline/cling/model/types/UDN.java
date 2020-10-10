package org.fourthline.cling.model.types;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.logging.Logger;
import org.fourthline.cling.model.ModelUtil;

public class UDN
{
  private static final Logger log = Logger.getLogger(UDN.class.getName());
  public static final String PREFIX = "uuid:";
  private String identifierString;
  
  public UDN(String identifierString)
  {
    this.identifierString = identifierString;
  }
  
  public UDN(UUID uuid)
  {
    this.identifierString = uuid.toString();
  }
  
  public boolean isUDA11Compliant()
  {
    try
    {
      UUID.fromString(this.identifierString);
      return true;
    }
    catch (IllegalArgumentException ex) {}
    return false;
  }
  
  public String getIdentifierString()
  {
    return this.identifierString;
  }
  
  public static UDN valueOf(String udnString)
  {
    return new UDN(udnString.startsWith("uuid:") ? udnString.substring("uuid:".length()) : udnString);
  }
  
  public static UDN uniqueSystemIdentifier(String salt)
  {
    StringBuilder systemSalt = new StringBuilder();
    if (!ModelUtil.ANDROID_RUNTIME) {
      try
      {
        systemSalt.append(new String(ModelUtil.getFirstNetworkInterfaceHardwareAddress(), "UTF-8"));
      }
      catch (UnsupportedEncodingException ex)
      {
        throw new RuntimeException(ex);
      }
    } else {
      throw new RuntimeException("This method does not create a unique identifier on Android, see the Javadoc and use new UDN(UUID) instead!");
    }
    try
    {
      byte[] hash = MessageDigest.getInstance("MD5").digest(systemSalt.toString().getBytes("UTF-8"));
      
      return new UDN(new UUID(new BigInteger(-1, hash).longValue(), salt.hashCode()));
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
  }
  
  public String toString()
  {
    return "uuid:" + getIdentifierString();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (!(o instanceof UDN))) {
      return false;
    }
    UDN udn = (UDN)o;
    return this.identifierString.equals(udn.identifierString);
  }
  
  public int hashCode()
  {
    return this.identifierString.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\UDN.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */