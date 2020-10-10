package com.sun.xml.bind.v2.model.runtime;

import com.sun.xml.bind.v2.model.core.ElementInfo;
import java.lang.reflect.Type;
import javax.xml.bind.JAXBElement;

public abstract interface RuntimeElementInfo
  extends ElementInfo<Type, Class>, RuntimeElement
{
  public abstract RuntimeClassInfo getScope();
  
  public abstract RuntimeElementPropertyInfo getProperty();
  
  public abstract Class<? extends JAXBElement> getType();
  
  public abstract RuntimeNonElement getContentType();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\runtime\RuntimeElementInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */