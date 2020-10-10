package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLable;

public class LibraryDesc
  implements XMLable
{
  private String _uniqueId;
  
  public LibraryDesc(String paramString)
  {
    this._uniqueId = paramString;
  }
  
  public String getUniqueId()
  {
    return this._uniqueId;
  }
  
  public XMLNode asXML()
  {
    return new XMLNode("library-desc", new XMLAttribute("unique-id", this._uniqueId));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\LibraryDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */