package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLNode;

public class PropertyDesc
  implements ResourceType
{
  private String _key;
  private String _value;
  
  public PropertyDesc(String paramString1, String paramString2)
  {
    this._key = paramString1;
    this._value = paramString2;
  }
  
  String getKey()
  {
    return this._key;
  }
  
  String getValue()
  {
    return this._value;
  }
  
  public void visit(ResourceVisitor paramResourceVisitor)
  {
    paramResourceVisitor.visitPropertyDesc(this);
  }
  
  public XMLNode asXML()
  {
    return new XMLNode("property", new XMLAttribute("name", getKey(), new XMLAttribute("value", getValue())), null, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\PropertyDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */