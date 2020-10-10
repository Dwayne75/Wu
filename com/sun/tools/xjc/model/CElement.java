package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.Element;

public abstract interface CElement
  extends CTypeInfo, Element<NType, NClass>
{
  public abstract void setAbstract();
  
  public abstract boolean isAbstract();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */