package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLable;
import com.sun.javaws.Globals;

public class ExtDownloadDesc
  implements XMLable
{
  private String _extensionPart;
  private String _part;
  private boolean _isLazy;
  
  public ExtDownloadDesc(String paramString1, String paramString2, boolean paramBoolean)
  {
    this._extensionPart = paramString1;
    this._part = paramString2;
    this._isLazy = ((paramBoolean) && (!Globals.isImportMode()));
  }
  
  public String getExtensionPart()
  {
    return this._extensionPart;
  }
  
  public String getPart()
  {
    return this._part;
  }
  
  public boolean isLazy()
  {
    return this._isLazy;
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("ext-part", this._extensionPart);
    localXMLAttributeBuilder.add("part", this._part);
    localXMLAttributeBuilder.add("download", this._isLazy ? "lazy" : "eager");
    return new XMLNode("ext-download", localXMLAttributeBuilder.getAttributeList());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\ExtDownloadDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */