package org.fourthline.cling.model;

public class ServerClientTokens
{
  public static final String UNKNOWN_PLACEHOLDER = "UNKNOWN";
  private int majorVersion = 1;
  private int minorVersion = 0;
  private String osName = System.getProperty("os.name").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
  private String osVersion = System.getProperty("os.version").replaceAll("[^a-zA-Z0-9\\.\\-_]", "");
  private String productName = "Cling";
  private String productVersion = "2.0";
  
  public ServerClientTokens() {}
  
  public ServerClientTokens(int majorVersion, int minorVersion)
  {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
  }
  
  public ServerClientTokens(String productName, String productVersion)
  {
    this.productName = productName;
    this.productVersion = productVersion;
  }
  
  public ServerClientTokens(int majorVersion, int minorVersion, String osName, String osVersion, String productName, String productVersion)
  {
    this.majorVersion = majorVersion;
    this.minorVersion = minorVersion;
    this.osName = osName;
    this.osVersion = osVersion;
    this.productName = productName;
    this.productVersion = productVersion;
  }
  
  public int getMajorVersion()
  {
    return this.majorVersion;
  }
  
  public void setMajorVersion(int majorVersion)
  {
    this.majorVersion = majorVersion;
  }
  
  public int getMinorVersion()
  {
    return this.minorVersion;
  }
  
  public void setMinorVersion(int minorVersion)
  {
    this.minorVersion = minorVersion;
  }
  
  public String getOsName()
  {
    return this.osName;
  }
  
  public void setOsName(String osName)
  {
    this.osName = osName;
  }
  
  public String getOsVersion()
  {
    return this.osVersion;
  }
  
  public void setOsVersion(String osVersion)
  {
    this.osVersion = osVersion;
  }
  
  public String getProductName()
  {
    return this.productName;
  }
  
  public void setProductName(String productName)
  {
    this.productName = productName;
  }
  
  public String getProductVersion()
  {
    return this.productVersion;
  }
  
  public void setProductVersion(String productVersion)
  {
    this.productVersion = productVersion;
  }
  
  public String toString()
  {
    return getOsName() + "/" + getOsVersion() + " UPnP/" + getMajorVersion() + "." + getMinorVersion() + " " + getProductName() + "/" + getProductVersion();
  }
  
  public String getHttpToken()
  {
    StringBuilder sb = new StringBuilder(256);
    sb.append(this.osName.indexOf(' ') != -1 ? this.osName.replace(' ', '_') : this.osName);
    sb.append('/');
    sb.append(this.osVersion.indexOf(' ') != -1 ? this.osVersion.replace(' ', '_') : this.osVersion);
    sb.append(" UPnP/");
    sb.append(this.majorVersion);
    sb.append('.');
    sb.append(this.minorVersion);
    sb.append(' ');
    sb.append(this.productName.indexOf(' ') != -1 ? this.productName.replace(' ', '_') : this.productName);
    sb.append('/');
    sb.append(this.productVersion.indexOf(' ') != -1 ? this.productVersion.replace(' ', '_') : this.productVersion);
    
    return sb.toString();
  }
  
  public String getOsToken()
  {
    return getOsName().replaceAll(" ", "_") + "/" + getOsVersion().replaceAll(" ", "_");
  }
  
  public String getProductToken()
  {
    return getProductName().replaceAll(" ", "_") + "/" + getProductVersion().replaceAll(" ", "_");
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    ServerClientTokens that = (ServerClientTokens)o;
    if (this.majorVersion != that.majorVersion) {
      return false;
    }
    if (this.minorVersion != that.minorVersion) {
      return false;
    }
    if (!this.osName.equals(that.osName)) {
      return false;
    }
    if (!this.osVersion.equals(that.osVersion)) {
      return false;
    }
    if (!this.productName.equals(that.productName)) {
      return false;
    }
    if (!this.productVersion.equals(that.productVersion)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.majorVersion;
    result = 31 * result + this.minorVersion;
    result = 31 * result + this.osName.hashCode();
    result = 31 * result + this.osVersion.hashCode();
    result = 31 * result + this.productName.hashCode();
    result = 31 * result + this.productVersion.hashCode();
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\ServerClientTokens.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */