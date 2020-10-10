package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

public class AppletDesc
  implements XMLable
{
  private String _name;
  private String _appletClass;
  private URL _documentBase;
  private int _width;
  private int _height;
  private Properties _params;
  
  public AppletDesc(String paramString1, String paramString2, URL paramURL, int paramInt1, int paramInt2, Properties paramProperties)
  {
    this._name = paramString1;
    this._appletClass = paramString2;
    this._documentBase = paramURL;
    this._width = paramInt1;
    this._height = paramInt2;
    this._params = paramProperties;
  }
  
  public String getName()
  {
    return this._name;
  }
  
  public String getAppletClass()
  {
    return this._appletClass;
  }
  
  public URL getDocumentBase()
  {
    return this._documentBase;
  }
  
  public int getWidth()
  {
    return this._width;
  }
  
  public int getHeight()
  {
    return this._height;
  }
  
  public Properties getParameters()
  {
    return this._params;
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("name", this._name);
    localXMLAttributeBuilder.add("code", this._appletClass);
    localXMLAttributeBuilder.add("documentbase", this._documentBase);
    localXMLAttributeBuilder.add("width", this._width);
    localXMLAttributeBuilder.add("height", this._height);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("applet-desc", localXMLAttributeBuilder.getAttributeList());
    if (this._params != null)
    {
      Enumeration localEnumeration = this._params.keys();
      while (localEnumeration.hasMoreElements())
      {
        String str1 = (String)localEnumeration.nextElement();
        String str2 = this._params.getProperty(str1);
        localXMLNodeBuilder.add(new XMLNode("param", new XMLAttribute("name", str1, new XMLAttribute("value", str2)), null, null));
      }
    }
    return localXMLNodeBuilder.getNode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\AppletDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */