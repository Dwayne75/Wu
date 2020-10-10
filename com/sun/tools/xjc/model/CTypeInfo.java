package com.sun.tools.xjc.model;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.TypeInfo;

public abstract interface CTypeInfo
  extends TypeInfo<NType, NClass>, CCustomizable
{
  public abstract JType toType(Outline paramOutline, Aspect paramAspect);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CTypeInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */