package com.sun.xml.bind.v2.runtime;

public final class NameList
{
  public final String[] namespaceURIs;
  public final boolean[] nsUriCannotBeDefaulted;
  public final String[] localNames;
  public final int numberOfElementNames;
  public final int numberOfAttributeNames;
  
  public NameList(String[] namespaceURIs, boolean[] nsUriCannotBeDefaulted, String[] localNames, int numberElementNames, int numberAttributeNames)
  {
    this.namespaceURIs = namespaceURIs;
    this.nsUriCannotBeDefaulted = nsUriCannotBeDefaulted;
    this.localNames = localNames;
    this.numberOfElementNames = numberElementNames;
    this.numberOfAttributeNames = numberAttributeNames;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\NameList.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */