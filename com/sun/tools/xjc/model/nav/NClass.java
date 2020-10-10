package com.sun.tools.xjc.model.nav;

import com.sun.codemodel.JClass;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;

public abstract interface NClass
  extends NType
{
  public abstract JClass toType(Outline paramOutline, Aspect paramAspect);
  
  public abstract boolean isAbstract();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\nav\NClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */