package com.sun.tools.xjc.model;

import org.w3c.dom.Element;
import org.xml.sax.Locator;

public class CPluginCustomization
{
  public final Element element;
  public final Locator locator;
  private boolean acknowledged;
  
  public void markAsAcknowledged()
  {
    this.acknowledged = true;
  }
  
  public CPluginCustomization(Element element, Locator locator)
  {
    this.element = element;
    this.locator = locator;
  }
  
  public boolean isAcknowledged()
  {
    return this.acknowledged;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CPluginCustomization.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */