package com.sun.tools.xjc.model;

import com.sun.xml.xsom.XSComponent;
import org.xml.sax.Locator;

public abstract interface CCustomizable
{
  public abstract CCustomizations getCustomizations();
  
  public abstract Locator getLocator();
  
  public abstract XSComponent getSchemaComponent();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CCustomizable.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */