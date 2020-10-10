package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class InstallerDesc
  implements XMLable
{
  private String _mainClass;
  
  public InstallerDesc(String paramString)
  {
    this._mainClass = paramString;
  }
  
  public String getMainClass()
  {
    return this._mainClass;
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("main-class", this._mainClass);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("installer-desc", localXMLAttributeBuilder.getAttributeList());
    return localXMLNodeBuilder.getNode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\InstallerDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */