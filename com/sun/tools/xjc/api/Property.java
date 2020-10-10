package com.sun.tools.xjc.api;

import com.sun.codemodel.JType;
import javax.xml.namespace.QName;

public abstract interface Property
{
  public abstract String name();
  
  public abstract JType type();
  
  public abstract QName elementName();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\Property.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */