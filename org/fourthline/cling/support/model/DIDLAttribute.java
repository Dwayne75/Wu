package org.fourthline.cling.support.model;

public class DIDLAttribute
{
  private String namespaceURI;
  private String prefix;
  private String value;
  
  public DIDLAttribute(String namespaceURI, String prefix, String value)
  {
    this.namespaceURI = namespaceURI;
    this.prefix = prefix;
    this.value = value;
  }
  
  public String getNamespaceURI()
  {
    return this.namespaceURI;
  }
  
  public String getPrefix()
  {
    return this.prefix;
  }
  
  public String getValue()
  {
    return this.value;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\DIDLAttribute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */