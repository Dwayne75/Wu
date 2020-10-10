package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class AssociationDesc
  implements XMLable
{
  private String _extensions;
  private String _mimeType;
  
  public AssociationDesc(String paramString1, String paramString2)
  {
    this._extensions = paramString1;
    this._mimeType = paramString2;
  }
  
  public String getExtensions()
  {
    return this._extensions;
  }
  
  public String getMimeType()
  {
    return this._mimeType;
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("extensions", this._extensions);
    localXMLAttributeBuilder.add("mime-type", this._mimeType);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("association", localXMLAttributeBuilder.getAttributeList());
    
    return localXMLNodeBuilder.getNode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\AssociationDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */