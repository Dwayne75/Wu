package org.fourthline.cling.support.model;

public enum BrowseFlag
{
  METADATA("BrowseMetadata"),  DIRECT_CHILDREN("BrowseDirectChildren");
  
  private String protocolString;
  
  private BrowseFlag(String protocolString)
  {
    this.protocolString = protocolString;
  }
  
  public String toString()
  {
    return this.protocolString;
  }
  
  public static BrowseFlag valueOrNullOf(String s)
  {
    for (BrowseFlag browseFlag : ) {
      if (browseFlag.toString().equals(s)) {
        return browseFlag;
      }
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\BrowseFlag.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */