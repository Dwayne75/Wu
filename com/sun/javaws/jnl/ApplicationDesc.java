package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class ApplicationDesc
  implements XMLable
{
  private String _mainClass;
  private String[] _arguments;
  
  public ApplicationDesc(String paramString, String[] paramArrayOfString)
  {
    this._mainClass = paramString;
    this._arguments = paramArrayOfString;
  }
  
  public String getMainClass()
  {
    return this._mainClass;
  }
  
  public String[] getArguments()
  {
    return this._arguments;
  }
  
  public void setArguments(String[] paramArrayOfString)
  {
    this._arguments = paramArrayOfString;
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("main-class", this._mainClass);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("application-desc", localXMLAttributeBuilder.getAttributeList());
    if (this._arguments != null) {
      for (int i = 0; i < this._arguments.length; i++) {
        localXMLNodeBuilder.add(new XMLNode("argument", null, new XMLNode(this._arguments[i]), null));
      }
    }
    return localXMLNodeBuilder.getNode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\ApplicationDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */