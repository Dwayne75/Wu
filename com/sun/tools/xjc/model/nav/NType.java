package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;

public abstract interface NType
{
  public abstract JType toType(Outline paramOutline, Aspect paramAspect);
  
  public abstract boolean isBoxedType();
  
  public abstract String fullName();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\nav\NType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */