package com.sun.javaws.jnl;

import com.sun.deploy.xml.XMLAttribute;
import com.sun.deploy.xml.XMLAttributeBuilder;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLNodeBuilder;
import com.sun.deploy.xml.XMLable;

public class ShortcutDesc
  implements XMLable
{
  private boolean _online;
  private boolean _desktop;
  private boolean _menu;
  private String _submenu;
  
  public ShortcutDesc(boolean paramBoolean1, boolean paramBoolean2, boolean paramBoolean3, String paramString)
  {
    this._online = paramBoolean1;
    this._desktop = paramBoolean2;
    this._menu = paramBoolean3;
    this._submenu = paramString;
  }
  
  public boolean getOnline()
  {
    return this._online;
  }
  
  public boolean getDesktop()
  {
    return this._desktop;
  }
  
  public boolean getMenu()
  {
    return this._menu;
  }
  
  public String getSubmenu()
  {
    return this._submenu;
  }
  
  public XMLNode asXML()
  {
    XMLAttributeBuilder localXMLAttributeBuilder = new XMLAttributeBuilder();
    localXMLAttributeBuilder.add("online", this._online);
    XMLNodeBuilder localXMLNodeBuilder = new XMLNodeBuilder("shortcut", localXMLAttributeBuilder.getAttributeList());
    if (this._desktop) {
      localXMLNodeBuilder.add("desktop", null);
    }
    if (this._menu) {
      if (this._submenu == null) {
        localXMLNodeBuilder.add("menu", null);
      } else {
        localXMLNodeBuilder.add(new XMLNode("menu", new XMLAttribute("submenu", this._submenu)));
      }
    }
    return localXMLNodeBuilder.getNode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\jnl\ShortcutDesc.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */