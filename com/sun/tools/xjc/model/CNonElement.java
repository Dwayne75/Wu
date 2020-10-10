package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.NonElement;

public abstract interface CNonElement
  extends NonElement<NType, NClass>, TypeUse, CTypeInfo
{
  @Deprecated
  public abstract CNonElement getInfo();
  
  @Deprecated
  public abstract boolean isCollection();
  
  @Deprecated
  public abstract CAdapter getAdapterUse();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CNonElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */