package com.sun.tools.xjc.api;

import java.util.List;
import javax.xml.namespace.QName;

public abstract interface Mapping
{
  public abstract QName getElement();
  
  public abstract TypeAndAnnotation getType();
  
  public abstract List<? extends Property> getWrapperStyleDrilldown();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\Mapping.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */